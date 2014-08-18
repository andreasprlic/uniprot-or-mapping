package org.biojava3.auto.core;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.hibernate.ejb.Ejb3Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.rcsb.core.context.*;
import org.rcsb.impl.core.CoreContext;

import com.google.common.base.Function;
import com.google.common.collect.Lists;



public class JpaStore extends ContextImpl {

    private static final long serialVersionUID = -1564984349134996086L;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public JpaStore() {
        for(Class<?> type:context().getTypes())
            this.register(new JpaFactory(type));
    }

    @Override
    public Class<?> implementation(Class<?> type) {
        return context().implementation(type);
    }

    @Override
    public Collection<Class<? extends Identifiable<?, ?>>> getImplementations() {
        return context().getImplementations();
    }

    EntityManager entityManager=null;

    public EntityManager getEntityManager() {
        if(entityManager==null)
            entityManager=newEntityManager();
        return entityManager;
    }

    public void setEntityManager(EntityManager em) {
        if(entityManager!=null) {
            commitTransaction();
            entityManager.close();
        }
        entityManager=em;
    }

    @SuppressWarnings("rawtypes")
    static Map properties=new Properties();
    static {
        try {
            ((Properties)properties).load(JpaStore.class.getClassLoader().getResourceAsStream("jpa.properties"));
        } catch (IOException e) {
            properties=System.getProperties();
        }
    }
    static EntityManagerFactory entityManagerFactory = null;


    public static EntityManagerFactory getEntityManagerFactory() {
        if(entityManagerFactory==null)
            entityManagerFactory=Persistence.createEntityManagerFactory( "org.rcsb.core",properties);
        return entityManagerFactory;
    }

    public static void setEntityManagerFactory(
            EntityManagerFactory entityManagerFactory) {
        JpaStore.entityManagerFactory = entityManagerFactory;
    }

    @SuppressWarnings("rawtypes")
    public static Map getProperties() {
        return properties;
    }

    @SuppressWarnings("rawtypes")
    public static void setProperties(Map properties) {
        JpaStore.properties = properties;
    }

    public static EntityManager newEntityManager() {
        EntityManager entityManager=getEntityManagerFactory().createEntityManager();
        return entityManager;
    }

    public static void createSchema(String fileName) {
        Ejb3Configuration conf=new Ejb3Configuration();
        conf=conf.configure( "org.rcsb.core", properties);
        SchemaExport export=new SchemaExport(conf.getHibernateConfiguration());
        export.setFormat(true);
        export.setOutputFile(fileName);
        export.create(false,true);
        // Got fedup trying to fix this properly to make unit tests work
        // HSQLDB or H2 compatibliity mode don't support this COLLATE so the annotation would break unit testing
        // Chain ids have to be case sensitive (chain A and a can co-exist in an entry)
        // Horrible hack
        try {
            @SuppressWarnings("deprecation")
            java.sql.Connection conn=((org.hibernate.Session)getEntityManagerFactory().createEntityManager().getDelegate()).connection();
            java.sql.Statement stat=conn.createStatement();
            stat.execute("alter table Chain modify id VARCHAR(255) COLLATE latin1_general_cs");
            stat.close();
            conn.close();
        } catch (SQLException e) {
            // This will always fail during unit testing - ignore
        }
    }

    EntityTransaction currentTransaction=null;
    private void beginTransaction() {
        if(currentTransaction==null) {
            currentTransaction=getEntityManager().getTransaction();
            currentTransaction.begin();
        }
    }

    private void commitTransaction() {
        if(currentTransaction!=null) {
            try {
                entityManager.flush();
                currentTransaction.commit();
            } catch(RuntimeException e) {
                currentTransaction.rollback();
                throw(e);
            } finally {
                currentTransaction=null;
            }
        }
    }

    private void rollbackTransaction() {
        if(currentTransaction!=null) {
            try {
                // TODO : currentTransaction.rollback();
                currentTransaction.commit();
            } finally {
                currentTransaction=null;
            }
        }
    }

