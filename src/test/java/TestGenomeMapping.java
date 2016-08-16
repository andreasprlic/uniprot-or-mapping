import com.google.common.collect.Range;
import junit.framework.TestCase;
import org.biojava.nbio.core.sequence.DNASequence;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AmbiguityDNACompoundSet;
import org.biojava.nbio.core.sequence.compound.AmbiguityRNACompoundSet;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.biojava.nbio.core.sequence.compound.NucleotideCompound;
import org.biojava.nbio.core.sequence.io.DNASequenceCreator;
import org.biojava.nbio.core.sequence.io.FastaReader;
import org.biojava.nbio.core.sequence.io.GenericFastaHeaderParser;
import org.biojava.nbio.core.sequence.template.CompoundSet;
import org.biojava.nbio.core.sequence.template.Sequence;
import org.biojava.nbio.core.sequence.transcription.Frame;
import org.biojava.nbio.core.sequence.transcription.TranscriptionEngine;
import org.biojava.nbio.core.util.InputStreamProvider;
import org.biojava.nbio.genome.parsers.genename.GeneChromosomePosition;
import org.biojava.nbio.genome.parsers.genename.GeneChromosomePositionParser;
import org.biojava.nbio.genome.parsers.twobit.TwoBitParser;
import org.biojava.nbio.genome.util.ChromosomeMappingTools;
import org.junit.Test;
import org.rcsb.uniprot.auto.Uniprot;
import org.rcsb.uniprot.auto.tools.UniProtTools;
import org.rcsb.uniprot.config.RCSBUniProtMirror;
import org.rcsb.uniprot.isoform.IsoformTools;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by andreas on 7/18/16.
 */
public class TestGenomeMapping extends TestCase {

    static String userHome = System.getProperty("user.home");

    static  String twoBitLocation = userHome + "/mmtf/hg38.2bit";

    static String geneRefLocation = userHome + "/mmtf/refFlat.txt.gz";

    @Test
    public void testP21802(){

        try {
            checkInstallFiles();
        } catch (IOException e){

            e.printStackTrace();
            fail(e.getMessage());
        }


        // a reverse strand example
        //String uniProtID = "P00568";
        //String geneName = "AK1";


        String geneName = "FGFR2";
        String uniProtID = "P21802";



        int altTranscriptCount = 0;
        try {

            System.out.println("parsing 2 bit");

            TwoBitParser parser = new TwoBitParser(new File(twoBitLocation));

            System.out.println("Parsed 2 bit file");

            InputStreamProvider prov = new InputStreamProvider();

            List<GeneChromosomePosition> gcps =  GeneChromosomePositionParser.getChromosomeMappings(prov.getInputStream(geneRefLocation));

            System.out.println("fetching uniprot file");
            File localFile = RCSBUniProtMirror.getLocalFileLocation(uniProtID);
            try {
                if ( ! localFile.exists())
                    localFile = UniProtTools.fetchFileFromUniProt(uniProtID, localFile);
            } catch (Exception e) {
                e.printStackTrace();
            }
            InputStream inStream = new FileInputStream(localFile);

            Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);
            IsoformTools it = new IsoformTools();
            ProteinSequence[] ps = it.getIsoforms(up);

            for (GeneChromosomePosition pos : gcps ){

                if ( ! pos.getGeneName().equals(geneName))
                    continue;

                altTranscriptCount++;

                System.out.println(altTranscriptCount + " " + pos);

                List<Range<Integer>> exons = ChromosomeMappingTools.getChromosomalRangesForCDS(pos);

                String cds = getCDS(parser,pos,exons);

                String fasta = ">cds\n" +cds;

                String proteinSequenceAltTranscript = translate(fasta);

                //System.out.println("alttrnscr : " + proteinSequenceAltTranscript.length() + " " + proteinSequenceAltTranscript);


                boolean found = false;
                int count = 0;
                for ( ProteinSequence p : ps){
                    count ++;
                    //System.out.println("isoform   : "+ p.getLength() + " " + p);

                    if ( p.getSequenceAsString().equals(proteinSequenceAltTranscript)){
                        System.out.println("FOUND MATCHING ISOFORM: " + count + " seq length: " + p.getLength());
                        found = true;
                    }
                }
                if ( ! found ){
                    System.out.println("NO MATCH FOUND!");
                }

            //break;
            }

        } catch (Exception e){
            e.printStackTrace();
        }

