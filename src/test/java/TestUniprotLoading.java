import junit.framework.TestCase;
import org.biojava3.auto.dao.UniprotDAO;
import org.biojava3.auto.dao.UniprotDAOImpl;
import org.biojava3.auto.tools.HibernateUtilsUniprot;
import org.biojava3.auto.tools.JpaUtilsUniProt;
import org.biojava3.auto.tools.UniProtTools;

import org.biojava3.auto.uniprot.Entry;
import org.biojava3.auto.uniprot.Uniprot;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.SortedMap;

/**
 * Created by ap3 on 13/08/2014.
 */
public class TestUniprotLoading extends TestCase {

    public void testReadingP50225() {

        testReading("P50225");

    }


    private void testReading(String accession) {
        //ClassLoader loader = Thread.currentThread().getContextClassLoader();

        URL u = UniProtTools.getURLforXML(accession);


        try {
            InputStream inStream = u.openStream();

            assertNotNull(inStream);

            Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);

            assertNotNull(up);

            assertTrue(up.getEntry().get(0).getAccession().size() > 1);

            assertNotNull(up.getEntry());
            assertNotNull(up.getEntry().get(0));
            assertNotNull(up.getEntry().get(0).getSequence());


            assertNotNull(up.getEntry().get(0).getSequence().getValue());
            String seq1 = new String(up.getEntry().get(0).getSequence().getValue());

            // System.out.println(seq1);

            UniprotDAO dao = new UniprotDAOImpl();


            seq1 = dao.cleanSequence(seq1);

            assertNotNull(seq1);
            assertTrue(seq1.length() == up.getEntry().get(0).getSequence().getLength());

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    /** makes sure that the sequencetype and EVIDENCEDSTRINGTYPE columns are defined correctly
     *
      */
    public void testWritingAndLoadingP50225(){

        testWritingAndLoading("P50225");

    };

    public void testWritingAndLoading(String accession){


        try {

            URL u = UniProtTools.getURLforXML(accession);

            InputStream inStream = u.openStream();


            Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);

            assertNotNull(up);

            Properties dbproperties = new Properties();
            InputStream propstream = this.getClass().getClassLoader().getResourceAsStream("database.properties");
            dbproperties.load(propstream);


            UniprotDAO dao = new UniprotDAOImpl();

            Uniprot up2 = dao.getUniProt(accession);

            if (up2 == null) {

                // not in DB yet...
                System.out.println("UP entry not found, loading into DB");

                EntityManager em = JpaUtilsUniProt.getEntityManager();


                em.getTransaction().begin();

                em.persist(up);

                em.getTransaction().commit();

                up2 = dao.getUniProt(accession);
            }

            String seq2 = dao.cleanSequence(up2.getEntry().get(0).getSequence().getValue().toString());
            assertTrue(seq2.length() == up2.getEntry().get(0).getSequence().getLength());

            String seq1 = dao.cleanSequence(up.getEntry().get(0).getSequence().getValue().toString());
            assertTrue(seq1.equals(seq2));

        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }

    }

//    public void testVersionLoading() {
//        UniprotDAO dao = new UniprotDAOImpl();
//        SortedMap<String, String> upVersions = UniProtTools.loadVersionsFromUniProt();
//        SortedMap<String, String> dbVersions = dao.getDbVersions();
//
//        for (String ac : dbVersions.keySet()) {
//            System.out.println(dbVersions.get(ac) + " =? " + upVersions.get(ac));
//        }
//    }

    public void testReadingA0AVK6() {
        testReading("A0AVK6");
    }
    public void testWritingAndLoadingA0AVK6(){
        testWritingAndLoading("A0AVK6");
    }

    public void testReadingA0JNT9(){
        testReading("A0JNT9");
    }
    public void testWritingAndLoadingA0JNT9(){
        testWritingAndLoading("A0JNT9");
    }
}
