package org.rcsb.uniprot.isoform;


import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.SimpleGapPenalty;
import org.biojava.nbio.alignment.SimpleSubstitutionMatrix;
import org.biojava.nbio.alignment.SubstitutionMatrixHelper;
import org.biojava.nbio.alignment.template.*;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.rcsb.uniprot.auto.Uniprot;import java.lang.Override;import java.lang.System;


/** Maps coordinates between the main UniProt sequence and one of its isoforms
 *
 *
 * Created by ap3 on 14/05/2014.
 *
 *
 */
public class IsoformMapper implements CoordinateMapper {


    SequencePair<ProteinSequence, AminoAcidCompound> pair ;
    PairwiseSequenceAligner<ProteinSequence, AminoAcidCompound> aligner;

    /** Maps coordinates between the main UniProt sequence and one of its isoforms
     *
     * @param uniprot Uniprot entry
     * @param mainSeq main UP sequence
     * @param other isoform sequence
     */
    public IsoformMapper(Uniprot uniprot, ProteinSequence mainSeq, ProteinSequence other){

        // align them

        if ( mainSeq.getLength() + other.getLength() > 5000) {
            System.err.println("Warning, pairwise alignment of two long sequences! Make sure there is enough RAM! " + mainSeq.getAccession() + " " + other.getAccession());

        }

        SubstitutionMatrix<AminoAcidCompound> matrix = SubstitutionMatrixHelper.getBlosum62();

         aligner = Alignments.getPairwiseAligner(mainSeq,other,
                Alignments.PairwiseSequenceAlignerType.LOCAL, new SimpleGapPenalty(), matrix);

        pair = aligner.getPair();


    }



    @Override
    public int convertPos1toPos2(int coordinate) {

        AlignedSequence s1 = pair.getQuery();
        AlignedSequence s2 = pair.getTarget();

        int aligPos = s1.getAlignmentIndexAt(coordinate);
        return s2.getSequenceIndexAt(aligPos);


//
//        int index = pair.getIndexInQueryAt(coordinate);
//        int targetIndex = -1;
//        try {
//            targetIndex= pair.getIndexInTargetAt(index);
//        } catch (Exception e){
//            e.printStackTrace();
//            System.err.println(pair.toString(60));
//            System.err.println(pair.getLength());
//            System.err.println(coordinate + " ???");
//            AlignedSequence s1 = pair.getQuery();
//            //System.err.println(s1);
//            int aligIndex = s1.getAlignmentIndexAt(coordinate);
//            System.err.println(aligIndex);
//
//            System.err.println(s2.getSequenceIndexAt(aligIndex));
//
//        }
//        return targetIndex;
    }

    @Override
    public int convertPos2toPos1(int coordinate) {
//        int index = pair.getIndexInTargetAt(coordinate);
//        int queryindex = -1;
//        try {
//            queryindex= pair.getIndexInQueryAt(index);
//        } catch (Exception e){
//            e.printStackTrace();
//            System.err.println(pair.toString(60));
//        }
//        return queryindex;

        AlignedSequence s1 = pair.getQuery();
        AlignedSequence s2 = pair.getTarget();

        int aligPos = s2.getAlignmentIndexAt(coordinate);
        return s1.getSequenceIndexAt(aligPos);
    }

    public  SequencePair<ProteinSequence, AminoAcidCompound> getPair(){
        return pair;
    }

    public  void destroy(){
        AbstractPairwiseSequenceAligner aps = (AbstractPairwiseSequenceAligner)aligner;
        aps.setQuery(null);
        aps.setTarget(null);
        aps.setGapPenalty(null);
        aps.setSubstitutionMatrix(null);

        pair = null;
    }

}
