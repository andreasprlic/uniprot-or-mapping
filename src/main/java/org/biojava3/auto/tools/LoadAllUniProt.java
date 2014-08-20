package org.biojava3.auto.tools;

import org.biojava3.auto.uniprot.Uniprot;

import javax.persistence.EntityManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.SortedMap;
import java.util.SortedSet;


/**
 *  Loads ALL of UniProt into the local database;
 *
 * Created by ap3 on 19/08/2014.
 */
public class LoadAllUniProt {

    public static final String UNI_PATH = "/uniprot/";

    public static final String SERVER = "sandboxwest.rcsb.org";

    public static final String PATH = "pdbx/ext/";

    public static final String PATH_UNIPROT = "pdbx/uniprot/";

    public static final String FILE = "pdbtosp.txt";

    public static final String LOCAL_UNIPROT_DIR = "ftp://" + SERVER + "/pdbx/uniprot/";



    public static void main(String[] args) {

        SortedSet<String> upVersions = UniProtTools.getAllCurrentUniProtACs();

        System.out.println("Loading " + upVersions.size() + " UniProt entries into DB.");

        int count = 0;

        long totalTime = 0;
        EntityManager em = JpaUtilsUniProt.getEntityManager();

        em.getTransaction().begin();

        for (String accession : upVersions){

            long timeS = System.currentTimeMillis();
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
                        System.err.println("Could not load " + accession);
                        System.exit(0);

                    }
                }


                InputStream inStream = new FileInputStream(localFile);

                Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);

                em.persist(up);

                if (count %1000 == 0 && count >0) {
                    System.out.println("@@@Committing transaction@@@");
                    em.getTransaction().commit();
                    em.getTransaction().begin();
                }
                localFile.delete();

            } catch (Exception e){
                System.err.println("Failed to load " + accession);
                e.printStackTrace();
                System.exit(-1);
            }


            long timeE = System.currentTimeMillis();

            long diff = (timeE - timeS);

            totalTime += diff;
            System.out.println("#" + count +"/" + upVersions.size() + " loaded " + accession +" in " + diff +" ms. (average: " + totalTime/count + "ms.");
        }
        // commit open transaction
        if (  count % 1000 !=0)
            em.getTransaction().commit();

    }

}
