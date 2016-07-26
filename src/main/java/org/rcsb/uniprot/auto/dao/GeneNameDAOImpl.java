package org.rcsb.uniprot.auto.dao;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.zip.GZIPInputStream;

import com.google.common.collect.Range;
import org.biojava.nbio.genome.parsers.genename.GeneChromosomePosition;
import org.biojava.nbio.genome.parsers.genename.GeneChromosomePositionParser;
import org.biojava.nbio.genome.parsers.genename.GeneName;
import org.biojava.nbio.genome.parsers.genename.GeneNamesParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ap3 on 03/02/2016.
 */
public class GeneNameDAOImpl implements GeneNameDAO{

    private static final Logger PdbLogger = LoggerFactory.getLogger(GeneNameDAOImpl.class);

    public static final String DEFAULT_ASSEMBLY_VERSION="hg37";

    private static Map<String, GeneName> geneNamesMap = null;
    private static Map<String, List<GeneChromosomePosition>> geneChromosomePositionMap37 = null;
    private static Map<String, List<GeneChromosomePosition>> geneChromosomePositionMap38 = null;


    public static final String COLLECTION_NAME = "geneNames";

    public static final String APPROVED_GENENAMES_COLLECTION_NAME = "approvedGeneNames";

    private static final String geneNameFile = "geneNames.tsf";

    private static final String geneChromosomeFile37 = "geneChromosome37.tsf.gz";
    private static final String geneChromosomeFile38 = "geneChromosome38.tsf.gz";


    private static File geneNamesFileResource ;
    private static File geneChromosomePositionsResource37;
    private static File geneChromosomePositionsResource38;



    public static void main(String[] args){
        GeneNameDAOImpl me = new GeneNameDAOImpl();

        long timeS = System.currentTimeMillis();
        SortedSet<String> geneNames = me.getAllApprovedGeneSymbol();
        long timeE = System.currentTimeMillis();
        System.out.println("all gene names: " + geneNames.size() + " gene names in "  + (timeE - timeS) + " ms.");

        timeS = System.currentTimeMillis();
        GeneName a2m = me.getGeneName("A2M");
        timeE = System.currentTimeMillis();
        System.out.println("gene by ID:" + a2m + " in " +  (timeE - timeS) + " ms.");

        timeS = System.currentTimeMillis();
        List<GeneChromosomePosition>  genePos = me.getChromosomePosition("A2M");
        timeE = System.currentTimeMillis();
        System.out.println("a chromosome pos:" + genePos + " in " +  (timeE - timeS) + " ms.");


        timeS = System.currentTimeMillis();
        SortedSet<String> gene4UP = me.getGeneNamesForUniprotId("P49796");
        timeE = System.currentTimeMillis();
        if (gene4UP == null) {
            System.err.println("no gene found for P49796!" );
        } else
            System.out.println("a gene for a UP:" + gene4UP.size() + " " + gene4UP.first() + " in " +  (timeE - timeS) + " ms.");

        timeS = System.currentTimeMillis();
        SortedSet<String> uniprots = me.getUniprotIdsWithGeneName();
        timeE = System.currentTimeMillis();
        System.out.println("all uniprots with gene names:" + uniprots.size() + " " + uniprots.first() + " in " +  (timeE - timeS) + " ms.");



        timeS = System.currentTimeMillis();
        SortedSet<String> related = me.findRelatedGeneNames("RNF17");
        timeE = System.currentTimeMillis();
        System.out.println("related gene names:" + related.size() + " " + related.first() + " in " +  (timeE - timeS) + " ms.");

        timeS = System.currentTimeMillis();
        String wrongID = "P50225";
        boolean known = me.isApprovedSymbol(wrongID);
        timeE = System.currentTimeMillis();
        System.out.println("wrong id:" + wrongID + " " + known + " in " +  (timeE - timeS) + " ms.");

        timeS = System.currentTimeMillis();
        String trueID = "ZNF691";
        boolean tknown = me.isApprovedSymbol(trueID);
        timeE = System.currentTimeMillis();
        System.out.println("true id:" + trueID + " " + tknown + " in " +  (timeE - timeS) + " ms.");

//		 timeS = System.currentTimeMillis();
//		SortedSet<GeneName> ggeneNames = me.getAllGeneNames();
//		 timeE = System.currentTimeMillis();
//		System.out.println("all full gene names: " + ggeneNames.size() + " gene names in "  + (timeE - timeS) + " ms.");

        List<GeneChromosomePosition>  chromosomePosition = me.getChromosomePosition("AK5", DEFAULT_ASSEMBLY_VERSION);
        System.out.println(chromosomePosition);

        List<GeneChromosomePosition> chromosomePosition2 = me.getChromosomePosition("AK5", "hg38");
        System.out.println(chromosomePosition2);

        List<GeneChromosomePosition>  chromosomePosition3 = me.getChromosomePosition("OBSCN", DEFAULT_ASSEMBLY_VERSION);
        for ( GeneChromosomePosition gpos : chromosomePosition3){
            System.out.println(gpos);
        }

        List<GeneChromosomePosition> chromosomePosition4 = me.getChromosomePosition("OBSCN", "hg38");
        System.out.println(chromosomePosition4);

        System.exit(0);
    }

