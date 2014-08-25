package org.biojava3.auto.load;

import org.biojava.bio.structure.align.util.CliTools;
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

    public static void main(String[] args) {

        long timeS = System.currentTimeMillis();

        StartupParameters params = new StartupParameters();
        try {

            CliTools.configureBean(params, args);

        } catch (Exception e){
            e.printStackTrace();

            return ;
        }

        System.out.println(params);

        int threadPoolSize = params.getThreadSize();

        ExecutorService pool = Executors.newFixedThreadPool(threadPoolSize);

        SortedSet<String> upVersions = UniProtTools.getAllCurrentUniProtACs();

        System.out.println("UniProt currently contains " + upVersions.size() + " entries.");

        UniprotDAO dao = new UniprotDAOImpl();
        SortedSet<String> dbVersions = new TreeSet<String>(dao.getDbVersions().keySet());

        System.out.println("DB contains " + dbVersions.size() + " entries.");

        upVersions.removeAll(dbVersions);

        System.out.println("Loading missing " + upVersions.size() + " UniProt entries into DB.");


        int count = 0;
        try {


            int chunkSize = 5000;

            if ( params.getEndPosition() > -1 ) {
                int l = Math.abs(params.getEndPosition() - params.getStartPosition());
                chunkSize = l / params.getThreadSize();
            }

            List<String> accessions = new ArrayList<String>();

            List<Future<List<String>>> futureResults = new ArrayList<Future<List<String>>>();
            for (String ac : upVersions) {

                count++;

                if ( count < params.getStartPosition()) {
                    continue;
                }

                if ( params.getEndPosition() > -1) {
                    if ( count > params.getEndPosition())
                        continue;
                }


                accessions.add(ac);

                if (accessions.size() == chunkSize) {
                    // submit job
                    CallableLoader loader = new CallableLoader(accessions);
                    loader.setParams(params);
                    Future<List<String>> badResult = pool.submit(loader);
                    futureResults.add(badResult);

                    accessions = new ArrayList<String>();

                }

            }

            // wrap up the remaining  accessions
            CallableLoader loader = new CallableLoader(accessions);
            loader.setParams(params);
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

        long timeE = System.currentTimeMillis();
        System.out.println("All OK! Total time: " + (timeE-timeS)/1000 + " sec.");
        System.exit(0);

    }
}
