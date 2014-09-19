package org.rcsb.uniprot.auto.tools;

import org.rcsb.uniprot.auto.Uniprot;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.*;
import java.net.URL;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.GZIPInputStream;

/**
 * Created by ap3 on 13/08/2014.
 */
public class UniProtTools {

    public static Uniprot readUniProtFromInputStream(InputStream inputStream) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(new Class[]{Uniprot.class});

        Unmarshaller um = ctx.createUnmarshaller();

        Uniprot uniprot = (Uniprot) um.unmarshal(inputStream);

        return uniprot;
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
        SortedSet<String> data = new TreeSet<String>();
        BufferedReader br = null;
        try {
            HttpResource resource = new HttpResource(new URL(uniProtQuery), cachedUniProtACsFile);
            if (!resource.isLocal()) {
                resource.download();
            } else {
                System.out.println("Re-using " + resource.cachedFile.getAbsolutePath());
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
           e.printStackTrace();

            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    //PdbLogger.ignoringError(e1);
                    System.err.println("UniProtTools Ignoring error " + e1.getMessage());
                }
            }

            return null;

        }

        return data;

    }

}
