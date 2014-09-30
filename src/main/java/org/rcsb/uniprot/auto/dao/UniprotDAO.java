package org.rcsb.uniprot.auto.dao;

import org.rcsb.uniprot.auto.Entry;
import org.rcsb.uniprot.auto.Uniprot;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;


public interface UniprotDAO {


    public Uniprot getUniProt(String uniprotAccession);
    public Uniprot getUniProt(String uniprotID, EntityManager em);

    public  String getUniProtAcByName(String uniprotName);

    public List<String> getAllUniProtIDs();

    public List<String> getGeneNames(String uniprotID);

    public String getDescription(String uniprotID);

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
     * @see(org.rcsb.uniprot.auto.tools.UniProtTools.loadVersionsFromUniProt())
     *
     * @return
     */
    public SortedMap<String, Integer> getDbVersions();

    /** returns all the uniprot IDs that are in the Moped database
     *
     * @return list of Uniprot IDs
     */
    public SortedSet<String> getMOPEDids();

    /** ALLOWS THE PDB webapp to register which UniProt IDs are required. Allows the UniProt framework to also load some Trembl entries, without having to load all of Trembl!
     *
     * @param requiredUniProtIDS
     */
    public void registerRequiredUniProtIds(SortedSet<String> requiredUniProtIDS);

    public SortedSet<String> getRequiredUniProtIds();
}