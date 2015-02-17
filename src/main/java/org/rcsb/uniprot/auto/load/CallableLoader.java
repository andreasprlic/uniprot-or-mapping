package org.rcsb.uniprot.auto.load;

import org.rcsb.uniprot.auto.tools.HttpResource;
import org.rcsb.uniprot.auto.tools.JpaUtilsUniProt;
import org.rcsb.uniprot.auto.tools.UniProtTools;
import org.rcsb.uniprot.auto.Uniprot;
import org.rcsb.uniprot.config.RCSBUniProtMirror;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by ap3 on 21/08/2014.
 */
public class CallableLoader implements Callable<List<String>> {


    boolean debug = false;

    List<String> accessions2load;






    public static final String PATH = "pdbx/ext/";



    public static final String FILE = "pdbtosp.txt";

   // public static final String LOCAL_UNIPROT_DIR = "ftp://" + SERVER + "/pdbx/uniprot/";



    static Integer jobs = -1;


    Integer jobNr;

    StartupParameters params = new StartupParameters();
    /**
     * Send a list of uniprot accessions to be loader
     *
     * @param uniprotAccessions
     */
    public CallableLoader(List<String> uniprotAccessions) {

        synchronized (jobs){
            jobs++;
            jobNr = jobs;
        }

        accessions2load = uniprotAccessions;
    }

    public List<String> call() {

        System.out.println("# " + jobNr +" starting to load " + accessions2load.size() + " UP entries");

        long timeS = System.currentTimeMillis();
        List<String> badAccessions = new ArrayList<String>();

        int count = 0;

        long totalTime = 0;

        EntityManager em = JpaUtilsUniProt.getEntityManager();

        em.getTransaction().begin();

        for (String accession : accessions2load){

            count++;

            try {


                Uniprot up = RCSBUniProtMirror.getUniProtFromFile(accession);

                if ( up == null){

                    badAccessions.add(accession);
                    RCSBUniProtMirror.delete(accession);
                    continue;
                }

                em.persist(up);
                RCSBUniProtMirror.delete(accession);

                if (count % params.getCommitSize() == 0 && count >0) {
                    System.out.println("# " + jobNr +" Committing transaction. pos #" + count);
                    if ( em.getTransaction().isActive())
                        em.getTransaction().commit();
                    em.close();
                    em = JpaUtilsUniProt.getEntityManager();
                    em.getTransaction().begin();
                    long timeN = System.currentTimeMillis();
                    System.out.println("# " + jobNr + " badlist size:" + badAccessions.size() + " speed: " + (timeN-timeS)/count + " ms. / entry");
                    System.out.println(badAccessions);
                }



            } catch (Exception e){
                System.err.println("# " + jobNr + " Failed to load " + accession);
                e.printStackTrace();
                //System.exit(-1);
                badAccessions.add(accession);
                if ( em.getTransaction().isActive())
                    em.getTransaction().rollback();
                em.close();

                if ( debug)
                    System.exit(0);

                em = JpaUtilsUniProt.getEntityManager();
                continue;
            }





            //  System.out.println("# " + jobNr +" " + count +"/" + accessions2load.size() + " loaded " + accession +" in " + diff +" ms. (average: " + totalTime/count + "ms.");
        }

        // commit ongoing transaction
        if (  em.getTransaction().isActive())

            em.getTransaction().commit();

        long timeE = System.currentTimeMillis();

        long diff = (timeE - timeS);
        if ( diff == 0)
            diff = 1;
        totalTime += diff;
        System.out.println("# " + jobNr +" " + count +"/" + accessions2load.size() + " loaded " + accessions2load.size() +" entries in " + diff/1000 +" sec. (average: " + totalTime/count + "ms.)");
        System.out.println("# " + jobNr + " done! ");
        return badAccessions;


    }

    public StartupParameters getParams() {
        return params;
    }

    public void setParams(StartupParameters params) {
        this.params = params;
    }
}
