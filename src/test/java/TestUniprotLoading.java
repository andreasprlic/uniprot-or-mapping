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

        System.out.println(u);
        try {
            InputStream inStream = u.openStream();

            assertNotNull(inStream);

            Uniprot up = UniProtTools.readUniProtFromInputStream(inStream);

            assertNotNull(up);

            assertTrue(up.getEntry().get(0).getAccession().size() > 0);

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

    public void testReadingA1A4Y4(){
        testReading("A1A4Y4");
    }

    /** 2014-08-19 19:38:02,268 ERROR [org.hibernate.engine.jdbc.spi.SqlExceptionHelper] - <Data truncation: Data too long for column 'DESCRIPTION' at row 1>
     Failed to load A1A4Y4
     javax.persistence.PersistenceException: org.hibernate.exception.DataException: could not execute statement
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1763)
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1677)
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1683)
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.persist(AbstractEntityManagerImpl.java:1187)
     at org.biojava3.auto.org.biojava3.auto.load.LoadAllUniProt.main(LoadAllUniProt.java:76)
     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     at java.lang.reflect.Method.invoke(Method.java:601)
     at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)
     Caused by: org.hibernate.exception.DataException: could not execute statement
     at org.hibernate.exception.internal.SQLExceptionTypeDelegate.convert(SQLExceptionTypeDelegate.java:69)
     at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:49)
     at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:126)
     at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:112)
     at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:211)
     at org.hibernate.id.IdentityGenerator$GetGeneratedKeysDelegate.executeAndExtract(IdentityGenerator.java:96)
     at org.hibernate.id.insert.AbstractReturningDelegate.performInsert(AbstractReturningDelegate.java:58)
     at org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:3032)
     at org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:3558)
     at org.hibernate.action.internal.EntityIdentityInsertAction.execute(EntityIdentityInsertAction.java:98)
     at org.hibernate.engine.spi.ActionQueue.execute(ActionQueue.java:490)
     at org.hibernate.engine.spi.ActionQueue.addResolvedEntityInsertAction(ActionQueue.java:195)
     at org.hibernate.engine.spi.ActionQueue.addInsertAction(ActionQueue.java:179)
     at org.hibernate.engine.spi.ActionQueue.addAction(ActionQueue.java:214)
     at org.hibernate.event.internal.AbstractSaveEventListener.addInsertAction(AbstractSaveEventListener.java:324)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:288)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:194)
     at org.hibernate.event.internal.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:125)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener.saveWithGeneratedId(JpaPersistEventListener.java:84)
     at org.hibernate.event.internal.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:206)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:149)
     at org.hibernate.internal.SessionImpl.firePersist(SessionImpl.java:801)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:794)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener$1.cascade(JpaPersistEventListener.java:97)
     at org.hibernate.engine.internal.Cascade.cascadeToOne(Cascade.java:350)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:293)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascade(Cascade.java:118)
     at org.hibernate.event.internal.AbstractSaveEventListener.cascadeBeforeSave(AbstractSaveEventListener.java:432)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:265)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:194)
     at org.hibernate.event.internal.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:125)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener.saveWithGeneratedId(JpaPersistEventListener.java:84)
     at org.hibernate.event.internal.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:206)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:149)
     at org.hibernate.internal.SessionImpl.firePersist(SessionImpl.java:801)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:794)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener$1.cascade(JpaPersistEventListener.java:97)
     at org.hibernate.engine.internal.Cascade.cascadeToOne(Cascade.java:350)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:293)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascadeCollectionElements(Cascade.java:379)
     at org.hibernate.engine.internal.Cascade.cascadeCollection(Cascade.java:319)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:296)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascade(Cascade.java:118)
     at org.hibernate.event.internal.AbstractSaveEventListener.cascadeAfterSave(AbstractSaveEventListener.java:460)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:294)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:194)
     at org.hibernate.event.internal.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:125)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener.saveWithGeneratedId(JpaPersistEventListener.java:84)
     at org.hibernate.event.internal.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:206)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:149)
     at org.hibernate.internal.SessionImpl.firePersist(SessionImpl.java:801)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:794)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener$1.cascade(JpaPersistEventListener.java:97)
     at org.hibernate.engine.internal.Cascade.cascadeToOne(Cascade.java:350)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:293)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascadeCollectionElements(Cascade.java:379)
     at org.hibernate.engine.internal.Cascade.cascadeCollection(Cascade.java:319)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:296)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascade(Cascade.java:118)
     at org.hibernate.event.internal.AbstractSaveEventListener.cascadeAfterSave(AbstractSaveEventListener.java:460)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:294)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:194)
     at org.hibernate.event.internal.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:125)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener.saveWithGeneratedId(JpaPersistEventListener.java:84)
     at org.hibernate.event.internal.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:206)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:149)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:75)
     at org.hibernate.internal.SessionImpl.firePersist(SessionImpl.java:811)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:784)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:789)
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.persist(AbstractEntityManagerImpl.java:1181)
     ... 6 more
     Caused by: com.mysql.jdbc.MysqlDataTruncation: Data truncation: Data too long for column 'DESCRIPTION' at row 1
     at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:4224)
     at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:4158)
     at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:2615)
     at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2776)
     at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2840)
     at com.mysql.jdbc.PreparedStatement.executeInternal(PreparedStatement.java:2082)
     at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2334)
     at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2262)
     at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2246)
     at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:208)
     ... 77 more
     */
    public void testWritingAndLoadingA1A4Y4(){
        testReading("A1A4Y4");
    }

    /**
     * 10703
     * Failed to load A1KNB8
     javax.persistence.PersistenceException: org.hibernate.exception.DataException: could not execute statement
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1763)
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1677)
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.convert(AbstractEntityManagerImpl.java:1683)
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.persist(AbstractEntityManagerImpl.java:1187)
     at org.biojava3.auto.org.biojava3.auto.load.LoadAllUniProt.main(LoadAllUniProt.java:77)
     at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
     at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:57)
     at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
     at java.lang.reflect.Method.invoke(Method.java:601)
     at com.intellij.rt.execution.application.AppMain.main(AppMain.java:134)
     Caused by: org.hibernate.exception.DataException: could not execute statement
     at org.hibernate.exception.internal.SQLExceptionTypeDelegate.convert(SQLExceptionTypeDelegate.java:69)
     at org.hibernate.exception.internal.StandardSQLExceptionConverter.convert(StandardSQLExceptionConverter.java:49)
     at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:126)
     at org.hibernate.engine.jdbc.spi.SqlExceptionHelper.convert(SqlExceptionHelper.java:112)
     at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:211)
     at org.hibernate.id.IdentityGenerator$GetGeneratedKeysDelegate.executeAndExtract(IdentityGenerator.java:96)
     at org.hibernate.id.insert.AbstractReturningDelegate.performInsert(AbstractReturningDelegate.java:58)
     at org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:3032)
     at org.hibernate.persister.entity.AbstractEntityPersister.insert(AbstractEntityPersister.java:3558)
     at org.hibernate.action.internal.EntityIdentityInsertAction.execute(EntityIdentityInsertAction.java:98)
     at org.hibernate.engine.spi.ActionQueue.execute(ActionQueue.java:490)
     at org.hibernate.engine.spi.ActionQueue.addResolvedEntityInsertAction(ActionQueue.java:195)
     at org.hibernate.engine.spi.ActionQueue.addInsertAction(ActionQueue.java:179)
     at org.hibernate.engine.spi.ActionQueue.addAction(ActionQueue.java:214)
     at org.hibernate.event.internal.AbstractSaveEventListener.addInsertAction(AbstractSaveEventListener.java:324)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:288)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:194)
     at org.hibernate.event.internal.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:125)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener.saveWithGeneratedId(JpaPersistEventListener.java:84)
     at org.hibernate.event.internal.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:206)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:149)
     at org.hibernate.internal.SessionImpl.firePersist(SessionImpl.java:801)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:794)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener$1.cascade(JpaPersistEventListener.java:97)
     at org.hibernate.engine.internal.Cascade.cascadeToOne(Cascade.java:350)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:293)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascade(Cascade.java:118)
     at org.hibernate.event.internal.AbstractSaveEventListener.cascadeBeforeSave(AbstractSaveEventListener.java:432)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:265)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:194)
     at org.hibernate.event.internal.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:125)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener.saveWithGeneratedId(JpaPersistEventListener.java:84)
     at org.hibernate.event.internal.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:206)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:149)
     at org.hibernate.internal.SessionImpl.firePersist(SessionImpl.java:801)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:794)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener$1.cascade(JpaPersistEventListener.java:97)
     at org.hibernate.engine.internal.Cascade.cascadeToOne(Cascade.java:350)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:293)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascadeCollectionElements(Cascade.java:379)
     at org.hibernate.engine.internal.Cascade.cascadeCollection(Cascade.java:319)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:296)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascade(Cascade.java:118)
     at org.hibernate.event.internal.AbstractSaveEventListener.cascadeAfterSave(AbstractSaveEventListener.java:460)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:294)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:194)
     at org.hibernate.event.internal.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:125)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener.saveWithGeneratedId(JpaPersistEventListener.java:84)
     at org.hibernate.event.internal.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:206)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:149)
     at org.hibernate.internal.SessionImpl.firePersist(SessionImpl.java:801)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:794)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener$1.cascade(JpaPersistEventListener.java:97)
     at org.hibernate.engine.internal.Cascade.cascadeToOne(Cascade.java:350)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:293)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascadeCollectionElements(Cascade.java:379)
     at org.hibernate.engine.internal.Cascade.cascadeCollection(Cascade.java:319)
     at org.hibernate.engine.internal.Cascade.cascadeAssociation(Cascade.java:296)
     at org.hibernate.engine.internal.Cascade.cascadeProperty(Cascade.java:161)
     at org.hibernate.engine.internal.Cascade.cascade(Cascade.java:118)
     at org.hibernate.event.internal.AbstractSaveEventListener.cascadeAfterSave(AbstractSaveEventListener.java:460)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSaveOrReplicate(AbstractSaveEventListener.java:294)
     at org.hibernate.event.internal.AbstractSaveEventListener.performSave(AbstractSaveEventListener.java:194)
     at org.hibernate.event.internal.AbstractSaveEventListener.saveWithGeneratedId(AbstractSaveEventListener.java:125)
     at org.hibernate.jpa.event.internal.core.JpaPersistEventListener.saveWithGeneratedId(JpaPersistEventListener.java:84)
     at org.hibernate.event.internal.DefaultPersistEventListener.entityIsTransient(DefaultPersistEventListener.java:206)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:149)
     at org.hibernate.event.internal.DefaultPersistEventListener.onPersist(DefaultPersistEventListener.java:75)
     at org.hibernate.internal.SessionImpl.firePersist(SessionImpl.java:811)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:784)
     at org.hibernate.internal.SessionImpl.persist(SessionImpl.java:789)
     at org.hibernate.jpa.spi.AbstractEntityManagerImpl.persist(AbstractEntityManagerImpl.java:1181)
     ... 6 more
     Caused by: com.mysql.jdbc.MysqlDataTruncation: Data truncation: Data too long for column 'TITLE' at row 1
     at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:4224)
     at com.mysql.jdbc.MysqlIO.checkErrorPacket(MysqlIO.java:4158)
     at com.mysql.jdbc.MysqlIO.sendCommand(MysqlIO.java:2615)
     at com.mysql.jdbc.MysqlIO.sqlQueryDirect(MysqlIO.java:2776)
     at com.mysql.jdbc.ConnectionImpl.execSQL(ConnectionImpl.java:2840)
     at com.mysql.jdbc.PreparedStatement.executeInternal(PreparedStatement.java:2082)
     at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2334)
     at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2262)
     at com.mysql.jdbc.PreparedStatement.executeUpdate(PreparedStatement.java:2246)
     at org.hibernate.engine.jdbc.internal.ResultSetReturnImpl.executeUpdate(ResultSetReturnImpl.java:208)
     ... 77 more

     Process finished with exit code 255

     */
    public void testReadingA1KNB8(){
        testReading("A1KNB8");

    }
    public void testWritingAndLoadingA1KNB8(){
        testWritingAndLoading("A1KNB8");
    }


    public void testReadingA2AKX3(){
        testReading("A2AKX3");
    }

    public void testWritingAndLoadingA2AKX3(){
        testWritingAndLoading("A2AKX3");
    }

    //Caused by: com.mysql.jdbc.MysqlDataTruncation: Data truncation: Data too long for column 'HJVALUE' at row 1
    public void testReadingA2AKY4(){
        testReading("A2AKY4");
    }

    public void testWritingAndLoadingA2AKY4(){
        testWritingAndLoading("A2AKY4");
    }

    // A8WGB1 A2AL36 A4UHT7
}
