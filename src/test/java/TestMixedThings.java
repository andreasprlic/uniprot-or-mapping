import junit.framework.TestCase;
import org.rcsb.uniprot.auto.Uniprot;
import org.rcsb.uniprot.auto.dao.UniprotDAO;
import org.rcsb.uniprot.auto.dao.UniprotDAOImpl;

import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by ap3 on 17/12/2014.
 */
public class TestMixedThings  extends TestCase{

    public void testUniProtNameMap(){

        UniprotDAO dao = new UniprotDAOImpl();

        String[] ids = new String[]{"P00123","P50225","Q92818"};

        Set<String> myIds = new TreeSet<String>();

        for (String s : ids){
            myIds.add(s);
        }
        Map<String,String> nameMap = dao.getAC2NameMap(myIds);

        System.out.println(nameMap);

        assertTrue(nameMap.keySet().size()==2);

        assertTrue(! nameMap.keySet().contains("Q92818"));

    }

    public void testUniprotRecNames(){
        UniprotDAO dao = new UniprotDAOImpl();

        String[] ids = new String[]{"P00123","P50225","Q92818"};

        Set<String> myIds = new TreeSet<String>();

        for (String s : ids){
            myIds.add(s);
        }
        Map<String,String> nameMap = dao.getRecommendedNameMap(myIds);

        assertTrue(nameMap.keySet().size()>1);
    }

    public void testUniprotSubmittedNames(){

        // this test will only work if Trembl entries have been loaded into the DB....
        // since we only load SWISSPROT entries by default, this test is mainly here for demonstration purposes.

        UniprotDAO dao = new UniprotDAOImpl();

        String[] ids = new String[]{"O28736","Q70KP4","O28875","A8B2I4","C6LVW8","Q04822"};

        Set<String> myIds = new TreeSet<String>();

        for (String s : ids){
            myIds.add(s);
        }
        Map<String,String> nameMap = dao.getSubmittedNameMap(myIds);

        System.out.println(nameMap);
    }
}
