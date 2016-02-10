package org.rcsb.uniprot.auto.tools;

import org.biojava.nbio.core.sequence.ProteinSequence;
import org.rcsb.uniprot.auto.Entry;
import org.rcsb.uniprot.auto.Uniprot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 * Created by ap3 on 13/08/2014.
 */
public class UniProtTools {

    private static final Logger logger = LoggerFactory.getLogger(UniProtTools.class);

    public static Uniprot readUniProtFromInputStream(InputStream inputStream) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(new Class[]{Uniprot.class});

        Unmarshaller um = ctx.createUnmarshaller();

        Uniprot uniprot = (Uniprot) um.unmarshal(inputStream);

        return uniprot;
    }

    public static final String UNI_SERVER = "www.uniprot.org";
    public static final String UNI_PATH = "/uniprot/";
    public static final int CONNECT_TIMEOUT_MS = 60000;
    public static final int READ_TIMEOUT_MS = 600000;

    public static File fetchFileFromUniProt(String accession, File localFile) throws IOException {

        long timeS = System.currentTimeMillis();
        // fetch directly from UniProt
        URL u = new URL("http://" + UNI_SERVER + UNI_PATH + accession + ".xml");
        URLConnection conn = u.openConnection();
        // setting these timeouts ensures the client does not deadlock indefinitely
        // when the server has problems.
        conn.setConnectTimeout(CONNECT_TIMEOUT_MS);
        conn.setReadTimeout(READ_TIMEOUT_MS);


        try (InputStream in = conn.getInputStream()) {

            Files.copy(in, localFile.toPath());
            // Files.copy(Path source, OutputStream out)

            in.close();
        }

        long timeE = System.currentTimeMillis();

        System.out.println("downloaded file from " + u + " in " + (timeE - timeS) + " ms.");

        return localFile;
    }

    public static final String upRegexp = "[OPQ][0-9][A-Z0-9]{3}[0-9]|[A-NR-Z][0-9]([A-Z][A-Z0-9]{2}[0-9]){1,2}";

    /**
     * Tests if an accession code is a valid UniProt accession code. Works with both old-style 6 character as well as new 10 character long ACs.
     *
     * @param accession
     * @return
     */
    public static boolean isValidUniProtAC(String accession) {
        return (accession.matches(upRegexp));
    }


    private static File cachedVersionsFile = new File(System.getProperty("java.io.tmpdir") + "/uniprotVersions.txt.gz");


    /** returns the URL to the XML file for a UniProt accession
     *
     * @param accession
     * @return
     */
    public static URL getURLforXML(String accession){

        String xml = String.format("http://www.uniprot.org/uniprot/%s.xml", accession);
        try {
            URL u = new URL(xml);
            return u;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }


    public static SortedMap<String, String> loadVersionsFromUniProt() {

        System.out.println("UniProtTools loadVersionsFromUniProt");

        long timeS = System.currentTimeMillis();
        int count = 0;

        SortedMap<String, String> ans = new TreeMap<String, String>();
        BufferedReader br = null;

        try {
            URL uniprotVersions = new URL(
                    "http://www.uniprot.org/uniprot/?query=existence%3a%22evidence+at+protein+level%22&compress=yes&format=tab&columns=id,version,last-modified");


            HttpResource resource = new HttpResource(uniprotVersions,cachedVersionsFile);
            if ( ! resource.isLocal())
                resource.download();


            System.out.println("reading uniprot versions from local file: " + cachedVersionsFile.getAbsolutePath());
            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(cachedVersionsFile))));

            String lineOfData = null;
            String[] parts = null;

            while ((lineOfData = br.readLine()) != null) {
                parts = lineOfData.split("\t");
                if (parts != null && parts.length > 1) {
                    ans.put(parts[0], parts[1]);
                    count++;
                }
            }
            br.close();
            br = null;
        } catch (Exception e) {
            e.printStackTrace();
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    //PdbLogger.ignoringError(e1);
                    System.err.println("UniProtTools Ignoring error " + e1.getMessage());
                }
            }
        }
        long timeE = System.currentTimeMillis();
        System.out.println("UniProtTools parsed " + count + " date records from UniProt.org in " + (timeE - timeS) / 1000 + " sec.");
        return ans;
    }



    private static File cachedUniProtACsFile = new File(System.getProperty("java.io.tmpdir") + "/uniprotEntriesReviewed.txt.gz");


    public static SortedSet<String> getAllCurrentUniProtACs(){
        //String uniProtQuery = "http://www.uniprot.org/uniprot/?query=reviewed%3ayes&force=yes&format=tab&columns=id,entry%20name,reviewed,genes,organism,length";
        String uniProtQuery = "http://www.uniprot.org/uniprot/?query=reviewed%3ayes&force=yes&format=list&compress=yes";
        System.out.println("Fetching " + uniProtQuery);
        SortedSet<String> data = new TreeSet<String>();
        BufferedReader br = null;
        try {
            HttpResource resource = new HttpResource(new URL(uniProtQuery), cachedUniProtACsFile);
            if (!resource.isLocal()) {
                resource.download();
            } else {
                logger.info("Re-using " + resource.cachedFile.getAbsolutePath());
            }

            br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(cachedUniProtACsFile))));

            String lineOfData = null;
            String[] parts = null;

            while ((lineOfData = br.readLine()) != null) {
               lineOfData = lineOfData.trim();
               if ( lineOfData.length() > 0)
                        data.add(lineOfData);


            }
            br.close();
            br = null;


        } catch (Exception e){
           logger.error(e.getMessage(),e);

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    //PdbLogger.ignoringError(e1);
                    logger.error("Could not close file {}. Error: {}", cachedUniProtACsFile, e1.getMessage());
                }
            }

            return null;

        }

        return data;

    }

    public  String getSequence(Uniprot up) {

        String sequence = null;

        for ( Entry entry : up.getEntry()){
            //System.out.println(entry.getName());
            sequence  = entry.getSequence().getValue();
            if ( sequence != null)
                return cleanSequence(sequence);
        }



        return sequence;
    }

    public static String cleanSequence(String sequence) {
        sequence = sequence.trim();
        sequence = sequence.replaceAll("\\s+", "");
        sequence = sequence.replaceAll("\\r\\n|\\r|\\n", "");
        return sequence;
    }

    public static  void prettyPrint(ProteinSequence s) {
        System.out.println("\t" + s.getAccession() + " \t " + s.getLength() + ":" );

        String rawSeq = s.getSequenceAsString();

        StringBuffer header = new StringBuffer();
        StringBuffer seqStr = new StringBuffer();

        int lineLength = 60;

        for (int i = 0 ; i < rawSeq.length() ; i ++){

            seqStr.append( rawSeq.charAt(i));

            if ( (i+1) % lineLength == 0) {

                header.append(String.format("%10s", (i+1)));
                System.out.println(header);
                System.out.println(seqStr);
                System.out.println();
                header = new StringBuffer();
                seqStr = new StringBuffer();

            }

            if ( ( (i+1)%lineLength ) != 0 && (i+1) % 10 == 0) {

                header.append(String.format("%10s ", (i+1)));

                seqStr.append(" ");

            }
        }

        System.out.println(header);
        System.out.println(seqStr);

    }


}
