package org.biojava3.auto.org.biojava3.auto.load;

import org.biojava3.auto.dao.UniprotDAO;
import org.biojava3.auto.dao.UniprotDAOImpl;
import org.biojava3.auto.tools.JpaUtilsUniProt;
import org.biojava3.auto.tools.UniProtTools;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Created by ap3 on 21/08/2014.
 */
public class LoadMissing {

    static int availableProcs = Runtime.getRuntime().availableProcessors();
    static int threadPoolSize = availableProcs - 1;
    static {
        if ( threadPoolSize < 1)
            threadPoolSize = 1;
    }
    static ExecutorService pool = Executors.newFixedThreadPool(threadPoolSize);


    public static void main(String[] args) {

        System.out.println("Using a thread pool size of " +threadPoolSize);

        SortedSet<String> upVersions = UniProtTools.getAllCurrentUniProtACs();

        System.out.println("UniProt currently contains " + upVersions.size() + " entries.");

        UniprotDAO dao = new UniprotDAOImpl();
        SortedSet<String> dbVersions = new TreeSet<String>(dao.getDbVersions().keySet());

        System.out.println("DB contains " + dbVersions.size() + " entries.");

        upVersions.removeAll(dbVersions);

        System.out.println("Loading missing " + upVersions.size() + " UniProt entries into DB.");

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

            // wrap up the remaining  accessions
            CallableLoader loader = new CallableLoader(accessions);
            Future<List<String>> badResult = pool.submit(loader);
            futureResults.add(badResult);


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