    protected ContextFactory context() {
        return CoreContext.context();
    }


    protected class JpaFactory<T extends Identifiable<T,K>,K extends Comparable <K>> extends AbstractStoreFactory<T,K> {

        protected JpaFactory(Class<T> type) {
            super(type,context().getFactory(type));
        }

        @Override
        public T save(T obj, boolean override) {
            beginTransaction();
            try {
                if(!getEntityManager().contains(obj)) {
                    //System.out.println("Will persist "+obj);
                    getEntityManager().persist(obj);
                }/* else if(override) {
				System.out.println("Will merge "+obj);
			    getEntityManager().merge(obj);
			} */
            } catch(RuntimeException e) {
                System.out.println("Failed to save "+obj+" will rollback");
                rollbackTransaction();
                throw(e);
            }
            return obj;
        }

        @Override
        public T delete(T obj) {
            beginTransaction();
            try {
                getEntityManager().remove(obj);
                return obj;
            } catch(RuntimeException e) {
                System.out.println("Failed to save "+obj+" will rollback");
                rollbackTransaction();
                throw(e);
            }
        }

        @Override
        public SortedSet<K> getIdenties() {
            Function<T,K> identityFunction=new Function<T,K>() {
                @Override public K apply(T input) { return input.getIdentity(); }
            };

            Function<K,T> reverseFunction=new Function<K,T>() {
                @Override public T apply(K input) {	return get(input); }
            };

            return Collections.transformSortedSet(getAll(), identityFunction, reverseFunction);
        }

        @Override
        public SortedSet<T> getAll() {
            SortedSet<T> results=new TreeSet<T>();
            @SuppressWarnings("unchecked")
            // The reason that I had to move back to hibernate here, is that the JOIN Fetch annotations are not used by JPA QL
                    // and there is not a strong type API like hibernate. I think that this is fixed in JPA 2
                    List<T> resultList = ((org.hibernate.Session)getEntityManager().getDelegate()).createCriteria(getType()).list();
            //List<T> resultList = getEntityManager().createQuery("from "+getType().getSimpleName()).getResultList();
            results.addAll(resultList);
            return results;
        }

        @SuppressWarnings("unchecked")
        @Override
        public T get(K identity) {
            Object o=getEntityManager().find(implementation(getType()), identity);
            return (T)o;
        }
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void copy(Context context) {
        for(Class type:context.getTypes()) {
            //System.out.println("Will save Type "+type.getSimpleName());
            Access access=context.getAccess(type);
            for(Object obj:access.getAll()) {
                try {
                    //System.out.println("Saving "+type.getSimpleName()+": "+obj);
                    getFactory(type).save((Identifiable)obj, true);
                } catch(RuntimeException e) {
                    System.out.println("Failed to save "+obj+" - "+e);
                }
            }
            commitTransaction();
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void clear() {
        setEntityManager(newEntityManager());
        boolean retry=true;
        Exception lastException=null;
        for(int i=0;i<10 && retry;i++)  { // retry up to 10 times
            retry=false;
            for(Class clazz:Lists.reverse(new ArrayList<Class>(getTypes()))) {
                ArrayList allObjects=new ArrayList();
                //System.out.println("Deleting "+clazz);
                allObjects.addAll(getEntityManager().createQuery("from "+clazz.getSimpleName()).getResultList());
                for(Object o:allObjects) {
                    try {
                        Class type=context().getType(o.getClass());
                        getFactory(type).delete((Identifiable)o);
                    } catch (Exception e) {
                        lastException=e;
                        retry=true;
                    }
                }
                try {
                    commitTransaction();
                } catch (RuntimeException e) {
                    lastException=e;
                    retry=true;
                }
            }
        }
        if(retry) {
            System.out.println("Failed to clean up "+lastException);
            lastException.printStackTrace();
            throw new RuntimeException("Failed to clean up",lastException);
        } else {
            //System.out.println("Clean up worked");
        }
        setEntityManager(newEntityManager());

    }

}