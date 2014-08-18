import junit.framework.TestCase;
import org.biojava3.auto.tools.HibernateUtilsUniprot;
import org.biojava3.auto.tools.UniProtTools;

import org.biojava3.auto.uniprot.Uniprot;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.io.InputStream;

/**
 * Created by ap3 on 13/08/2014.
 */
public class TestUniprotLoading extends TestCase{

    public void testLoadingP50225(){
    ClassLoader loader = Thread.currentThread().getContextClassLoader();
    InputStream inStream = loader.getResourceAsStream("P50225.xml");

    assertNotNull(inStream);

        try {
            Uniprot up = UniProtTools.loadUniProt(inStream);

            assertNotNull(up);

            assertTrue(up.getEntry().get(0).getAccession().size()>1);
            HibernateUtilsUniprot hibernate = new HibernateUtilsUniprot();
            Session sess = hibernate.getSession();

            sess.save(up);

            sess.close();

            hibernate.close();

            assertTrue(true);
        } catch (Exception e){
            e.printStackTrace();
            fail(e.getMessage());
        }

    }
}
