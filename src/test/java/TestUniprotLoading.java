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
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ap3 on 13/08/2014.
 */
public class TestUniprotLoading extends TestCase{

    public void testReadingP50225(){
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        InputStream inStream = loader.getResourceAsStream("P50225.xml");



        try {


        assertNotNull(inStream);
        Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);

        assertNotNull(up);

        assertTrue(up.getEntry().get(0).getAccession().size()>1);

        assertNotNull(up.getEntry());
        assertNotNull(up.getEntry().get(0));
        assertNotNull(up.getEntry().get(0).getSequence());

        System.out.println(up.getEntry().get(0).getSequence().getValue());

        assertNotNull(up.getEntry().get(0).getSequence().getValue());
        String seq1 = up.getEntry().get(0).getSequence().getValue().toString();

            assertNotNull(seq1);
            assertTrue(seq1.length() == up.getEntry().get(0).getSequence().getLength());

        } catch (Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }
    }


    public void testWritingAndLoadingP50225(){


        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            InputStream inStream = loader.getResourceAsStream("P50225.xml");
            Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);

            assertNotNull(up);

            Properties dbproperties = new Properties();
            InputStream propstream = loader.getResourceAsStream("database.properties");
            dbproperties.load(propstream);

            UniprotDAO dao = new UniprotDAOImpl();

            Uniprot up2 = dao.getUniProt("P50225");

            if ( up2 == null) {

                // not in DB yet...
                System.out.println("UP entry not found, loading into DB");
                EntityManager em = JpaUtilsUniProt.getEntityManager();

                em.getTransaction().begin();


                em.persist(up);

                em.getTransaction().commit();

                up2 = dao.getUniProt("P50225");
            }


            String seq2 = up2.getEntry().get(0).getSequence().getValue().toString();
            assertTrue(seq2.length() == up2.getEntry().get(0).getSequence().getLength());

            String seq1 = up.getEntry().get(0).getSequence().getValue().toString();
            assertTrue(seq1.equals(seq2));



            assertTrue(true);
        } catch (Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
}
