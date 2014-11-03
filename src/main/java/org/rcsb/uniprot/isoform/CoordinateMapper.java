package org.rcsb.uniprot.isoform;

/**
 * Created by ap3 on 14/05/2014.
 */
public interface CoordinateMapper {


    public int convertPos1toPos2(int coordinate);
    public int convertPos2toPos1(int coordinate);


}
