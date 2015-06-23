package org.rcsb.uniprot.auto.dao;

import org.rcsb.uniprot.auto.Entry;
import org.rcsb.uniprot.auto.Uniprot;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import java.util.*;


public interface UniprotDAO {


    public Uniprot getUniProt(EntityManager em,String uniprotAccession);

    public  String getUniProtAcByName(String uniprotName);

    /** Get a list of UniProt accessions and their corresponding EC numbers on an protein (and not protein component) level.
     * e.g P50225 - 2.8.2.1
     *  ( for performance reasons we only look at UniProts that are linked to PDB (in the uniprotpdbmap table)
     * @return a list of 2-dimensional objects
     */
    public List<Object[]> getECNumbers();

    /** same as getECNumbers but now for components
     *
     * @return
     */
    public List<Object[]> getECNumbers4Components();


    /**Get the recommended names for components and their sequence positions

      */
    public List<Object[]> getRecommendedNames4Components();

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


    /** get the common name of an organism
     *
     * @param scientificName
     * @return
     */
    public String getCommonName(String scientificName);

    /** get the scientific organism name
     *
     * @param uniprotAc
     * @return
     */
    public List<String> getOrganism(String uniprotAc);

    public  String getUniprotName(Entry e);


    /** get a list of all mappings between PDB and uniprot. Returns chains and ranges on chains if available.
     *
     * @return
     */
    public List<Object[]> getPdbReferencesFromUniProt();

    /** get a list of all references of type dbType from uniprot
     *
     * @param dbType can be e.g. EMBL, PDB,KEGG,STRING,RefSeq. In total there are currentl more than 100 reference types in UniProt.
     *               (see them all with SELECT distinct(type_) FROM dbreferencetype )
     *
     * @return
     */
    public List<Object[]> getDbReferencesFromUniProt(String dbType) ;

    /** returns a Map<String,String> that contains a mapping from uniprot accessions to entry names.
     *
     * @param aaccessions - a list of UniProt Accessions (e.g. P00123)
     */
    public Map<String, String> getAC2NameMap(Set<String> aaccessions );

    /** get a Map of all recommended names for UniProt IDs
     *
     * @return
     */
    public Map<String,String> getRecommendedNameMap();

    /** returns a map that contains a mapping of uniprot accessions to recommended names
     *
     * @param aaccessions
     * @return
     */
    public Map<String, String> getRecommendedNameMap(Set<String> aaccessions );

    /** returns a map that contains a mapping of uniprot accessions to submitted names
     *
     * @param aaccessions
     * @return
     */
    public Map<String, String> getSubmittedNameMap(Set<String> aaccessions );


    /** get a Map of all alternative names for UniProt IDs
     *
     * @return
     */
    public Map<String,List<String>> getAlternativeNameMap();



    /** get a Map of all short names for UniProt IDs
     *
     * @return
     */
    public Map<String,List<String>> getShortNameMap();


    /** get a Map of all organisms for UniProt IDs
     *
     * @return
     */
    public Map<String,List<String>> getOrganismMap();

}