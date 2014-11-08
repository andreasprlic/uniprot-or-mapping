import junit.framework.TestCase;
import org.rcsb.uniprot.auto.Uniprot;
import org.rcsb.uniprot.auto.dao.UniprotDAO;
import org.rcsb.uniprot.auto.dao.UniprotDAOImpl;
import org.rcsb.uniprot.auto.tools.HibernateUtilsUniprot;
import org.rcsb.uniprot.auto.tools.JpaUtilsUniProt;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ap3 on 07/11/2014.
 */
public class TestOrganism extends TestCase {

    public void testHBB(){

        String geneName = "HBB";
        UniprotDAO dao = new UniprotDAOImpl();

        List<String> data = dao.getUniProtACsByGeneName(geneName);

//        System.out.println(data.size());
//
//        for ( String ac : data){
//            System.out.println(ac + " " + dao.getOrganism(ac) + " " + dao.getCommonName(dao.getOrganism(ac)));
//        }

        String rat = "P02091";


        // the rat entry has lower casecharacters in the gene name: Hbb
        assertTrue(data.contains(rat));

        // this is an old identifier for the rat entry:
        assertFalse(data.contains("P33584"));

        assertEquals(dao.getOrganism(rat),"Rattus norvegicus");

        String human ="P68871";

        assertTrue(data.contains(human));

        // and an secondary ID for human...
        assertFalse(data.contains("A4GX73"));

       assertTrue(dao.getOrganism(human).startsWith("Homo "));

        System.out.println(dao.getGeneNames(human));

    }
}
