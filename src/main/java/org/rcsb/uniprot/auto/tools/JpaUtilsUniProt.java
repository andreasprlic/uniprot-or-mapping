package org.rcsb.uniprot.auto.tools;

import org.hibernate.Session;
import org.hibernate.jpa.HibernateEntityManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ap3 on 18/08/2014.
 */
public class JpaUtilsUniProt {



    private static EntityManagerFactory entityManagerFactory;

    private static AtomicBoolean busy = new AtomicBoolean(false);

    private static void init() {

        if (entityManagerFactory != null)
            return;

        if (busy.get()) {
            System.err.println("Already initializing uniprot persistence in other thread");
        }

        while (busy.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if ( entityManagerFactory != null);
                return;
        }

        busy.set(true);


        EntityManagerFactory emf = createDefaultEntityManagerFactory();

        EntityManager entityManager = emf.createEntityManager();

        validateSQLSchema(entityManager);

        entityManager.close();
        busy.set(false);

    }

    public static void setEntityManagerFactory(EntityManagerFactory factory){

        entityManagerFactory = factory;
    }

    public static EntityManagerFactory getEntityManagerFactory(){
        if (entityManagerFactory != null)
            return entityManagerFactory;

        entityManagerFactory = createDefaultEntityManagerFactory();
        return entityManagerFactory;
    }

    public static EntityManagerFactory createEntityManagerFactory(InputStream propstream){

        Properties dbproperties = new Properties();
        try {
            dbproperties.load(propstream);

        } catch (Exception e) {
            e.printStackTrace();
            // use log4j for logging
        }

        EntityManagerFactory myEntityManagerFactory = Persistence.createEntityManagerFactory("org.rcsb.uniprot.auto", dbproperties);

        return myEntityManagerFactory;
    }

    private static EntityManagerFactory createDefaultEntityManagerFactory() {

        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        InputStream propstream = cloader.getResourceAsStream("database.properties");
        if (propstream == null) {
            System.err.println("Could not get file database.properties from class context!");
        }

        return createEntityManagerFactory(propstream);

    }

    public static void validateSQLSchema(EntityManager entityManager) {

        // STEP 1 check if we need to modify the column definitions of a few tables

        // Does the required table exist?
        boolean hasTable = hasRequiredIdsTable();

        // STEP 2 update the column definition, where we can't use hyperjaxb3 due to the lack of
        // support for inheritance

        if ( ! hasTable)
            fixSQLSchema(entityManager);


    }

    private static boolean hasRequiredIdsTable() {

        String sql = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA='uniprot' AND TABLE_NAME='required_ids'";

         EntityManager em = getEntityManager();
        Query q = em.createNativeQuery(sql);
        boolean hasTable = q.getResultList().size()>0;

        em.close();

        return hasTable;
    }

    /**
     * make sure the sequence table is correct
     * UGLY HACK, BECAUSE HYPERJAXB3 does not seem to support resolving inherited definitions !!!
     * ARGH
     */
    private static void fixSQLSchema(EntityManager entityManager) {

        System.out.println("Updating UniProt DB schema");

        entityManager.getTransaction().begin();
        String sql1 = "alter table sequencetype change column value_ value_ TEXT";
        entityManager.createNativeQuery(sql1).executeUpdate();

        String sql2 = "alter table EVIDENCEDSTRINGTYPE change column value_ value_ TEXT";
        entityManager.createNativeQuery(sql2).executeUpdate();

        // isoform type note (e.g. Q9N0Z4-2)
        String sql3 = "alter table NOTE change column value_ value_ TEXT";
        entityManager.createNativeQuery(sql3).executeUpdate();

        // featuretype_variation. could not get this to work using bindings.xjb
        String sql4 = "alter table featuretype_variation change column hjvalue hjvalue text";
        entityManager.createNativeQuery(sql4).executeUpdate();

        // Fix for C0LGT6 reference scop
        String sql5 = "alter table referencetype_scope_ change column hjvalue hjvalue text";
        entityManager.createNativeQuery(sql5).executeUpdate();

        // create the table required_ids which tracks what uniprot IDS should be loaded by the loading framework
        String sql6 = "CREATE TABLE required_ids (uniprot_ac varchar(15));";

        entityManager.createNativeQuery(sql6).executeUpdate();

        // add index to table
        String sql7 = "alter table entry_accession add index (HJVALUE)";
        entityManager.createNativeQuery(sql7).executeUpdate();


        entityManager.getTransaction().commit();

    }

    public static EntityManager getEntityManager() {
        if (entityManagerFactory == null) {
            init();
        }

        // we need to recreate the entity manager, it has been closed

        EntityManagerFactory emf = getEntityManagerFactory();

        return (emf.createEntityManager());


    }

}
