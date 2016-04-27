package org.rcsb.uniprot.auto.tools;


import org.hibernate.jpa.HibernateEntityManagerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.Map;
import java.util.Properties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** JPA tools for when dealing with a UniProt installation. Also contains some hacks to modifiy the DB schema, to work
 *  around some hyperjaxb shortcomings.
 *
 * Created by Andreas Prlic on 18/08/2014.
 */
public class JpaUtilsUniProt {

    private static final Logger logger = LoggerFactory.getLogger(JpaUtilsUniProt.class );

    /**
     * The properties file with db connectoin settings in home directory. This will override the file on classpath
     */
    public static final File PROPERTIES_FILE_HOME = new File(System.getProperty("user.home"), "uniprot.database.properties");

    private static EntityManagerFactory entityManagerFactory;

    private static AtomicBoolean busy = new AtomicBoolean(false);

    private static void init() {

        if (entityManagerFactory != null)
            return;

        if (busy.get()) {
            logger.warn("Already initializing uniprot persistence in other thread");
        }

        while (busy.get()) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
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

        } catch (IOException e) {
           logger.error("Could not read uniprot jpa properties from input stream. Error: {}", e.getMessage());
        }

        logger.info("create EMF...");
        long timeS = System.currentTimeMillis();
        EntityManagerFactory myEntityManagerFactory = Persistence.createEntityManagerFactory("org.rcsb.uniprot.auto", dbproperties);
        long timeE = System.currentTimeMillis();

        logger.info("took " + (timeE- timeS) + " to init uniprot EMF");

        return myEntityManagerFactory;
    }

    private static EntityManagerFactory createDefaultEntityManagerFactory() {

        ClassLoader cloader = Thread.currentThread().getContextClassLoader();
        InputStream propstream = cloader.getResourceAsStream("database.properties");
        if (propstream == null) {
            logger.error("Could not get file database.properties from class context!");
        }

        // if a properties file is present in home it will override the classpath one
        if (PROPERTIES_FILE_HOME.exists()) {
            try {
                propstream = new FileInputStream(PROPERTIES_FILE_HOME);
            } catch (IOException e) {
                logger.error("Properties file {} could not be read. Error: {} ", PROPERTIES_FILE_HOME, e.getMessage());
            }
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

    private static String getDBNAme(){

        logger.info("get DBA NAMe");

        String databaseName = "uniprot";

        EntityManagerFactory emf = getEntityManagerFactory();

        if ( emf instanceof HibernateEntityManagerFactory) {
            HibernateEntityManagerFactory hemf = (HibernateEntityManagerFactory) emf;

            Map<String, Object> props = hemf.getProperties();
            for (String key : props.keySet()) {
//                if ( key.startsWith("hibernate") || key.startsWith("c3p0"))
//                    System.out.println(key + " : " + props.get(key).toString());

                if ( key.equals("hibernate.connection.url")){

                    // extract the DB name from the URL...

                    databaseName = extractDBNameFromURL(props.get(key).toString());

                }
            }

        }

        return databaseName;
    }

    private static String extractDBNameFromURL(String s) {

        Pattern pattern = Pattern.compile("jdbc:(\\S+):(\\S+)/(\\S+)");

        Matcher matcher = pattern.matcher(s);

        if ( matcher.find()) {


            String g3 = matcher.group(3);

            String[] spl = g3.split("\\?");
            if ( spl.length > 1) {

                return  spl[0];
            } else {
                return g3;
            }
        }

        return null;

    }


    private static boolean hasRequiredIdsTable() {

        String databaseName = "uniprot";

        databaseName = getDBNAme();

        logger.info("using DB name :" + databaseName);

        String sql = "SELECT * FROM INFORMATION_SCHEMA.COLUMNS " +
        " WHERE TABLE_SCHEMA='"+ databaseName+"' " +
        " AND TABLE_NAME='required_ids' ";


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

        logger.info("Updating UniProt DB schema");

        entityManager.getTransaction().begin();
        String sql1 = "alter table sequence_type change column value_ value_ TEXT";
        entityManager.createNativeQuery(sql1).executeUpdate();

        String sql2 = "alter table EVIDENCED_STRING_TYPE change column value_ value_ TEXT";
        entityManager.createNativeQuery(sql2).executeUpdate();

        // isoform type note (e.g. Q9N0Z4-2)
//        String sql3 = "alter table NOTE change column value_ value_ TEXT";
//        entityManager.createNativeQuery(sql3).executeUpdate();

        // featuretype_variation. could not get this to work using bindings.xjb
        String sql4 = "alter table feature_type_variation change column hjvalue hjvalue text";
        entityManager.createNativeQuery(sql4).executeUpdate();

        // Fix for C0LGT6 reference scop
        String sql5 = "alter table reference_type_scope_ change column hjvalue hjvalue text";
        entityManager.createNativeQuery(sql5).executeUpdate();

        // create the table required_ids which tracks what uniprot IDS should be loaded by the loading framework
        String sql6 = "CREATE TABLE required_ids (uniprot_ac varchar(15));";

        entityManager.createNativeQuery(sql6).executeUpdate();

        // add index to table
        String sql7 = "alter table entry_accession add index (HJVALUE)";
        entityManager.createNativeQuery(sql7).executeUpdate();

        String sql8 = "alter table db_reference_type add index (TYPE_)";
        entityManager.createNativeQuery(sql8).executeUpdate();

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
