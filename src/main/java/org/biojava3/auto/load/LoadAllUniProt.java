package org.biojava3.auto.load;

import org.biojava3.auto.tools.HttpResource;
import org.biojava3.auto.tools.JpaUtilsUniProt;
import org.biojava3.auto.tools.UniProtTools;
import org.biojava3.auto.uniprot.Uniprot;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


/**
 *  Loads ALL of UniProt into the local database;
 *
 * Created by ap3 on 19/08/2014.
 */
public class LoadAllUniProt {

    static int availableProcs = Runtime.getRuntime().availableProcessors();
    static int threadPoolSize = availableProcs - 1;
    static {
        if ( threadPoolSize < 1)
            threadPoolSize = 1;
    }
    static ExecutorService pool = Executors.newFixedThreadPool(threadPoolSize);


    public static void main(String[] args) {

        SortedSet<String> upVersions = UniProtTools.getAllCurrentUniProtACs();

        System.out.println("Loading " + upVersions.size() + " UniProt entries into DB.");

        try {

            int chunkSize = 5000;

            List<String> accessions = new ArrayList<String>();

            List<Future<List<String>>> futureResults = new ArrayList<Future<List<String>>>();
            for (String ac : upVersions) {

                accessions.add(ac);

                if (accessions.size() == chunkSize) {
                    // submit job

                    CallableLoader loader = new CallableLoader(accessions);

                    Future<List<String>> badResult = pool.submit(loader);
                    futureResults.add(badResult);

                    accessions = new ArrayList<String>();

                }

            }

            System.out.println("Broke up calculations into " + futureResults.size() + " jobs...");

            List<String> badResults = new ArrayList<String>();
            for (Future<List<String>> b : futureResults) {

                badResults.addAll(b.get());

            }

        } catch (Exception e){
            e.printStackTrace();
        }

    }

}
