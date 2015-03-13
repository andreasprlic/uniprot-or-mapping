package org.rcsb.uniprot.isoform;

import org.biojava.nbio.core.exceptions.CompoundNotFoundException;
import org.biojava.nbio.core.sequence.AccessionID;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.biojava.nbio.core.sequence.compound.AminoAcidCompoundSet;

import org.rcsb.uniprot.auto.*;
import org.rcsb.uniprot.auto.CommentType;
import org.rcsb.uniprot.auto.FeatureType;
import org.rcsb.uniprot.auto.IsoformType;
import org.rcsb.uniprot.auto.LocationType;
import org.rcsb.uniprot.auto.PositionType;
import org.rcsb.uniprot.auto.Uniprot;
import org.rcsb.uniprot.auto.dao.UniprotDAO;
import org.rcsb.uniprot.auto.dao.UniprotDAOImpl;
import org.rcsb.uniprot.auto.tools.JpaUtilsUniProt;
import org.rcsb.uniprot.auto.tools.UniProtTools;


import javax.persistence.EntityManager;
import java.lang.RuntimeException;import java.lang.String;import java.lang.System;import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ap3 on 17/04/2014.
 */
public class IsoformTools {

    // good test cases:
    // Q5JS13 - complicated isoforms

    private static final boolean debug = false;


    /* isoform nr starts at 1 first canonical isoform nr.

     */

    public static void main(String[] args){

        String up = "Q9BZ29";

        EntityManager em = JpaUtilsUniProt.getEntityManager();

        UniprotDAO dao = new UniprotDAOImpl();

        Uniprot uniprot =dao.getUniProt(em, up);

        IsoformTools me = new IsoformTools();

        IsoformType isot = me.getIsoformType(uniprot,5);

        System.out.println(isot.getName().get(0).getValue());
        System.out.println(isot.getNote().getValue());


    }

    public IsoformType getIsoformType(Uniprot up,int isoformNr) {
        if (up.getEntry().size() < 1) {
            System.err.println("UP entry does not contain an entry!! ");
            return null;
        }

        //System.out.println("     loading isoforms for " + up.getEntry().get(0).getAccession());

        ProteinSequence[] data = null;
        synchronized (up) {
            List<FeatureType> features = up.getEntry().get(0).getFeature();
            List<CommentType> comments = up.getEntry().get(0).getComment();


            for (CommentType c : comments) {

                // check isoform
                List<IsoformType> isoforms = c.getIsoform();
                if (isoforms != null && isoforms.size() > 0) {
                    return isoforms.get(isoformNr - 1);
                }
            }
        }
        return null;
    }

    public  synchronized ProteinSequence[] getIsoforms(Uniprot up) throws CompoundNotFoundException {

        if (up.getEntry().size() < 1) {
            System.err.println("UP entry does not contain an entry!! ");
            return null;
        }

        //System.out.println("     loading isoforms for " + up.getEntry().get(0).getAccession());

        ProteinSequence[] data = null;
        synchronized (up) {
            List<FeatureType> features = up.getEntry().get(0).getFeature();
            List<CommentType> comments = up.getEntry().get(0).getComment();



            ProteinSequence originalSequence = new ProteinSequence(UniProtTools.cleanSequence(up.getEntry().get(0).getSequence().getValue()), AminoAcidCompoundSet.getAminoAcidCompoundSet());

            for (CommentType c : comments) {

                // check isoform
                List<IsoformType> isoforms = c.getIsoform();
                if (isoforms != null && isoforms.size() > 0) {

                    if (debug)
                        System.out.println("\tFound isoforms: " + isoforms.size());

                    data = new ProteinSequence[isoforms.size()];

                    int isopos = 0;

                    for (IsoformType iso : isoforms) {
                        if (debug)
                            System.out.println();

                        String seqType = iso.getSequence().getType();

                        if (debug)
                            System.out.println("###\t" + (isopos + 1) + " " + iso.getName().get(0).getValue() + " " + iso.getId().get(0) + " "
                                    + " " + seqType +
                                    " " + iso.getSequence().getRef());

                        if (seqType.equals("not described")) {
                            // we actually don't have information to describe this isoform!

                            data[isopos] = null;
                            isopos++;
                            continue;
                        }

                        AccessionID id = new AccessionID(iso.getId().get(0));
                        String refs = iso.getSequence().getRef();

                        if (seqType.equals("displayed")) {
                            //This isoform has been chosen as the 'canonical' sequence. All positional information in this entry refers to it. This is also the sequence that appears in the downloadable versions of the entry.
                            data[isopos] = originalSequence;
                            data[isopos].setAccession(id);

                        } else {
                            // seqtype: described
                            // we need to infer the isoform. That's a painful business
                            ProteinSequence ps = buildIsoform(originalSequence, id, features, iso.getSequence().getRef());
                            if (debug)
                                UniProtTools.prettyPrint(ps);
                            //ProteinSequence ps = new ProteinSequence("A", AminoAcidCompoundSet.getAminoAcidCompoundSet());
                            ps.setAccession(id);
                            data[isopos] = ps;
                        }
                        isopos++;

                    }
                }


            }
        }

        if (data == null) {

            // looks like no isoform.. .however let's check if there is a sequence, without isoform..


            ProteinSequence ps = new ProteinSequence(UniProtTools.cleanSequence(up.getEntry().get(0).getSequence().getValue()), AminoAcidCompoundSet.getAminoAcidCompoundSet());
            data = new ProteinSequence[1];
            data[0] = ps;
            return data;

        }

        return data;
    }


