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
import java.util.concurrent.Callable;

/**
 * Created by ap3 on 21/08/2014.
 */
public class CallableLoader implements Callable<List<String>> {


    List<String> accessions2load;


    public static final String UNI_PATH = "/uniprot/";

    public static final String SERVER = "sandboxwest.rcsb.org";

    public static final String PATH = "pdbx/ext/";

    public static final String PATH_UNIPROT = "pdbx/uniprot/";

    public static final String FILE = "pdbtosp.txt";

    public static final String LOCAL_UNIPROT_DIR = "ftp://" + SERVER + "/pdbx/uniprot/";



    static Integer jobs = -1;


    Integer jobNr;
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

        System.out.println("# " + jobNr +" starting calculations");

        long timeS = System.currentTimeMillis();
        List<String> badAccessions = new ArrayList<String>();

        int count = 0;

        long totalTime = 0;
        EntityManager em = JpaUtilsUniProt.getEntityManager();

        em.getTransaction().begin();

        for (String accession : accessions2load){


            count++;
            try {

                String xmlDir = PATH_UNIPROT + accession.substring(0, 1) + "/" + accession.substring(1, 2) + "/" + accession.substring(2, 3) + "/" + accession.substring(3, 4) + "/";
                String xmlFile = accession + ".xml";
                URL remoteURL = new URL ("http://" + SERVER+ ":"+10601+"/"+ xmlDir + xmlFile);

                File localFile = new File(System.getProperty("java.io.tmpdir")+"/"+xmlFile);

                HttpResource upFile = new HttpResource(remoteURL , localFile);

                if (! upFile.isLocal()) {
                    boolean success = upFile.download();
                    if ( ! success) {
                        System.err.println("# " + jobNr +" Could not load " + accession);
                        // System.exit(0);
                        badAccessions.add(accession);
                        continue;

                    }
                }


                InputStream inStream = new FileInputStream(localFile);

                Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);

                em.persist(up);

                if (count %500 == 0 && count >0) {
                    System.out.println("# " + jobNr +" Committing transaction. pos #" + count);
                    if ( em.getTransaction().isActive())
                        em.getTransaction().commit();
                    em.close();
                    em = JpaUtilsUniProt.getEntityManager();
                    em.getTransaction().begin();
                    System.out.println("# " + jobNr + " badlist size:" + badAccessions.size());
                    System.out.println(badAccessions);
                }
                localFile.delete();

            } catch (Exception e){
                System.err.println("# " + jobNr + " Failed to load " + accession);
                e.printStackTrace();
                //System.exit(-1);
                badAccessions.add(accession);
                if ( em.getTransaction().isActive())
                    em.getTransaction().rollback();
                em.close();
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
        totalTime += diff;
        System.out.println("# " + jobNr +" " + count +"/" + accessions2load.size() + " loaded " + accessions2load.size() +" entries in " + diff/1000 +" sec. (average: " + totalTime/count + "ms.");
        System.out.println("# " + jobNr + " done! ");
        return badAccessions;


    }
}