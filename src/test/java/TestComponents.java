import junit.framework.TestCase;
import org.rcsb.uniprot.auto.Component;
import org.rcsb.uniprot.auto.Uniprot;
import org.rcsb.uniprot.auto.dao.UniprotDAO;
import org.rcsb.uniprot.auto.dao.UniprotDAOImpl;
import org.rcsb.uniprot.auto.tools.JpaUtilsUniProt;

import javax.persistence.EntityManager;

/**
 * Created by ap3 on 24/11/2014.
 */
public class TestComponents extends TestCase{

    public void testQ8V5E0(){
        String accession = "Q8V5E0";

        UniprotDAO dao = new UniprotDAOImpl();

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        Uniprot up = dao.getUniProt(em,accession);

        for ( Component c: up.getEntry().get(0).getProtein().getComponent()){
            System.out.println(c.getRecommendedName().getFullName().getValue());


        }
        em.close();
    }

    public void testP50225(){
        String accession = "P50225";

        UniprotDAO dao = new UniprotDAOImpl();

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        Uniprot up = dao.getUniProt(em,accession);

        for ( Component c: up.getEntry().get(0).getProtein().getComponent()){
            System.out.println(c.getRecommendedName().getFullName().getValue());


        }
        em.close();
    }
}
