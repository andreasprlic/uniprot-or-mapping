import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.rcsb.uniprot.auto.dao.UniprotDAO;
import org.rcsb.uniprot.auto.dao.UniprotDAOImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by ap3 on 17/12/2014.
 */
public class TestMixedThings  extends TestCase{

    private static final Logger logger = LoggerFactory.getLogger(TestMixedThings.class);

    private UniprotDAO dao;

    @Before
    public void setUp() {
        dao = new UniprotDAOImpl();

    }

    @After
    public void tearDown() {

    }

    public void testUniProtNameMap(){

        String[] ids = new String[]{"P00123","P50225","Q92818"};

        Set<String> myIds = new TreeSet<String>();

        for (String s : ids){
            myIds.add(s);
        }
        Map<String,String> nameMap = dao.getAC2NameMap(myIds);

        System.out.println(nameMap);

        assertTrue(nameMap.keySet().size()==2);
        assertTrue(! nameMap.keySet().contains("Q92818"));
        logger.info("completed testUniProtNameMap ");

    }

    public void testUniprotRecNames(){

        String[] ids = new String[]{"P00123","P50225","Q92818"};

        Set<String> myIds = new TreeSet<String>();

        for (String s : ids){
            myIds.add(s);
        }

        assertTrue(dao.getRecommendedNameMap(myIds).keySet().size()>1);
        logger.info("completed testUniprotRecNames ");
    }

    public void testUniprotSubmittedNames(){

        // this test will only work if Trembl entries have been loaded into the DB....
        // since we only load SWISSPROT entries by default, this test is mainly here for demonstration purposes.


        String[] ids = new String[]{"O28736","Q70KP4","O28875","A8B2I4","C6LVW8","Q04822"};

        Set<String> myIds = new TreeSet<String>();

        for (String s : ids){
            myIds.add(s);
        }

        assertTrue(dao.getSubmittedNameMap(myIds).size() > 0);
        logger.info("completed testUniprotSubmittedNames ");
    }

    public void testUniprotAlternateNames(){

        // this test will only work if Trembl entries have been loaded into the DB....
        // since we only load SWISSPROT entries by default, this test is mainly here for demonstration purposes.


        String[] ids = new String[]{"O28736","Q70KP4","O28875","A8B2I4","C6LVW8","Q04822"};

        Set<String> myIds = new TreeSet<String>();

        for (String s : ids){
            myIds.add(s);
        }

        assertTrue(dao.getAlternativeNameMap(myIds).size() > 0);
        logger.info("completed testUniprotAlternateNames ");
    }

    public void testAccessMethods(){


        dao.getAllGeneNames();

//        dao.getRecommendedNames4Components();
//        dao.getAllUniProtIDs();
//        dao.getAlternativeNameMap();
//        dao.getECNumbers4Components();
//        dao.getDbVersions();
//        dao.getECNumbers();
//        dao.getShortNameMap();
//        dao.getCommonName("homo sapiens");
//        dao.getPdbReferencesFromUniProt();


    }

}
