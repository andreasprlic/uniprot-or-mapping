package org.biojava3.auto.tools;

import org.hibernate.Session;
import org.hibernate.jpa.HibernateEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ap3 on 18/08/2014.
 */
public class JpaUtilsUniProt {


    private static EntityManager entityManager;


    private static void init() {

        if (entityManager != null)
            return;

        Properties dbproperties = new Properties();

        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        InputStream propstream = cloader.getResourceAsStream("database.properties");
        try {
            dbproperties.load(propstream);
        } catch (Exception e) {
            e.printStackTrace();
            // use log4j for logging
        }
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("org.biojava3.auto.uniprot", dbproperties);

        entityManager = emf.createEntityManager();

    }

    public static EntityManager getEntityManager() {
        if (entityManager == null) {
            init();
        }

        return entityManager;
    }

//    public static Session getSession() {
//        if (entityManager == null) {
//            init();
//        }
//
//        HibernateEntityManager hem = entityManager.unwrap(HibernateEntityManager.class);
//        return hem.getSession();
//    }
}
