package org.rcsb.uniprot.config;

import org.rcsb.uniprot.auto.Uniprot;
import org.rcsb.uniprot.auto.tools.HttpResource;
import org.rcsb.uniprot.auto.tools.UniProtTools;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;

/**
 * Created by ap3 on 17/12/2014.
 */
public class RCSBUniProtMirror {


    public static final String SERVER = "sandboxwest.rcsb.org";
    public static final String PATH_UNIPROT = "pdbx/uniprot/";


    public static File getLocalFile(String accession) throws MalformedURLException {

        URL remoteURL = getSandboxURL(accession);

        File localFile = getLocalFileLocation(accession);

        HttpResource upFile = new HttpResource(remoteURL , localFile);

        if (upFile.isLocal()){

            return localFile;
        } else {
            boolean success = upFile.download();
            if (  success) {
                return  localFile;
            }
        }

        return null;
    }

    private static File getLocalFileLocation(String accession) {
        String xmlFile = accession + ".xml";

        File localFile = new File(System.getProperty("java.io.tmpdir")+"/"+xmlFile);
        return localFile;
    }

    private static URL getSandboxURL(String accession) throws MalformedURLException {
        String xmlDir = PATH_UNIPROT + accession.substring(0, 1) + "/" + accession.substring(1, 2) + "/" + accession.substring(2, 3) + "/" + accession.substring(3, 4) + "/";
        String xmlFile = accession + ".xml";
        URL remoteURL = new URL ("http://" + SERVER+ ":"+10601+"/"+ xmlDir + xmlFile);

        return remoteURL;
    }

    public static Uniprot getUniProtFromFile(String accession) throws FileNotFoundException, JAXBException {
        File localFile = null;

        try {
            localFile = RCSBUniProtMirror.getLocalFile(accession);
        } catch (Exception e){
            e.printStackTrace();
        }

        if ( localFile == null)

        {

            /// probably a Trembl ID!

            try {

                localFile = getLocalFileLocation(accession);
                localFile = UniProtTools.fetchFileFromUniProt(accession, localFile);

            } catch (Exception e) {
                e.printStackTrace();


                return null;
            }

        }

        InputStream inStream = new FileInputStream(localFile);

        Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);
        return up;
    }

    // clean up local file if exists...
    public static void delete(String accession){

        File localFile = getLocalFileLocation(accession);

        if ( localFile.exists())
            localFile.delete();
    }


}
