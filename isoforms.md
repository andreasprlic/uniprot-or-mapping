# Mapping of UniProt isoforms to alternative transcripts on the genome

This project can map UniProt isoforms to alternative transcripts using BioJava.   

## FGFR2 - P21802

This gene/protein is a nice test case for mapping alternative transcripts to UniProt isoforms. There are 13 alternative transcripts that need to get mapped to 23 annotated isoforms in UniProt.

Below is how to do this mapping. The code assumes that there are a local copy of the human genome in a 2bit file and a refFlat.txt file, as it is available from the UCSC genome browser. They can be also downloaded from http://cdn.rcsb.org/gene/hg38/hg38.2bit and http://cdn.rcsb.org/gene/hg38/geneChromosome38.tsf.gz (contains the content of refFlat.txt for assembly 38).

For the full code for this example, please see [here](https://github.com/rcsb/uniprot-or-mapping/blob/master/src/test/java/TestGenomeMapping.java)

```java

        String geneName = "FGFR2";
        String uniProtID = "P21802";

        String twoBitLocation = "/Users/andreas/mmtf/hg38.2bit";

        String geneRefLocation = "/Users/andreas/mmtf/refFlat.txt";

        int altTranscriptCount = 0;
        try {

            System.out.println("parsing 2 bit");

            TwoBitParser parser = new TwoBitParser(new File(twoBitLocation));

            System.out.println("Parsed 2 bit file");

            List<GeneChromosomePosition> gcps =  GeneChromosomePositionParser.getChromosomeMappings(new FileInputStream(new File(geneRefLocation)));

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

                List<Range> exons = ChromosomeMappingTools.getChromosomalRangesForCDS(pos);

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

            }

        } catch (Exception e){
            e.printStackTrace();
        }

```

This code will provide the following output:

```
parsing 2 bit
Parsed 2 bit file
fetching uniprot file
1 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001320654, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121531314, cdsStart=121479856, cdsEnd=121526752, exonCount=17, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121517318, 121519978, 121526154, 121526728, 121527675, 121530321, 121531266], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121517463, 121520169, 121526194, 121526954, 121528003, 121530458, 121531314]]
NO MATCH FOUND!
2 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001144914, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121593967, cdsStart=121479856, cdsEnd=121593817, exonCount=15, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121538591, 121551289, 121564501, 121565437, 121593708], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121538715, 121551459, 121564579, 121565704, 121593967]]
FOUND MATCHING ISOFORM: 23 seq length: 709
3 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_023029, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121593967, cdsStart=121479856, cdsEnd=121593817, exonCount=16, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121517318, 121519978, 121538591, 121551289, 121564501, 121593708], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121517463, 121520169, 121538715, 121551459, 121564579, 121593967]]
NO MATCH FOUND!
4 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001144916, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121594258, cdsStart=121479856, cdsEnd=121593817, exonCount=15, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121517318, 121519978, 121538591, 121551289, 121593708], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121517463, 121520169, 121538715, 121551459, 121594258]]
NO MATCH FOUND!
5 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001144915, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121596645, cdsStart=121479580, cdsEnd=121593817, exonCount=17, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121517318, 121519978, 121538591, 121551289, 121564501, 121593708, 121596476], exonEnds=[121479670, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121517463, 121520169, 121538715, 121551459, 121564579, 121593967, 121596645]]
FOUND MATCHING ISOFORM: 21 seq length: 707
6 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_000141, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121598458, cdsStart=121479856, cdsEnd=121593817, exonCount=18, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121517318, 121519978, 121538591, 121551289, 121564501, 121565437, 121593708, 121597961], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121517463, 121520169, 121538715, 121551459, 121564579, 121565704, 121593967, 121598458]]
FOUND MATCHING ISOFORM: 1 seq length: 821
7 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001144917, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121598458, cdsStart=121479856, cdsEnd=121593817, exonCount=16, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121519978, 121538591, 121551289, 121564501, 121565437, 121593708, 121597961], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121520169, 121538715, 121551459, 121564579, 121565704, 121593967, 121598458]]
FOUND MATCHING ISOFORM: 15 seq length: 705
8 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001144918, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121598458, cdsStart=121479856, cdsEnd=121593817, exonCount=16, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515122, 121517318, 121519978, 121538591, 121551289, 121593708, 121597961], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121517463, 121520169, 121538715, 121551459, 121593967, 121598458]]
FOUND MATCHING ISOFORM: 20 seq length: 704
9 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001320658, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121598458, cdsStart=121479856, cdsEnd=121593817, exonCount=18, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515122, 121517318, 121519978, 121538591, 121551289, 121564501, 121565437, 121593708, 121597961], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121517463, 121520169, 121538715, 121551459, 121564579, 121565704, 121593967, 121598458]]
FOUND MATCHING ISOFORM: 5 seq length: 819
10 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_022970, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121598458, cdsStart=121479856, cdsEnd=121593817, exonCount=18, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121518681, 121519978, 121538591, 121551289, 121564501, 121565437, 121593708, 121597961], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121518829, 121520169, 121538715, 121551459, 121564579, 121565704, 121593967, 121598458]]
FOUND MATCHING ISOFORM: 3 seq length: 822
11 GeneChromosomePosition [geneName=FGFR2, genebankId=NR_073009, chromosome=chr10, orientation=-, transcriptionStart=121478329, transcriptionEnd=121598458, cdsStart=121598458, cdsEnd=121598458, exonCount=17, exonStarts=[121478329, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121517318, 121518681, 121519978, 121538591, 121551289, 121593708, 121597961], exonEnds=[121480021, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121517463, 121518829, 121520169, 121538715, 121551459, 121593967, 121598458]]
NO MATCH FOUND!
12 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001144913, chromosome=chr10, orientation=-, transcriptionStart=121481852, transcriptionEnd=121593967, cdsStart=121482171, cdsEnd=121593817, exonCount=17, exonStarts=[121481852, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121518681, 121519978, 121538591, 121551289, 121564501, 121565437, 121593708], exonEnds=[121482177, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121518829, 121520169, 121538715, 121551459, 121564579, 121565704, 121593967]]
FOUND MATCHING ISOFORM: 17 seq length: 769
13 GeneChromosomePosition [geneName=FGFR2, genebankId=NM_001144919, chromosome=chr10, orientation=-, transcriptionStart=121481852, transcriptionEnd=121598458, cdsStart=121482171, cdsEnd=121593817, exonCount=17, exonStarts=[121481852, 121483697, 121485394, 121487353, 121487990, 121496531, 121498494, 121500825, 121503789, 121515116, 121518681, 121519978, 121538591, 121551289, 121564501, 121593708, 121597961], exonEnds=[121482177, 121483803, 121485532, 121487424, 121488113, 121496722, 121498605, 121500947, 121503941, 121515319, 121518829, 121520169, 121538715, 121551459, 121564579, 121593967, 121598458]]
FOUND MATCHING ISOFORM: 22 seq length: 680

```
