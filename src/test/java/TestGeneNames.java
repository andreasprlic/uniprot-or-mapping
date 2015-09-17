import junit.framework.TestCase;
import org.rcsb.uniprot.auto.dao.UniprotDAO;
import org.rcsb.uniprot.auto.dao.UniprotDAOImpl;

import java.util.List;

/**
 * Created by ap3 on 28/10/2014.
 */
public class TestGeneNames  extends TestCase {

    public void testHBA1(){

        UniprotDAO dao = new UniprotDAOImpl();

        List<String> geneNames = dao.getGeneNames("P69905");

        assertNotNull(geneNames);
        assertTrue(geneNames.size() >0);

        System.out.println(geneNames);

    }
}