    public  ProteinSequence buildIsoform(ProteinSequence orig, AccessionID id, List<FeatureType> features, String ref) throws CompoundNotFoundException {

        String[] refs;

        if (ref != null)
            refs = ref.split(" ");
        else refs = new String[0];
        if (debug)
            System.out.println("### " + id + " Got refs: " + Arrays.asList(refs));

        IndexOffset offset = new IndexOffset(orig.getLength());


        ProteinSequence isoform = new ProteinSequence(orig.getSequenceAsString(), AminoAcidCompoundSet.getAminoAcidCompoundSet());
        isoform.setAccession(id);

        for (FeatureType f : features) {

            if (!f.getType().equals("splice variant"))
                continue;

            boolean found = false;
            if (refs.length == 0)
                found = true;
            for (String r : refs) {
                if (f.getId().equals(r))
                    found = true;
            }
            if (!found)
                continue;
            if (debug)
                System.out.println(" Variant:  " + f.getId() + " " + f.getDescription());

            // let's find the correct feature for this ref.
            // System.out.println("\tVariant:" + isoformpos + " " + f.getId() + " " + f.getType() + " " + f.getDescription());

            LocationType loc = f.getLocation();

            if (loc == null)
                continue;

            List<String> variations = f.getVariation();
            String original = f.getOriginal();

            if (debug) {
                if (variations.size() > 0)
                    System.out.println("\tV: variations: " + variations);
                else
                    System.out.println("\tV: deletion");
            }
            PositionType begin = loc.getBegin();
            PositionType end = loc.getEnd();
            PositionType pos = loc.getPosition();

            if (pos == null) {

                // a range
                if (debug)
                    System.out.println("\t in range:" + begin.getPosition().intValue() + " - " + end.getPosition().intValue());

                if (begin != null && end != null) {
                    // this range needs to be replaced with new sequence:
                    //System.out.println("\t" + begin.getPosition().intValue() + " - " + end.getPosition().intValue());

                    if (variations == null || variations.size() == 0) {

                        // this means a deletion
                        int lengthBefore = isoform.getLength();
                        BigInteger diff = end.getPosition().subtract(begin.getPosition());
                        int lengthcheck = lengthBefore - diff.intValue() - 1;

                        isoform = delete(isoform, begin, end, offset);

                        if (isoform.getLength() != lengthcheck) {
                            if (debug) {

                                UniProtTools.prettyPrint(isoform);
                            }
                            System.err.println("Did not delete the right sequence!");
                            System.err.println("should be of length " + (lengthcheck) + " but got: " + isoform.getLength());
                            //System.exit(-1);
                            throw new RuntimeException("error when recreating isoform  (delete region in variant) from UniProt for " + id);
                        }

                    } else {

                        String seqvariation = variations.get(0);

                        int lengthBefore = isoform.getLength();
                        int lengthcheck = lengthBefore + seqvariation.length() - original.length();

                        isoform = replace(isoform, begin, end, seqvariation, offset, original);

                        int lengthAfter = isoform.getLength();
                        if (lengthAfter != lengthcheck) {
                            System.err.println("Did not replace the correct sequence!");
                            System.err.println("should be of length " + lengthcheck + " but got " + lengthAfter);
                            //System.exit(-1);
                            throw new RuntimeException("Error while recreating sequence isoform (replacement of region) from UniProt " + id);
                        }
                    }

                }

            } else {

                if (debug)
                    System.out.println("\t Variation at a position:" + variations);

                if (variations.size() > 1) {
                    throw new RuntimeException("More than one variation found! " + id);
                    // System.exit(0);
                }

                if ((original == null || original.length() == 0) && variations.size() == 1)

                    isoform = insert(isoform, variations.get(0), pos, offset);

                else if (variations.size() == 1) {

                    String seqvariation = variations.get(0);

                    PositionType tmp = new PositionType();
                    if (original.length() == 1)
                        tmp.setPosition(pos.getPosition().add(BigInteger.ONE));
                    else
                        tmp.setPosition(pos.getPosition());
                    isoform = replace(isoform, pos, pos, seqvariation, offset, original);

                } else {
                    // delete a point mutation
                    isoform = delete(isoform, pos, pos, offset);
                }


            }

        }
        return isoform;
    }

