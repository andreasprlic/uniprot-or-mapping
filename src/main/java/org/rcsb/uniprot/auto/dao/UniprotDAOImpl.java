package org.rcsb.uniprot.auto.dao;

import java.io.*;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.biojava3.auto.tools.JpaUtilsUniProt;

import org.biojava3.auto.uniprot.*;
import org.hibernate.Session;

import javax.persistence.EntityManager;
import javax.persistence.Query;

public class UniprotDAOImpl implements UniprotDAO {
    static List<String> allUniprotIDs;

    static Map<String, String> uniprotNameMap;
    static SortedSet<String> geneNames;
    static Map<String, List<String>> uniprotGeneMap;
    static Map<String, List<String>> ac2geneName;
    ;
    static Map<String, Integer> pdbCounts;
    static SortedSet<String> mopedIds;
    static AtomicBoolean busyWithInit = new AtomicBoolean(false);

    public static final String MOPED_LOCATION = "https://www.proteinspire.org/MOPED/services/referencedata/proteinNames";

    private static final boolean profiling = true;

    public static void main(String[] args) {
        UniprotDAOImpl me = new UniprotDAOImpl();
        me.init();
    }

    public static void init() {
        long timeS = System.currentTimeMillis();
        while (busyWithInit.get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return;
        }

        busyWithInit.set(true);


        initAllUniprotIDs();
        if (profiling) {
            long time1 = System.currentTimeMillis();
            System.out.println("Time to initAllUniprotIDs " + (time1 - timeS));
        }
        initGeneNames();
        if (profiling) {
            long time1 = System.currentTimeMillis();
            System.out.println("Time to initGeneNames " + (time1 - timeS));
        }

        initUniprotNameMap();
        if (profiling) {
            long time1 = System.currentTimeMillis();
            System.out.println("Time to initUniprotNameMap " + (time1 - timeS));
        }

        busyWithInit.set(false);

        long timeE = System.currentTimeMillis();
        System.out.println("Time to init UniprotDAO: " + (timeE - timeS));
    }

    private static List<String> initAllUniprotIDs() {
        long timeS = System.currentTimeMillis();
        allUniprotIDs = new ArrayList<String>();
        List<String> ups = new ArrayList<String>();
        Session sess = null;
        try {
            EntityManager entityManager = JpaUtilsUniProt.getEntityManager();
            String sql = "select distinct(element) from up_entry_accession";
            Query q = entityManager.createNativeQuery(sql);
            List l = q.getResultList();
            for (Object obj : l) {

                //org.rcsb.external.uniprot.Entry upentry = (org.rcsb.external.uniprot.Entry) obj;

                ups.add((String) obj);

            }
        } catch (Exception e) {
            e.printStackTrace();

        } finally {
            if (sess != null)
                sess.close();
        }
        allUniprotIDs = ups;

        long timeE = System.currentTimeMillis();

        System.out.println("time required to initialize all " + allUniprotIDs.size() + " uniprot IDs: " + (timeE - timeS));
        return ups;
    }


    public String getUniProtAcByName(String uniprotName) {

        System.out.println("Get UNIPROT AC BY NAME: " + uniprotName);

        if (uniprotNameMap == null) {
            init();
        }

        return uniprotNameMap.get(uniprotName);


    }

    public String checkForPrimaryAccession(String uniprotAccession) {


        Uniprot up = getUniProt(uniprotAccession );

        if (up == null)
            return null;

        for (Entry e : up.getEntry()) {
            List<String> accessions = e.getAccession();
            if (accessions.contains(uniprotAccession))
                return accessions.get(0);
        }


        return null;
    }


    private static void initUniprotNameMap() {
        uniprotNameMap = new TreeMap<String, String>();
        long timeS = System.currentTimeMillis();


        try {
            EntityManager emf = JpaUtilsUniProt.getEntityManager();

            String sql = "select  acc.element  , nam.element  from " +
                    " up_entry_accession acc,  " +
                    " up_entry_name nam  " +
                    " where acc.up_entry_objId = nam.up_entry_objId";

//            String hql = "select e.accession from " +
//                    " org.rcsb.external.uniprot.Entry as e   " ;
//                  ;

            //String hql = "select e.name, e.accession from org.rcsb.external.uniprot.Entry as e";

            Query q = emf.createNativeQuery(sql);
            // Query q = sess.createQuery(hql);


            List<Object[]> l = (List<Object[]>) q.getResultList();
            Iterator<Object[]> iter = l.iterator();
            while (iter.hasNext()) {
                Object[] obj = iter.next();
                System.out.println(Arrays.toString(obj) + " " + obj[0].getClass().getName() + " " + obj.length);
                String ac = (String) obj[0];
                String name = (String) obj[1];
                System.out.println(name + " " + ac);
                uniprotNameMap.put(name, ac);

            }


        } catch (Exception e) {
            e.printStackTrace();


        }

        long timeE = System.currentTimeMillis();

        if (profiling) {
            System.out.println("init uniprotName map for " + uniprotNameMap.keySet().size() + " names in : " + (timeE - timeS) + " ms.");
            System.out.println(uniprotNameMap.get("ELNE_HUMAN"));
            System.out.println(uniprotNameMap.get("P08246"));
        }
        return;

    }


