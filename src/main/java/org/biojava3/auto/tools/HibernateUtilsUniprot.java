package org.biojava3.auto.tools;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;

import javax.naming.NamingException;
import java.sql.Connection;


/**
 * Created by ap3 on 14/08/2014.
 */
public class HibernateUtilsUniprot {

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

        System.err.println("\n" +
                "##############################################################################################\n"
                + "                 HibernateUtils Uniprot DB init building Configuration and SessionFactory          \n"
                + "##############################################################################################\n");

        try
        {
            System.out.println("loading config from hibernate.cfg.xml file (uniprot project)");
            uniprotfactory = uniprotcfg.configure("hibernate.cfg.xml").buildSessionFactory();

        }
        catch (Exception e)
        {
            e.printStackTrace();
            System.err.println("error configuring UniprotDb");
            throw new RuntimeException(e.getMessage());
        }
        try {

            System.err.println("Initialized UniprotDb factory class:" + uniprotfactory.getClass().getName());
            System.out.println("URL:" + uniprotcfg.getProperty(Environment.URL));

        } catch (Exception e){
            e.printStackTrace();
        }

    }




    public  void addDefaultUniprotDbConfig(Configuration cfg) throws NamingException
    {

        System.out.println("setting default UniprotDb config...");


        cfg.setProperty("hibernate.connection.datasource","java:/comp/env/jdbc/UniProtDb");
        cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.MySQL5InnoDBDialect");
        cfg.setProperty("hibernate.connection.driver_class", "com.mysql.jdbc.Driver");
        cfg.setProperty("hibernate.connection.username", "developer");
        cfg.setProperty("hibernate.connection.password", "SuperSecure!");
        cfg.setProperty("hibernate.connection.url",
                "jdbc:mysql://db-developer1.rcsb.org:8888/uniprot?autoReconnect=true&max_allowed_packet=2048000");
        cfg.setProperty("hibernate.show_sql", "true");
        cfg.setProperty("hibernate.use_outer_join", "true");
        cfg.setProperty("hibernate.jdbc.batch_size", "500");
        cfg.setProperty("hibernate.connection.isolation", "2"); // TRANSACTION_READ_COMMITTED
        cfg.setProperty("hibernate.c3p0.min_size", "5");
        cfg.setProperty("hibernate.c3p0.max_size", "25");
        cfg.setProperty("hibernate.c3p0.timeout", "1800");
        cfg.setProperty("hibernate.cache.provider_class", "org.hibernate.cache.NoCacheProvider");
        cfg.setProperty("hibernate.cache.use_query_cache", "false");
        cfg.setProperty("hibernate.cache.use_minimal_puts", "false");
        cfg.setProperty("hibernate.max_fetch_depth", "3");
        cfg.setProperty("hibernate.hbm2ddl.auto", "update");

        System.out.println("using default uniprotDB database connection: " + cfg.getProperty("hibernate.connection.url"));


    }


    //////////////////////////////////////////////////////////////////////////////////
    ///////  Uniprot DB Session stuff
    //////////////////////////////////////////////////////////////////////////////////




    public synchronized Session getSession() throws HibernateException
    {
        Session ans = null;
        try
        {

            checkUniprotDbCatalog();

            SessionFactory aligfactory = getSessionFactory();

            ans = aligfactory.openSession();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new HibernateException(e);
        }
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
            e.printStackTrace();
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
