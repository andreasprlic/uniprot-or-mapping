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


    public Uniprot getUniProt(EntityManager em,String uniprotAccession);

    public  String getUniProtAcByName(String uniprotName);

    /** Get a list of UniProt accessions and their corresponding EC numbers
     * e.g P50225 - 2.8.2.1
     *  ( for performance reasons we only look at UniProts that are linked to PDB (in the uniprotpdbmap table)
     * @return a list of 2-dimensional objects
     */
    public List<Object[]> getECNumbers();

    public List<String> getAllUniProtIDs();

    public List<String> getGeneNames(String uniprotID);

    public String getDescription(String uniprotID);

    public int getLength(String uniprotID);

    public  String checkForPrimaryAccession(String uniprotAccession);

    public  String getUniProtHeader(EntityManager em, String uniprotAccession) ;

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

    /** get a list of all gene names in UniProt (for all organisms)
     *
     * @return list of gene names as string
     */
    public SortedSet<String> getAllGeneNames();


    /** Get uniprot accesion codes that match a specific gene name
     *
     * @param gn
     * @return
     */
    public List<String> getUniProtACsByGeneName(String gn);

    /** Clear all PDB to uniprot mappings
     *
     */
    public void clearPdbUniProtMapping();

    /** add a new mapping to the the database
     *
     * @param accession
     * @param pdbIds
     */
    public void addToPdbUniProtMapping(EntityManager em, String accession, SortedSet<String> pdbIds);

    /** get all PDB IDs that are mapped to a specific uniprot ID
     *
     * @param accession
     * @return
     */
    public SortedSet<String> getPdbForUniProt(String accession);

    /** get all Uniprot IDs that are mapped to a PDB
     *
     * @param pdbId
     * @return
     */
    public SortedSet<String> getUniProtForPDB(String pdbId);

    /** is the table in the database empty, or does it contain mappings?
     *
     * @return
     */
    public boolean hasPdbUniProtMapping();



}