    public void init() {


        // init geneNames

        // make sure this gets initialized only once.
        if (geneNamesMap != null)
            return;

        GeneNamesParser p = new GeneNamesParser();
        List<GeneChromosomePosition> genePositions37 = new ArrayList<GeneChromosomePosition>();
        List<GeneChromosomePosition> genePositions38 = new ArrayList<GeneChromosomePosition>();

        List<File> externalresources = getRequiredExternalResourcesList();

        File f = null;
        File f37 = null;
        File f38 = null;

        for (File resource : externalresources) {

            PdbLogger.info(resource.getName());

            if (resource.getName().equals(geneNameFile)) {
                f = resource;
            }

            if (resource.getName().equals(geneChromosomeFile37)) {
                f37 = resource;
            }
            if (resource.getName().equals(geneChromosomeFile38)) {
                f38 = resource;
            }

        }

        if (f37!=null)
            PdbLogger.info(f37.toString());

        InputStream instream = null;

        try {

            //File f = new File("/Users/ap3/Downloads/genenames.txt");
            instream = new FileInputStream(f);
            List<GeneName> geneNames = GeneNamesParser.getGeneNames(instream);

            geneNamesMap = new HashMap<String, GeneName>();
            for (GeneName gn : geneNames) {
                geneNamesMap.put(gn.getApprovedSymbol(), gn);
            }
            geneChromosomePositionMap37 = new HashMap<String, List<GeneChromosomePosition>>();
            geneChromosomePositionMap38 = new HashMap<String, List<GeneChromosomePosition>>();


            mapGeneNames2Position(geneChromosomeFile37, f37, genePositions37, geneChromosomePositionMap37);
            mapGeneNames2Position(geneChromosomeFile38, f38, genePositions38, geneChromosomePositionMap38);


        } catch (IOException e) {
            PdbLogger.error("Caught IOException while reading resource file {}. Error: {} ", f , e.getMessage());
        } finally {
            if (instream!=null) {
                try {
                    instream.close();
                } catch (IOException e) {
                    PdbLogger.error("Could not close stream opened from file {}. Error: {}", f, e.getMessage());
                }
            }
        }
    }

    public List<File> getRequiredExternalResourcesList()
    {
        if ( geneNamesFileResource == null)
            downloadRequiredExternalResources();


        List<File> answer = new ArrayList<>();


        answer.add(geneNamesFileResource);
        answer.add(geneChromosomePositionsResource37);
        answer.add(geneChromosomePositionsResource38);
        return answer;
    }

