package org.biojava3.auto.tools;
import org.biojava3.auto.uniprot.Uniprot;
import org.hibernate.Session;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.InputStream;

/**
 * Created by ap3 on 13/08/2014.
 */
public class UniProtTools {

    public static Uniprot loadUniProt(InputStream inputStream) throws JAXBException {
        JAXBContext ctx = JAXBContext.newInstance(new Class[] {Uniprot.class});

        Unmarshaller um = ctx.createUnmarshaller();

        Uniprot uniprot = (Uniprot) um.unmarshal(inputStream);

        return uniprot;
    }
}