    private static void initGeneNames() {

        geneNames = new TreeSet<String>();
        uniprotGeneMap = new TreeMap<String, List<String>>();
        ac2geneName = new TreeMap<String, List<String>>();
        long timeS = System.currentTimeMillis();

        String sql = "select ac.element, gn.value from up_genename as gn , up_gene_up_genename gnn , up_gene g , " +
                " up_entry_up_gene eg , up_entry e , up_uniprot_up_entry ue, up_uniprot u , up_entry_accession ac " +
                " where gn.objId = gnn.name_objId and g.objId = gnn.up_gene_objId and eg.gene_objId = g.objId " +
                " and e.objId = eg.up_entry_objId  and e.objId = ue.entry_objId and ue.up_uniprot_objId = u.objId " +
                " and ac.up_entry_objId = e.objId";

        //System.out.println(sql);

        try {
            EntityManager emf = JpaUtilsUniProt.getEntityManager();

            Query q = emf.createNativeQuery(sql);


            List<Object[]> l = (List<Object[]>) q.getResultList();
            for (Object[] obj : l) {

                String ac = (String) obj[0];
                String gn = (String) obj[1];

                if (gn == null || ac == null)
                    continue;

                if (!geneNames.contains(gn)) {
                    geneNames.add(gn);
                }

                List<String> acs = uniprotGeneMap.get(gn);
                if (acs == null) {
                    acs = new ArrayList<String>();
                    uniprotGeneMap.put(gn, acs);
                }
                acs.add(ac);

                List<String> gns = ac2geneName.get(ac);
                if (gns == null) {
                    gns = new ArrayList<String>();
                    ac2geneName.put(ac, gns);
                }
                gns.add(gn);

            }


        } catch (Exception e) {
            e.printStackTrace();


        }
        long timeE = System.currentTimeMillis();

        System.out.println("time to init " + geneNames.size() + " gene names for uniprot: " + (timeE - timeS) + " ms. ");

    }


    public List<String> getAllUniProtIDs() {
        if (allUniprotIDs == null) {
            init();
        }

        return allUniprotIDs;
    }

    public List<String> getGeneNames(String uniprotID) {
        if (ac2geneName == null)
            init();

        return ac2geneName.get(uniprotID);
    }


    public Map<String, Integer> getAllPDBCounts() {
        if (pdbCounts == null)
            init();

        return pdbCounts;
    }


    public SortedSet<String> getMOPEDids() {
        if (mopedIds == null)
            init();
        return mopedIds;
    }


    public int getLength(String uniprotAccession) {


//        String sql = "select ea.element,  us.length  " +
//                " from up_entry_accession ea, up_sequence  us, up_entry ue, up_entry_sequence ues " +
//                " where ea.element =? and ea.up_entry_objId = ue.objId " +
//                " and ues.objId = ue.objId and ues.sequence_objId = us.objId   ";

        int length = -1;

        try {
//            EntityManager emf = JpaUtilsUniProt.getEntityManager();
//
//            Query q = emf.createNativeQuery(sql);
//
//            q.setParameter(0, uniprotID);
//
//            List<Object[]> l = (List<Object[]>) q.getResultList();
//            for (Object[] obj : l) {
//                //String ac = (String)obj[0];
//                BigDecimal d = (BigDecimal) obj[1];
//                if (d != null)
//                    length = d.intValue();
//
//
//            }


            Uniprot up = getUniProt(uniprotAccession);
            length = up.getEntry().get(0).getSequence().getLength();


        } catch (Exception e) {
            e.printStackTrace();


        }

        return length;
    }


