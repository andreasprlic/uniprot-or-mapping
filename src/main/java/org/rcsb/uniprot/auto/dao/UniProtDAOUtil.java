package org.rcsb.uniprot.auto.dao;

import org.apache.commons.lang.StringUtils;
import org.rcsb.uniprot.auto.tools.JpaUtilsUniProt;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.util.*;

/**
 * Created by ap3 on 14/07/2016.
 */
public class UniProtDAOUtil {

    // this provides access to the recommended short name:

    public  static Map<String, String> getRecommendedNameMap(Set<String> accessions ) {
        StringBuffer sb = new StringBuffer("select a.hjvalue, est.value_ ");
        sb.append(" from entry_accession as a ");
        sb.append(" join entry as en on a.hjid = en.hjid ");
        sb.append(" join protein_type as p on en.protein_entry_hjid = p.hjid ");
        sb.append(" join recommended_name as r on p.recommended_name_protein_typ_0 = r.hjid ");
        sb.append(" join evidenced_string_type est on r.full_name_recommended_name_h_0 = est.hjid ");
        if (accessions != null && accessions.size() > 0) {
            sb.append(" where a.hjvalue in ( '" + StringUtils.join(accessions, "','") + "') ");
        }
        sb.append(" group by a.hjvalue, est.value_ ");

        String sql = sb.toString();

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        Query q = em.createNativeQuery(sql);
        List<Object[]> data = q.getResultList();

        Map<String, String> results = new HashMap<String, String>();
        for (Object[] d : data) {
            String ac = d[0].toString();
            String recname = d[1].toString();
            results.put(ac, recname);
        }
        em.close();
        return results;
    }



    public static Map<String,List<String>> getAlternativeNameMap(Set<String> accessions){

        StringBuffer sb = new StringBuffer("select a.hjvalue, est.value_ ");
        sb.append(" from entry_accession as a ");
        sb.append(" join entry as en on en.HJID = a.HJID ");
        sb.append(" join alternative_name an on an.ALTERNATIVE_NAME_PROTEIN_TYP_0 = en.PROTEIN_ENTRY_HJID ");
        sb.append(" join evidenced_string_type as est on an.FULL_NAME_ALTERNATIVE_NAME_H_0 = est.HJID  ");
        if(accessions != null && accessions.size() > 0) {
            sb.append("where a.hjvalue in ( '" + StringUtils.join(accessions, "','")+ "') ");
        }

        String sql = sb.toString();

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        List<Object[]> data = (List<Object[]>) em.createNativeQuery(sql).getResultList();

        Map<String,List<String>> results = new TreeMap<String,List<String>>();

        for ( Object[] d : data){
            String ac = d[0].toString();
            String altname = d[1].toString();
            List<String> alternatives = results.get(ac);
            if ( alternatives == null){
                alternatives = new ArrayList<>();
                results.put(ac,alternatives);
            }
            if ( ! alternatives.contains(altname)) {
                alternatives.add(altname);
            }
        }
        em.close();
        return results;

    }

    /** returns a map that contains a mapping of uniprot accessions to submitted names
     *
     * @param accessions
     * @return
     */
    public static Map<String, String> getSubmittedNameMap(Set<String> accessions ){

        StringBuffer sb = new StringBuffer("select a.hjvalue, est.value_  ");
        sb.append(" from entry_accession as a ");
        sb.append(" join entry as en on a.hjid = en.hjid ");
        sb.append(" join protein_type as p on en.protein_entry_hjid = p.hjid ");
        sb.append(" join submitted_name as s on p.HJID = s.SUBMITTED_NAME_PROTEIN_TYPE__0 ");
        sb.append(" join evidenced_string_type est on s.FULL_NAME_SUBMITTED_NAME_HJID = est.HJID  ");
        if(accessions != null && accessions.size() > 0) {
            sb.append(" where a.hjvalue in ( '" + StringUtils.join(accessions, "','")+ "') ");
        }
        sb.append(" group by a.hjvalue, est.value_ having count(*) > 0");

        String sql = sb.toString();

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        Query q = em.createNativeQuery(sql);
        List<Object[]> data = q.getResultList();

        Map<String,String> results = new HashMap<String,String>();
        for ( Object[] d : data){
            String ac = d[0].toString();
            String recname = d[1].toString();
            results.put(ac,recname);
        }
        em.close();
        return results;

    }
}
