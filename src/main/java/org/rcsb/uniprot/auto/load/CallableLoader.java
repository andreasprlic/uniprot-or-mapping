package org.rcsb.uniprot.auto.load;

import org.rcsb.uniprot.auto.tools.HttpResource;
import org.rcsb.uniprot.auto.tools.JpaUtilsUniProt;
import org.rcsb.uniprot.auto.tools.UniProtTools;
import org.rcsb.uniprot.auto.Uniprot;

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


    public static final String UNI_SERVER = "www.uniprot.org";

    public static final String UNI_PATH = "/uniprot/";

    public static final String SERVER = "sandboxwest.rcsb.org";

    public static final String PATH = "pdbx/ext/";

    public static final String PATH_UNIPROT = "pdbx/uniprot/";

    public static final String FILE = "pdbtosp.txt";

    public static final String LOCAL_UNIPROT_DIR = "ftp://" + SERVER + "/pdbx/uniprot/";

    public static final int CONNECT_TIMEOUT_MS = 60000;
    public static final int READ_TIMEOUT_MS = 600000;

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

                String xmlDir = PATH_UNIPROT + accession.substring(0, 1) + "/" + accession.substring(1, 2) + "/" + accession.substring(2, 3) + "/" + accession.substring(3, 4) + "/";
                String xmlFile = accession + ".xml";
                URL remoteURL = new URL ("http://" + SERVER+ ":"+10601+"/"+ xmlDir + xmlFile);

                File localFile = new File(System.getProperty("java.io.tmpdir")+"/"+xmlFile);

                HttpResource upFile = new HttpResource(remoteURL , localFile);

                if (! upFile.isLocal()) {
                    boolean success = upFile.download();
                    if ( ! success) {
                        System.err.println("# " + jobNr +" Could not download " + accession);

                        /// probably a Trembl ID!

                        try {

                            // fetch directly from UniProt
                            URL u = new URL("http://" + UNI_SERVER + UNI_PATH + accession + ".xml");
                            URLConnection conn = u.openConnection();
                            // setting these timeouts ensures the client does not deadlock indefinitely
                            // when the server has problems.
                            conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
                            conn.setReadTimeout(READ_TIMEOUT_MS);
                            InputStream in = conn.getInputStream();

                            Files.copy(in, localFile.toPath());
                           // Files.copy(Path source, OutputStream out)

                            in.close();

                            System.out.println("got file from " + u);

                        } catch (Exception e) {
                            e.printStackTrace();


                            // System.exit(0);
                            badAccessions.add(accession);
                            if (debug)
                                System.exit(0);
                            continue;
                        }

                    }
                }

                if ( debug)
                    System.out.println("# loading " + accession);
                InputStream inStream = new FileInputStream(localFile);

                Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);

                em.persist(up);

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
                localFile.delete();

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
