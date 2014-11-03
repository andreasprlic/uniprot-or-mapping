package org.rcsb.uniprot.isoform;

import java.lang.System;import java.util.Arrays;

/**
 * Created by ap3 on 07/05/2014.
 * <p/>
 * A class that tracks index offset when re-constructing UniProt Isoforms.
 *
 * //TODO: move to UniProtDAO project
 */
public class IndexOffset {


    private static final boolean debug = false;
    short[] offset;

    public IndexOffset(int length) {
        offset = getOffsetArray(length);
    }

    public short[] getOffsetArray(int length) {

        short[] data = new short[length];

        for (int i = 0; i < length; i++) {
            data[i] = 0;
        }
        return data;

    }

    public int getOffset(int position) {
        if (debug)
            System.out.println("            O: get " + position + " " + offset[position] + " : " + (position + offset[position]));

        return position + offset[position];
    }

    public void setOffset(int start, int end, short modifier) {
        if (debug)
            System.out.println("   O: setting offset " + start + " - " + end + ": " + modifier);

        //for ( int i = start ; i < end && i < data.length; i++){
        for (int i = start; i < end; i++) {
            offset[i] += modifier;
        }
    }


    /**
     * make the offset array longer...
     *
     * @param length
     */
    public void adjustOffset(int length) {
        if (debug)
            System.out.println("            O: adjusting offset array size from " + offset.length + " to new length:" + length + " (+" + (length - offset.length) + ")");


        int pos = offset.length;
        int diff = length - pos;

        if (offset.length < length) {

            offset = Arrays.copyOf(offset, length);

            for (int i = pos; i < length; i++) {
                //offset[i] = (short) ((i - pos+1));
                offset[i] = 0;
                // System.err.println("     I:" + i + " " + offset[i]);
            }
        }


    }


    public int getLength() {
        return offset.length;
    }
}