    private  ProteinSequence replace(ProteinSequence isoform,
                                           PositionType begin,
                                           PositionType end,
                                           String seqvariation,
                                           IndexOffset offset,
                                           String original) throws CompoundNotFoundException {

        // the region that is in the location has actually been removed

        int beginOffset = offset.getOffset(begin.getPosition().intValue() - 1);
        int endOffset = offset.getOffset(end.getPosition().intValue() - 1);


//        if ( endOffset > isoform.getLength())
//            endOffset = isoform.getLength()-1;
        if (debug)
            System.out.println("   R : " + beginOffset + " " + endOffset + " (orig: " + begin.getPosition() + " - " + end.getPosition() + ")");

        String s0 = "";


        if (begin.getPosition().intValue() > 1)
            s0 = isoform.getSubSequence(1, beginOffset).getSequenceAsString();

        String s1 = seqvariation;

        String s2 = "";


        if (isoform.getLength() - endOffset > 0)
            s2 = isoform.getSubSequence(endOffset + 2, isoform.getLength()).getSequenceAsString();
        if (debug)
            System.out.println("   R : s0.length " + s0.length() +
                    " s1.length:" + s1.length() +
                    " s2.length:" + s2.length() +
                    " diff:" + (endOffset - beginOffset) +
                    " l:" + isoform.getLength());

        int modifier = 0;
        if (endOffset == beginOffset)
            modifier++;


        String old = isoform.getSubSequence(beginOffset + 1, endOffset + 1).getSequenceAsString();

        short insertDiff = (short) (s1.length() - old.length());
        if (debug) {
            System.out.println("   R : s0 (" + s0.length() + ") " + s0);
            System.out.println("   R : old(" + old.length() + ") " + old);
            System.out.println("   R : s1 (" + s1.length() + ") " + s1);
            System.out.println("   R : s2 (" + s2.length() + ") " + s2);
            System.out.println("   R : diff(" + insertDiff + ")");
        }
        if (!original.equals(old)) {
            System.err.println("The extracted sequence does not match the info in UniProt! ");
            System.err.println("original should be:" + original);
            System.err.println("but it was        :" + old);

            if (original.length() > 4)
                System.err.println(" seems to start at: " + old.indexOf(original.substring(0, 4)));

            throw new RuntimeException("Error when recreating isoform (replacement of variant) from UniProt for " + isoform.getAccession().getID());
        }

        ProteinSequence modifiedSequence = new ProteinSequence(s0 + s1 + s2, AminoAcidCompoundSet.getAminoAcidCompoundSet());

        AccessionID id = isoform.getAccession();

        modifiedSequence.setAccession(id);
        if (debug)
            System.out.println("   R : final length " + modifiedSequence.getLength());

        short l = (short) (end.getPosition().intValue() - begin.getPosition().intValue());
        short diff = (short) (s1.length() - l - 1);

        if (s2.length() > s1.length()) {
            if (debug)
                System.out.println("WARNING: insertion longer than original!" + (s2.length() - s1.length()) + " longer");
            // offset.setOffset(endOffset,offset.getLength(),(short)(s2.length()-s1.length()));
        }
        int oldOffsetLength = offset.getLength();
        if (modifiedSequence.getLength() > offset.getLength()) {
            if (debug)
                System.out.println("Offset has become too short!, need to extend");
            offset.adjustOffset(modifiedSequence.getLength());
            //offset.setOffset(endOffset,offset.getLength(),(short)(insertDiff));
        }
        //setOffset(begin.getPosition().intValue()-1, end.getPosition().intValue(),offset,diff);

        offset.setOffset(end.getPosition().intValue(), oldOffsetLength, diff);

//        int beginOffsetEnd =  offset.getOffset(begin.getPosition().intValue()-1);
//        int endOffsetEnd   =  offset.getOffset( end.getPosition().intValue()-1);
//        System.out.println("   R : " + beginOffsetEnd + " " + endOffsetEnd + " (orig: " + begin.getPosition() + " - " + end.getPosition() + ")");


        return modifiedSequence;

    }