    public boolean downloadRequiredExternalResources() {


        try {
            URL geneNameURL = new URL(GeneNamesParser.DEFAULT_GENENAMES_URL);

            //String remoteFile = geneNameURL.getFile().substring(1);

            if ( geneNamesFileResource == null || ! geneNamesFileResource.exists()){

                geneNamesFileResource = downloadFile(geneNameURL, geneNameFile);
            }


            if ( geneChromosomePositionsResource37 == null || ! geneChromosomePositionsResource37.exists() ) {
                URL chromoUrl37 = new URL("http://hgdownload.cse.ucsc.edu/goldenPath/hg19/database/refFlat.txt.gz");

                geneChromosomePositionsResource37 = downloadFile(chromoUrl37,geneChromosomeFile37);
            }

            if ( geneChromosomePositionsResource38 == null || ! geneChromosomePositionsResource38.exists() ) {
                URL chromoUrl38 = new URL("http://hgdownload.cse.ucsc.edu/goldenPath/hg38/database/refFlat.txt.gz");

                geneChromosomePositionsResource38 = downloadFile(chromoUrl38,geneChromosomeFile38);
            }

        } catch (IOException e){
            PdbLogger.error("Caught IOException while downloading external resources: ", e.getMessage());
            return false;
        }

        return true;

    }

    private File downloadFile(URL website, String geneNameFile) throws IOException {
        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        File f = new File(geneNameFile);
        FileOutputStream fos = new FileOutputStream(f);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);

