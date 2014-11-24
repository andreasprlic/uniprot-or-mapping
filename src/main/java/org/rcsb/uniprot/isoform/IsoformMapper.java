package org.rcsb.uniprot.isoform;


import org.biojava3.alignment.Alignments;
import org.biojava3.alignment.SimpleGapPenalty;
import org.biojava3.alignment.SimpleSubstitutionMatrix;
import org.biojava3.alignment.SubstitutionMatrixHelper;
import org.biojava3.alignment.template.AlignedSequence;
import org.biojava3.alignment.template.SequencePair;
import org.biojava3.alignment.template.SubstitutionMatrix;
import org.biojava3.core.sequence.ProteinSequence;
import org.biojava3.core.sequence.compound.AminoAcidCompound;
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
        pair = Alignments.getPairwiseAlignment(mainSeq,other,
                Alignments.PairwiseSequenceAlignerType.LOCAL, new SimpleGapPenalty(), matrix);


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
}
