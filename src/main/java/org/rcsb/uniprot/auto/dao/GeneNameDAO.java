package org.rcsb.uniprot.auto.dao;

import org.biojava.nbio.genome.parsers.genename.GeneChromosomePosition;
import org.biojava.nbio.genome.parsers.genename.GeneName;

import java.util.List;
import java.util.SortedSet;

/**
 * Created by ap3 on 03/02/2016.
 */
public interface GeneNameDAO {


    /** Test if a gene ID is an approved gene name by the HUGO Gene Nomenclature Committee (HGNC)
     *
     * @param geneId
     * @return
     */
    public boolean isApprovedSymbol(String geneId);

    /** Get the GeneName record based on the approvedGeneName
     *
     * @param approvedGeneName - approved gene symbol by HGNC
     * @return
     */
    public GeneName getGeneName(String approvedGeneName);

    /** Get the chromosome position for an approved gene name
     *
     * @param approvedGeneName  - approved gene symbol by HGNC
     * @return
     */
    public List<GeneChromosomePosition> getChromosomePosition(String approvedGeneName);

    public List<GeneChromosomePosition>  getChromosomePosition(String approvedGeneName, String assemblyVersion);

    /** Return all uniprot IDs that have links to (human) gene
     *
     * @return
     */
    public SortedSet<String> getUniprotIdsWithGeneName();

    /** Returns a list of all approved Gene Names
     *
     * @return
     */
    public SortedSet<String> getAllApprovedGeneSymbol();


    /** Returns all available human Gene Names
     *
     * @return
     */
    public List<GeneName> getAllGeneNames();

    /** find (approved) gene names by synonym or by previous name
     *
     * @param query
     * @return
     */
    public SortedSet<String> findRelatedGeneNames(String query);

    public SortedSet<String> getGeneNamesForUniprotId(String uniprotId);

    public List<GeneChromosomePosition> getChromosomePositionsInRange(String chromosome, int start, int end);

    public List<GeneChromosomePosition> getChromosomePositionsInRange(String chromosome, int start, int end, String assemblyVersion);

    public GeneChromosomePosition getMostLikelyChromosomePosition(List<GeneChromosomePosition> positions);

    public boolean inExon(GeneChromosomePosition pos , String chromosome, int chromosomePos,String assemblyVersion);


}