        return f;

    }

    private String getExonStrings(GeneChromosomePosition g) {
        return g.getExonStarts().toString()+ g.getExonEnds().toString();
    }


    private void mapGeneNames2Position(String geneChromosomeFile, File f,List<GeneChromosomePosition> genePositions,
                                       Map<String, List<GeneChromosomePosition>> geneChromosomePositionMap) throws IOException {

        //File f2 = new File("/Users/ap3/Downloads/refFlat.txt.gz");
        PdbLogger.info("parsing " + f.getAbsolutePath());
        InputStream instream2 = new GZIPInputStream(new FileInputStream(f));
        GeneChromosomePositionParser gp = new GeneChromosomePositionParser();


        genePositions = GeneChromosomePositionParser.getChromosomeMappings(instream2);

        for (GeneChromosomePosition pos : genePositions) {

            List<GeneChromosomePosition> posMap = geneChromosomePositionMap.get(pos.getGeneName());

            if (posMap == null) {
                posMap = new ArrayList<GeneChromosomePosition>();
                geneChromosomePositionMap.put(pos.getGeneName(), posMap);
            }

            String exonS1 = getExonStrings(pos);

            boolean found = false;
            for (GeneChromosomePosition g : posMap) {
                String exonS2 = getExonStrings(g);
                if (exonS1.equals(exonS2)) {
                    found = true;
                    break;
                }
            }

            if (!found)
                posMap.add(pos);


        }
        // there is multiple locations for this gene. let's make sure we use the one that
        // can get mapped based on the Refseq ID...

//                System.out.println("Multiple locations for gene " + pos.getGeneName() + " !!");


//                for ( GeneName geneName : geneNamesMap.values()) {
//                    if (geneName.getApprovedSymbol().equals(pos.getGeneName())) {
//                        if (geneName.getRefseqIds().contains(pos.getGenebankId())) {
//                            geneChromosomePositionMap.put(pos.getGeneName(), pos);
//                           // System.out.println(" replacing geneName  " + pos.getGeneName() + " with " +pos);
//                        } else {
////                            System.out.println("COULD NOT MAP  !");
////                            System.out.println(geneName);
////                            System.out.println(pos);
//                        }
//                    }
//                }

//                if ( geneNamesMap.containsKey(pos.getGeneName())) {
//                    GeneName geneName = geneNamesMap.get(pos.getGeneName());
//
//                    // let's try to map this by genbank ID
//                    if (geneName.getRefseqIds().contains(pos.getGenebankId()))
//                        geneChromosomePositionMap.put(pos.getGeneName(), pos);
//                }


        PdbLogger.info("Got " + geneChromosomePositionMap.keySet().size() + " human gene positions");



    }

    public boolean isApprovedSymbol(String testGeneSymbol) {
        if ( geneNamesMap == null)
            init();

        return geneNamesMap.containsKey(testGeneSymbol);

    }


    public SortedSet<String> getUniprotIdsWithGeneName() {

        if ( geneNamesMap == null)
            init();

        SortedSet<String> uniprotIDs = new TreeSet<String>();

        for ( GeneName geneName : geneNamesMap.values()){
            if ( ! uniprotIDs.contains(geneName.getUniprot()))
                uniprotIDs.add(geneName.getUniprot());
        }

        return uniprotIDs;
    }


    public SortedSet<String> getAllApprovedGeneSymbol() {

        if ( geneNamesMap == null)
            init();

        SortedSet<String> approvedNames =  new TreeSet<String>();
        for ( String gn : geneNamesMap.keySet()){
            if ( ! approvedNames.contains(gn))
                approvedNames.add(gn);
        }


        return approvedNames;
    }


    public SortedSet<String> getGeneNamesForUniprotId(String uniprotId) {
        SortedSet<String> data = new TreeSet<String>();

        if ( geneNamesMap == null)
            init();
        for (GeneName geneName : geneNamesMap.values()) {

            if ( geneName.getUniprot().equals(uniprotId))
                data.add(geneName.getApprovedSymbol());
        }

        return data;

    }



    public GeneName getGeneName(String approvedGeneSymbol) {
        if ( geneNamesMap == null)
            init();

        GeneName gn = geneNamesMap.get(approvedGeneSymbol);

        if (gn != null)
            return gn;

        for (String s : geneNamesMap.keySet()){
            if ( s.equalsIgnoreCase(approvedGeneSymbol))
                return geneNamesMap.get(s);
        }
        return null;

    }

    public List<GeneChromosomePosition>  getChromosomePosition(String approvedSymbol){
        return getChromosomePosition(approvedSymbol,DEFAULT_ASSEMBLY_VERSION);
    }

    public List<GeneChromosomePosition> getChromosomePosition(String approvedSymbol, String assemblyVersion) {

        if ( assemblyVersion == null )
            assemblyVersion = DEFAULT_ASSEMBLY_VERSION;

        List<GeneChromosomePosition> results;

        if ( assemblyVersion.equals("hg37"))
            results =  geneChromosomePositionMap37.get(approvedSymbol);
        else
            results = geneChromosomePositionMap38.get(approvedSymbol);

        if ( results == null)
            return new ArrayList<>();

        return results;

    }


    public List<GeneChromosomePosition> getChromosomePositionsInRange(String chromosome, int start, int end){
        return getChromosomePositionsInRange(chromosome, start, end, DEFAULT_ASSEMBLY_VERSION);
    }

    public List<GeneChromosomePosition> getChromosomePositionsInRange(String chromosome, int start, int end, String assemblyVersion){
        List<GeneChromosomePosition> data = new ArrayList<GeneChromosomePosition>();

        Map<String, List<GeneChromosomePosition>> geneChromosomePositionMap ;

        if ( assemblyVersion == null )
            assemblyVersion = DEFAULT_ASSEMBLY_VERSION;

        if ( assemblyVersion.equals("hg37")){
            geneChromosomePositionMap = geneChromosomePositionMap37;
        } else {
            geneChromosomePositionMap = geneChromosomePositionMap38;
        }


        Range search = Range.closed(start,end);
        for ( String gene : geneChromosomePositionMap.keySet()){

            List<GeneChromosomePosition> positions = geneChromosomePositionMap.get(gene);

            for ( GeneChromosomePosition pos : positions) {
                if (!pos.getChromosome().equalsIgnoreCase(chromosome))
                    continue;

                //Range r = Range.closed(pos.getCdsStart(), pos.getCdsEnd());
                Range r = Range.closed(pos.getTranscriptionStart(), pos.getTranscriptionEnd());
                PdbLogger.debug(pos.getGeneName() + " " + search.lowerEndpoint() + " " + search.upperEndpoint() + " " + r.lowerEndpoint() + " " + r.upperEndpoint());
                if (search.isConnected(r)) {
                    data.add(pos);
                }
            }
        }

        return data;
    }

    /** Check if a particular position on this chromosome is in a UTR
     *
     * @param pos
     * @param chromosome
     * @param chromosomePos
     * @param assemblyVersion
     * @return
     */
    public  boolean inExon(GeneChromosomePosition pos , String chromosome, int chromosomePos, String assemblyVersion){

        List<GeneChromosomePosition> data = new ArrayList<GeneChromosomePosition>();

        Map<String, List<GeneChromosomePosition>> geneChromosomePositionMap ;

        if ( assemblyVersion == null )
            assemblyVersion = DEFAULT_ASSEMBLY_VERSION;

        if ( assemblyVersion.equals("hg37")){
            geneChromosomePositionMap = geneChromosomePositionMap37;
        } else {
            geneChromosomePositionMap = geneChromosomePositionMap38;
        }


        Range search = Range.closed(chromosomePos,chromosomePos);

        if (!pos.getChromosome().equalsIgnoreCase(chromosome))
            return false;

        //Range r = Range.closed(pos.getCdsStart(), pos.getCdsEnd());
        Range fullRange = Range.closed(pos.getTranscriptionStart(), pos.getTranscriptionEnd());
        //System.out.println(pos.getGeneName() + " " + search.lowerEndpoint() + " " + search.upperEndpoint() + " " + r.lowerEndpoint() + " " + r.upperEndpoint());

        // not even within the boundaries of this gene
        if (! search.isConnected(fullRange)) {
            return false;
        }

        for (int i = 0 ; i < pos.getExonStarts().size() ;i ++){
            int exonStart = pos.getExonStarts().get(i);
            int exonEnd   = pos.getExonEnds().get(i);

            Range r = Range.closed(exonStart,exonEnd);

            if ( search.isConnected(r))
                return true;
        }

        return false;


    }


    public SortedSet<String> findRelatedGeneNames(String synonym) {

        long timeS = System.currentTimeMillis();

        SortedSet<String> results = new TreeSet<String>();
        for ( GeneName geneName : geneNamesMap.values()){


            if ( geneName.getApprovedSymbol().contains(synonym)) {
                results.add(geneName.getApprovedSymbol());
                continue;
            }
            if ( geneName.getApprovedName().contains(synonym)){
                results.add(geneName.getApprovedSymbol());
                continue;
            }

            if ( geneName.getSynonyms().contains(synonym)) {
                results.add(geneName.getApprovedSymbol());
                continue;
            }

            if ( geneName.getEnsemblGeneId().contains(synonym)) {
                results.add(geneName.getApprovedSymbol());
                continue;
            }

            if ( geneName.getRefseqIds().contains(synonym)) {
                results.add(geneName.getApprovedSymbol());
                continue;
            }

            if ( geneName.getAccessionNr().contains(synonym)) {
                results.add(geneName.getApprovedSymbol());
                continue;
            }

            if ( geneName.getOmimId().contains(synonym)) {
                results.add(geneName.getApprovedSymbol());
                continue;
            }

            if ( geneName.getPreviousSymbols().contains(synonym)){
                results.add(geneName.getApprovedSymbol());
                continue;
            }

        }
        long timeE = System.currentTimeMillis();

        return results;
    }


    public List<GeneName> getAllGeneNames(){
        return new ArrayList<GeneName>(geneNamesMap.values());
    }

    public GeneChromosomePosition getMostLikelyChromosomePosition(List<GeneChromosomePosition> chromPositions) {
        GeneChromosomePosition chromPos = null;
        if ( chromPositions.size() > 0) {

            chromPos = chromPositions.get(0);
        }

        if ( chromPos == null)
            return null;
        for ( GeneChromosomePosition p : chromPositions){
            if ( chromPos.getChromosome().length() > p.getChromosome().length())
                chromPos = p;
        }
        return chromPos;
    }

}