    private  ProteinSequence insert(ProteinSequence isoform, String insert, PositionType pos, IndexOffset offset) throws CompoundNotFoundException {


        int offsetPos = offset.getOffset(pos.getPosition().intValue() - 1);
        if (debug)
            System.out.println("   I :got offset: " + offsetPos + " for " + pos.getPosition().intValue() + " " + offset.getOffset(pos.getPosition().intValue() - 1));
        //looks like an insertion at a position
        String s0 = isoform.getSubSequence(1, offsetPos).getSequenceAsString();
        if (debug)
            System.out.println("   D : s0 (" + s0.length() + "): " + s0);

        String s_insert = insert;
        if (debug)
            System.out.println("   D : s_insert (" + s_insert.length() + ") :" + s_insert);
        String s1 = "";
        if (pos.getPosition().intValue() < isoform.getLength())
            s1 = (isoform.getSubSequence(offsetPos + 1, isoform.getLength()).getSequenceAsString());
        if (debug)
            System.out.println("   D : s1 (" + s1.length() + ") :" + s1);
        //System.out.println(s);
        ProteinSequence modified = new ProteinSequence(s0 + s_insert + s1, AminoAcidCompoundSet.getAminoAcidCompoundSet());
        modified.setAccession(isoform.getAccession());

        offset.setOffset(pos.getPosition().intValue(), offset.getLength(), (short) insert.length());

        return modified;


    }

    private  ProteinSequence delete(ProteinSequence isoform, PositionType begin, PositionType end, IndexOffset offset) throws CompoundNotFoundException {

        // the region that is in the location has actually been removed

        int beginOffset = offset.getOffset(begin.getPosition().intValue() - 1);

        int endOffset = offset.getOffset(end.getPosition().intValue() - 1);
        if (debug)
            System.out.println("   D : " + beginOffset + " " + endOffset + " (orig: " + begin.getPosition() + " - " + end.getPosition() + ")");

        String s1 = "";

        int modifier = 0;
//        if ( beginOffset +1 != begin.getPosition().intValue()) {
//            // offset more than one.
//            // add -1
//            // see Q92994-2 VSP_006398
//            modifier = -1;
//        }

        if (begin.getPosition().intValue() > 1)
            s1 = isoform.getSubSequence(1, beginOffset + modifier).getSequenceAsString();

        String s2 = "";


        int emodifier = 0;
//        if ( endOffset +1 != end.getPosition().intValue()){
//            emodifier = -1;
//        }

        // endoffset + 2 verified in Q92994-2 VSP_006396
        if ((isoform.getLength() - endOffset) > 0)
            s2 = isoform.getSubSequence(endOffset + 2 + emodifier, isoform.getLength()).getSequenceAsString();
        if (debug) {
            System.out.println("   D : s1.length " + s1.length() +
                    " s2.length:" + s2.length() +
                    " diff:" + (endOffset - beginOffset) + " l:" + isoform.getLength());

            System.out.println("   D : s1( " + s1.length() + ") " + s1);
            System.out.println("   D : s2 (" + s2.length() + ") " + s2);
        }

        ProteinSequence modifiedSequence = new ProteinSequence(s1 + s2, AminoAcidCompoundSet.getAminoAcidCompoundSet());

        AccessionID id = isoform.getAccession();

        modifiedSequence.setAccession(id);

        short l = (short) (end.getPosition().intValue() - begin.getPosition().intValue() + 1);
        //setOffset(begin.getPosition().intValue()-1, end.getPosition().intValue(),offset,del);

        short omodifier = (short) (0 - l);

        offset.setOffset(beginOffset, offset.getLength(), omodifier);
        //setOffset(end.getPosition().intValue(), isoform.getLength(),offset,modifier);
        if (debug)
            System.out.println("   D : final length " + modifiedSequence.getLength());

        return modifiedSequence;

    }

}
