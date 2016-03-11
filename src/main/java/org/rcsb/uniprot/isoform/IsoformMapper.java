package org.rcsb.uniprot.isoform;


import org.biojava.nbio.alignment.Alignments;
import org.biojava.nbio.alignment.SimpleGapPenalty;

import org.biojava.nbio.alignment.template.*;
import org.biojava.nbio.core.alignment.matrices.SubstitutionMatrixHelper;
import org.biojava.nbio.core.alignment.template.AlignedSequence;
import org.biojava.nbio.core.alignment.template.SequencePair;
import org.biojava.nbio.core.alignment.template.SubstitutionMatrix;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompound;
import org.rcsb.uniprot.auto.Uniprot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Override;import java.lang.System;


/** Maps coordinates between the main UniProt sequence and one of its isoforms
 *
 *
 * Created by ap3 on 14/05/2014.
 *
 *
 */
public class IsoformMapper implements CoordinateMapper {

    private static final Logger logger = LoggerFactory.getLogger(IsoformMapper.class);

    private SequencePair<ProteinSequence, AminoAcidCompound> pair ;
    private PairwiseSequenceAligner<ProteinSequence, AminoAcidCompound> aligner;

    /** Maps coordinates between the main UniProt sequence and one of its isoforms
     *
     * @param uniprot Uniprot entry
     * @param mainSeq main UP sequence
     * @param other isoform sequence
     */
    public IsoformMapper(Uniprot uniprot, ProteinSequence mainSeq, ProteinSequence other){

        // align them

        if ( mainSeq.getLength() + other.getLength() > 5000) {
            logger.warn("Pairwise alignment of two long sequences! Make sure there is enough RAM! " + mainSeq.getAccession() + " " + other.getAccession());

        }

        SubstitutionMatrix<AminoAcidCompound> matrix = SubstitutionMatrixHelper.getBlosum62();

         aligner = Alignments.getPairwiseAligner(mainSeq,other,
                Alignments.PairwiseSequenceAlignerType.LOCAL, new SimpleGapPenalty(), matrix);

        pair = aligner.getPair();


    }


    /** returns the alignment length
     *
     * @return length of the alignment
     */
    public int getAlignmentLength(){

        return pair.getLength();

    }



    @Override
    public int convertPos1toPos2(int coordinate) {

        AlignedSequence s1 = pair.getQuery();
        AlignedSequence s2 = pair.getTarget();

        try {
            int aligPos = s1.getAlignmentIndexAt(coordinate);
            return s2.getSequenceIndexAt(aligPos);
        } catch (ArrayIndexOutOfBoundsException ex){
            // the user has requested a position which is not part of the alignment
            return -1;
        }


    }

    @Override
    public int convertPos2toPos1(int coordinate) {


        AlignedSequence s1 = pair.getQuery();
        AlignedSequence s2 = pair.getTarget();

        try {
            int aligPos = s2.getAlignmentIndexAt(coordinate);

            return s1.getSequenceIndexAt(aligPos);
        } catch (ArrayIndexOutOfBoundsException ex){
            // the user has requested a position which is not part of the alignment
            return -1;
        }
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