    public  String cleanSequence(String sequence) {
        sequence = sequence.trim();
        sequence = sequence.replaceAll("\\s+", "");
        sequence = sequence.replaceAll("\\r\\n|\\r|\\n", "");
        return sequence;
    }



    public  synchronized Uniprot getUniProt(String uniprotID, EntityManager em) {
        Uniprot up = null;
        try {
            @SuppressWarnings("JpaQlInspection")
            Query q = em.createQuery("from org.biojava3.auto.uniprot.Uniprot up where :val in elements  (up.entry.accession)  ");
            q.setParameter("val", uniprotID);

            List l = q.getResultList();
            for (Object obj : l) {
                up = (Uniprot) obj;
                break;
            }

        } catch (Exception e) {
            e.printStackTrace();

        }

        long timeE = System.currentTimeMillis();

        //System.out.println("  TrackTools took " + (timeE-timeS) + " ms. to load " + uniprotID);
        // note: we don;t close the session here because the outside will request specific details from the Uniprot object
        return up;

    }
    public  synchronized Uniprot getUniProt(String uniprotID) {

        long timeS = System.currentTimeMillis();
        //	System.out.println(" TrackTools: LOADING " + uniprotID + " FROM DB");


        Uniprot up = null;
        try {
            EntityManager em = JpaUtilsUniProt.getEntityManager();


            return getUniProt(uniprotID,
                    em);



        } catch (Exception e) {
            e.printStackTrace();

        }

        long timeE = System.currentTimeMillis();

        //System.out.println("  TrackTools took " + (timeE-timeS) + " ms. to load " + uniprotID);
        // note: we don;t close the session here because the outside will request specific details from the Uniprot object
        return up;
    }






    public String getDescription(String uniprotID) {

        String header = null;

        header = getUniProtHeader(uniprotID);

        return header;
    }

    public String getUniProtHeader(String uniprotAccession){
        String header = null;


        Uniprot up = getUniProt(uniprotAccession);
        if (up == null) {
            return "Unknown";
        }
        StringWriter h = new StringWriter();
        for (Entry e : up.getEntry()) {
            h.append(getUniprotName(e));


            List<String> acs = e.getAccession();
            if (acs != null && acs.size() > 0) {
                h.append(" - ");
                h.append(acs.get(0));
            }

            List<String> names = e.getName();
            if (names != null && names.size() > 0) {
                h.append(" (");
                h.append(names.get(0));
                h.append(")");
            }
            break;
        }
        header = h.toString();


        return header;
    }

    public  String getUniprotName(Entry e) {
        String desc = null;
        RecommendedName name = e.getProtein().getRecommendedName();

        if ( name != null)
            desc = name.getFullName().getValue().toString();

        if (desc == null) {
            List<AlternativeName> anames = e.getProtein().getAlternativeName();
            for (AlternativeName a : anames) {
                desc = a.getFullName().getValue().toString();
            }
        }

        if (desc == null) {
            List<SubmittedName> snames = e.getProtein().getSubmittedName();
            for (SubmittedName s : snames) {
                if (s.getFullName().getValue() != null)
                    desc = s.getFullName().getValue().toString();
            }
        }
        return desc;
    }

    public SortedMap<String, Integer> getDbVersions() {
        SortedMap<String, Integer> ans = new TreeMap<String, Integer>();
        // String sql = "select element, CEILING(version) from up_entry_accession ea inner join up_entry e on e.objid =
        // ea.up_entry_objid";



        String sql = "select a.hjvalue, en.version_ from entry_accession as a, entry as en where en.HJID = a.HJID";

        System.out.println("loading DB versions ");

        Integer version = null;
        List<String> accList = null;
        Iterator<String> it2 = null;
        try {
            EntityManager em = JpaUtilsUniProt.getEntityManager();

            @SuppressWarnings("JpaQlInspection")
//            Query q = em.createQuery("from org.biojava3.auto.uniprot.Entry");
                    Query q = em.createNativeQuery(sql);

            List<Object[]> li = q.getResultList();
            for (Object[] o : li) {

                ans.put((String) o[0], (Integer) o[1]);

//                Entry anEnt = (Entry) li.next();
//                accList = anEnt.getAccession();
//                version = anEnt.getVersion();
//                if (accList != null && version != null) {
//                    it2 = accList.iterator();
//                    while (it2.hasNext()) {
//                        String anAcc = (String) it2.next();
//                        ans.put(anAcc, version.toString());
//                    }
//                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return ans;
    }
}