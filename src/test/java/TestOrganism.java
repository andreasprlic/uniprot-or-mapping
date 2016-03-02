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

        assertNotNull(data);

//        System.out.println(data.size());
//
//        for ( String ac : data){
//            System.out.println(ac + " " + dao.getOrganism(ac) + " " + dao.getCommonName(dao.getOrganism(ac)));
//        }

        String rat = "P02091";

        // the rat entry has lower case characters in the gene name: Hbb
        assertTrue(data.contains(rat));

        // this is an old identifier for the rat entry:
        // these should be now included in the map
        assertTrue(data.contains("P33584"));

        // and the new ac code for the rat entry
        assertTrue(data.contains("P02091"));

        assertTrue(dao.getOrganism(rat).contains("Rattus norvegicus"));

        String human ="P68871";

        assertTrue(data.contains(human));

        // and an secondary ID for HBB human...
        assertTrue(data.contains("A4GX73"));

        assertTrue(dao.getOrganism(human).contains("Homo sapiens"));

        System.out.println(dao.getGeneNames(human));

        System.out.println(dao.getOrganism("P69905"));

    }
}
