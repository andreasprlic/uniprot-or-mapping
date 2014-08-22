package org.biojava3.auto.dao;

import org.biojava3.auto.uniprot.Entry;
import org.biojava3.auto.uniprot.Uniprot;
import org.hibernate.Session;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;


public interface UniprotDAO {


    public Uniprot getUniProt(String uniprotAccession);

    public  String getUniProtAcByName(String uniprotName);

    public List<String> getAllUniProtIDs();

    public List<String> getGeneNames(String uniprotID);

    public String getDescription(String uniprotID);

    public Map<String,Integer> getAllPDBCounts();

    public int getLength(String uniprotID);

    public  String checkForPrimaryAccession(String uniprotAccession);

    public  String getUniProtHeader(String uniprotAccession) ;

    /** Remove all whitespace and line breaks from a sequence string (as is in the UniProt file
     *
     * @param sequence
     * @return a simplified sequence string
     */
    public  String cleanSequence(String sequence);

    /** Load all versions of the UniProt entries from the database. Can be compared with
     * @see(org.biojava3.auto.tools.UniProtTools.loadVersionsFromUniProt())
     *
     * @return
     */
    public SortedMap<String, Integer> getDbVersions();
}