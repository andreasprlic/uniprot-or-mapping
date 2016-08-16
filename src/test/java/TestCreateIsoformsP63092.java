import junit.framework.TestCase;
import org.biojava.nbio.core.sequence.ProteinSequence;
import org.rcsb.uniprot.auto.Uniprot;
import org.rcsb.uniprot.config.RCSBUniProtMirror;
import org.rcsb.uniprot.isoform.IsoformMapper;
import org.rcsb.uniprot.isoform.IsoformTools;

import javax.xml.bind.JAXBException;
import java.io.FileNotFoundException;

/**
 * Created by ap3 on 16/08/2016.
 */
public class TestCreateIsoformsP63092 extends TestCase{

    public void testQ8V5E0(){


        try {
            Uniprot up = RCSBUniProtMirror.getUniProtFromFile("P63092");

            assertNotNull(up);

            IsoformTools isoTools = new IsoformTools();

            ProteinSequence[] isoforms = isoTools.getIsoforms(up);

            assertNotNull(isoforms);

            assertEquals(8,isoforms.length);


            // values from UniProt
            assertEquals(394,isoforms[0].getLength());

            assertEquals(380,isoforms[1].getLength());

            assertEquals(379,isoforms[2].getLength());

            assertEquals(395,isoforms[7].getLength());


            assertNotNull(isoforms);
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        }


    }


}
