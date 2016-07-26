package org.rcsb.uniprot.auto.tools;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingException;
import java.sql.Connection;


/**
 * Created by Andreas Prlic on 14/08/2014.
 */
public class HibernateUtilsUniprot {

    private static final Logger logger = LoggerFactory.getLogger(HibernateUtilsUniprot.class);

    protected static SessionFactory uniprotfactory = null;
    protected static Configuration uniprotcfg = null;

    public synchronized Configuration getConfiguration()
    {
        if (uniprotcfg == null)
        {
            init();
        }
        return uniprotcfg;
    }

    public synchronized  SessionFactory getSessionFactory()
    {
        if (uniprotfactory == null)
        {
            init();
        }
        return uniprotfactory;
    }


    private void init()
    {

        uniprotcfg = new AnnotationConfiguration();

        logger.info("\n" +
                "##############################################################################################\n"
                + "                 HibernateUtils Uniprot DB init building Configuration and SessionFactory          \n"
                + "##############################################################################################\n");

        try
        {
            logger.info("loading config from hibernate.cfg.xml file (uniprot project)");
            uniprotfactory = uniprotcfg.configure("hibernate.cfg.xml").buildSessionFactory();

        }
        catch (Exception e)
        {
            logger.error("error configuring UniprotDb: "+e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        try {

            logger.info("Initialized UniprotDb factory class:" + uniprotfactory.getClass().getName());
            logger.info("URL:" + uniprotcfg.getProperty(Environment.URL));

        } catch (Exception e){
            logger.error(e.getMessage(),e);
        }

    }


    //////////////////////////////////////////////////////////////////////////////////
    ///////  Uniprot DB Session stuff
    //////////////////////////////////////////////////////////////////////////////////




    public synchronized Session getSession() throws HibernateException
    {
        Session ans = null;


        checkUniprotDbCatalog();

        SessionFactory aligfactory = getSessionFactory();

        ans = aligfactory.openSession();

        return ans;
    }

    public  synchronized StatelessSession getStatelessSession()
    {
        StatelessSession ans = null;
        try
        {
            checkUniprotDbCatalog();
            ans = getSessionFactory().openStatelessSession();

        }
        catch (Exception e)
        {
            logger.error(e.getMessage(),e);
        }
        return ans;
    }

    public  synchronized void intializeUniprotDbSchema(Connection c)
    {

    }

    public  void checkUniprotDbCatalog()
    {
        return ;


    }

    public void close(){
        uniprotfactory.close();
    }


}