        //RCSBUniProtMirror.delete(uniProtID);
    }

    private void checkInstallFiles() throws IOException {

        File mmtf = new File(userHome +"/mmtf");
        if ( ! mmtf.exists()){
            mmtf.mkdir();
        }

        File twoBit = new File(twoBitLocation);
        if ( ! twoBit.exists()){
            download("http://cdn.rcsb.org//gene/hg38/hg38.2bit",twoBitLocation);
        }

        File refFlat = new File(geneRefLocation);
        if ( ! refFlat.exists()){
            download("http://hgdownload.cse.ucsc.edu/goldenPath/hg38/database/refFlat.txt.gz", geneRefLocation);
        }

    }

    private void download(String url, String localLocation) throws IOException {

        URL website = new URL(url);

        File localFile = new File(localLocation);

        ReadableByteChannel rbc = Channels.newChannel(website.openStream());
        FileOutputStream fos = new FileOutputStream(localFile);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);


    }


    private String getCDS( TwoBitParser parser,GeneChromosomePosition pos, List<Range<Integer>> exons) throws Exception {

        StringBuffer dna = new StringBuffer();

        String [] names = parser.getSequenceNames();

        parser.setCurrentSequence(pos.getChromosome());

        boolean first = true;
        for ( Range exon : exons){
            int start = (int)exon.lowerEndpoint();
            int end   = (int)exon.upperEndpoint();
            int length = (end-start);

            // non coding exon?
            if ( length < 1)
                continue;

            String d = parser.loadFragment(start,length);



            if ( pos.getOrientation().equals('-')){
                 DNASequence dnas = new DNASequence(d);
                 d = dnas.getReverseComplement().getSequenceAsString();

            }

            dna.append(d);


        }
        parser.close();



        return dna.toString();

    }


    private String translate(String dnaSequence) {
        try {

            // parse the raw sequence from the string
            InputStream stream = new ByteArrayInputStream(dnaSequence.getBytes());

            // define the Ambiguity Compound Sets
            AmbiguityDNACompoundSet ambiguityDNACompoundSet = AmbiguityDNACompoundSet.getDNACompoundSet();
            CompoundSet<NucleotideCompound> nucleotideCompoundSet = AmbiguityRNACompoundSet.getRNACompoundSet();

            FastaReader<DNASequence, NucleotideCompound> proxy =
                    new FastaReader<DNASequence, NucleotideCompound>(
                            stream,
                            new GenericFastaHeaderParser<DNASequence, NucleotideCompound>(),
                            new DNASequenceCreator(ambiguityDNACompoundSet));

            // has only one entry in this example, but could be easily extended to parse a FASTA file with multiple sequences
            LinkedHashMap<String, DNASequence> dnaSequences = proxy.process();

            // Initialize the Transcription Engine
            TranscriptionEngine engine = new
                    TranscriptionEngine.Builder().dnaCompounds(ambiguityDNACompoundSet).rnaCompounds(nucleotideCompoundSet).build();

            Frame[] sixFrames = Frame.getAllFrames();



            for (DNASequence dna : dnaSequences.values()) {

                Map<Frame, Sequence<AminoAcidCompound>> results = engine.multipleFrameTranslation(dna, sixFrames);

                Frame frame = sixFrames[0];
                return results.get(frame).getSequenceAsString();
            }
//                for (Frame frame : sixFrames){
//                    System.out.println("Translated Frame:" + frame +" : " + results.get(frame));
//                    //System.out.println(dna.getRNASequence(frame).getProteinSequence(engine));
//
//                    ProteinSequence ps = new ProteinSequence(results.get(frame).getSequenceAsString());
//                    System.out.println(ps);
//                    try {
//
//                    } catch (Exception e){
//                        System.err.println(e.getMessage() + " when trying to translate frame " + frame);
//                    }
//                }
//
//            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}

