package org.rcsb.uniprot.auto.dao;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.biojava3.core.util.InputStreamProvider;
import org.rcsb.uniprot.auto.or.UniProtPdbMap;
import org.rcsb.uniprot.auto.tools.JpaUtilsUniProt;

import org.rcsb.uniprot.auto.*;
import org.hibernate.Session;
import org.rcsb.uniprot.auto.tools.UniProtTools;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

public class UniprotDAOImpl implements UniprotDAO {
    static List<String> allUniprotIDs;

    static Map<String, String> uniprotNameMap;
    static SortedSet<String> geneNames;
    static Map<String, List<String>> uniprotGeneMap;
    static Map<String, List<String>> ac2geneName;
    ;

    static SortedSet<String> mopedIds;
    static AtomicBoolean busyWithInit = new AtomicBoolean(false);
    static AtomicBoolean initialized  = new AtomicBoolean(false);

    public static final String MOPED_LOCATION = "https://www.proteinspire.org/MOPED/services/referencedata/proteinNames";

    private static final boolean profiling = true;

    public static void main(String[] args) {
        UniprotDAOImpl me = new UniprotDAOImpl();
        System.out.println(me.hasPdbUniProtMapping());

        me.init();

        System.out.println("# UP ids:" + me.getAllUniProtIDs().size());
        System.out.println("P50225 header:" + me.getUniProtHeader("P50225"));

        Uniprot up = me.getUniProt("P50225");
        System.out.println(up.getEntry().get(0).getAccession().get(0));


        System.exit(0);

    }

    public static void init() {
        long timeS = System.currentTimeMillis();

        if ( initialized.get()){
            return;
        }

        while (busyWithInit.get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

                e.printStackTrace();

            }
            return;
        }

        busyWithInit.set(true);


        initAllUniprotIDs();
        long time1 = System.currentTimeMillis();
        if (profiling) {

            System.out.println("Time to initAllUniprotIDs " + (time1 - timeS));
        }
        initGeneNames();
        long time2 = System.currentTimeMillis();
        if (profiling) {

            System.out.println("Time to initGeneNames " + (time2 - time1));
        }

        initUniprotNameMap();
        long time3 = System.currentTimeMillis();
        if (profiling) {

            System.out.println("Time to initUniprotNameMap " + (time3 - time2));
        }

        initMoped();
        long time4 = System.currentTimeMillis();
        if ( profiling){

            System.out.println("Time to initMoped " + (time4 - time3));
        }


        initialized.set(true);
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
            String sql = "select distinct(HJVALUE) from entry_accession";
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

    private static void initMoped() {
        mopedIds = new TreeSet<String>();

        // fetch the list of supported Uniprot IDs from
        //http://www.proteinspire.org/MOPED/services/referencedata/proteinNames

        InputStreamProvider prov = new InputStreamProvider();

        try {

            URL u = new URL(MOPED_LOCATION);
            InputStream is = prov.getInputStream(u);


            BufferedReader dis
                    = new BufferedReader(new InputStreamReader(is));

            String line = null;
            while ((line = dis.readLine()) != null) {

                String upId = line.trim();
                if ( ! mopedIds.contains(upId))
                    mopedIds.add(upId);

            }

            System.out.println("loaded " + mopedIds.size() + " IDs from MOPED ("+MOPED_LOCATION+")");

        } catch (Exception e){

            e.printStackTrace();
        }



    }

    public SortedSet<String> getMOPEDids() {
        if ( mopedIds == null)
            init();
        return mopedIds;
    }

    @Override
    public void registerRequiredUniProtIds(SortedSet<String> requiredUniProtIDS) {

        EntityManager em = JpaUtilsUniProt.getEntityManager();

        String sql = "delete from required_ids";
        em.getTransaction().begin();
        em.createNativeQuery(sql).executeUpdate();

        StringBuffer buf = new StringBuffer();
        buf.append("insert into required_ids (uniprot_ac) values ");

        boolean first = true;
        for ( String ac: requiredUniProtIDS){
            if (UniProtTools.isValidUniProtAC(ac)){
                if ( ! first)
                    buf.append(",");
                buf.append("('");
                buf.append(ac);
                buf.append("')");
                first = false;
            }
        }

        em.createNativeQuery(buf.toString()).executeUpdate();

        em.getTransaction().commit();

        em.close();

    }

    public SortedSet<String> getRequiredUniProtIds() {
        EntityManager em = JpaUtilsUniProt.getEntityManager();

        String sql = "select distinct(uniprot_ac) from required_ids";

        SortedSet<String> ups = new TreeSet<String>();

            Query q = em.createNativeQuery(sql);
            List l = q.getResultList();
            for (Object obj : l) {

                ups.add((String) obj);

            }

        em.close();

        return ups;
    }


    public String getUniProtAcByName(String uniprotName) {

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

        EntityManager emf = JpaUtilsUniProt.getEntityManager();
        try {

            String sql = "select  ea.HJVALUE as acc , en.HJVALUE as nam from " +
                    " entry_accession ea,  " +
                    " entry_name_ en  " +
                    " where ea.HJID=en.HJID ";

            Query q = emf.createNativeQuery(sql);

            List<Object[]> l = (List<Object[]>) q.getResultList();
            Iterator<Object[]> iter = l.iterator();
            while (iter.hasNext()) {
                Object[] obj = iter.next();

                String ac = (String) obj[0];
                String name = (String) obj[1];

                uniprotNameMap.put(name, ac);

            }


        } catch (Exception e) {
            e.printStackTrace();


        }
        emf.close();
        long timeE = System.currentTimeMillis();

        if (profiling) {
            System.out.println("init uniprotName map for " + uniprotNameMap.keySet().size() + " names in : " + (timeE - timeS) + " ms.");
//            System.out.println(uniprotNameMap.get("ELNE_HUMAN"));
//            System.out.println(uniprotNameMap.get("P08246"));
        }
        return;

    }


    private static void initGeneNames() {

        geneNames = new TreeSet<String>();
        uniprotGeneMap = new TreeMap<String, List<String>>();
        ac2geneName = new TreeMap<String, List<String>>();
        long timeS = System.currentTimeMillis();

//        String sql = "select ac.element, gn.value from up_genename as gn , up_gene_up_genename gnn , up_gene g , " +
//                " up_entry_up_gene eg , up_entry e , up_uniprot_up_entry ue, up_uniprot u , up_entry_accession ac " +
//                " where gn.objId = gnn.name_objId and g.objId = gnn.up_gene_objId and eg.gene_objId = g.objId " +
//                " and e.objId = eg.up_entry_objId  and e.objId = ue.entry_objId and ue.up_uniprot_objId = u.objId " +
//                " and ac.up_entry_objId = e.objId";

        String sql ="SELECT gnt.VALUE_, ea.HJVALUE FROM genenametype gnt, genetype gt, entry e, entry_accession ea " +
                    " where e.HJID = gt.GENE_ENTRY_HJID and " +
                    " gnt.NAME__GENETYPE_HJID = gt.HJID and " +
                    " ea.HJID = e.HJID ";


        //System.out.println(sql);

        try {
            EntityManager emf = JpaUtilsUniProt.getEntityManager();

            Query q = emf.createNativeQuery(sql);


            List<Object[]> l = (List<Object[]>) q.getResultList();
            for (Object[] obj : l) {

                String ac = (String) obj[0];
                String gn = (String) obj[1];
               // System.out.println(ac + "  " +gn);
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


    public SortedSet<String> getAllGeneNames(){
        if ( geneNames == null)
            init();

        return geneNames;
    }

    public List<String> getUniProtACsByGeneName(String gn){
        if ( uniprotGeneMap == null)
            init();

        List<String> acs = uniprotGeneMap.get(gn);
        return acs;
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

            Query q = em.createQuery("select up from org.rcsb.uniprot.auto.Uniprot up where :element in elements (up.entry.accession)  ");
            q.setParameter("element", uniprotID);

            List l = q.getResultList();
            for (Object obj : l) {
                up = (Uniprot) obj;
                break;
            }

        } catch (Exception e) {
            System.err.println("Could not load UP for " +uniprotID);
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
//            Query q = em.createQuery("from org.rcsb.uniprot.auto.uniprot.Entry");
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



    public void clearPdbUniProtMapping(){

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        Query q = em.createQuery("delete from uniprotpdbmap");
        q.executeUpdate();
        em.close();

    }

    public boolean hasPdbUniProtMapping(){

        EntityManager em = JpaUtilsUniProt.getEntityManager();

        CriteriaBuilder builder = em.getCriteriaBuilder();
        CriteriaQuery<UniProtPdbMap> cQuery = builder.createQuery(UniProtPdbMap.class);
        Root<UniProtPdbMap> from = cQuery.from(UniProtPdbMap.class);
        CriteriaQuery<UniProtPdbMap>select = cQuery.select(from);

        CriteriaQuery<Long> cq = builder.createQuery(Long.class);
        cq.select(builder.count(cq.from(UniProtPdbMap.class)));

        Query countQ = em.createQuery(cq);
        //cq.where(pArray);

        List<Long> resultList = countQ.getResultList();
        boolean hasData = false;
        if (resultList.size()>0 &&  resultList.get(0) > 0 )
            hasData = true;
        em.close();

        return hasData;
    }

    public SortedSet<String> getPdbForUniProt(String accession){
        EntityManager em = JpaUtilsUniProt.getEntityManager();
        Query q = em.createQuery("Select pdbId from org.rcsb.uniprot.auto.or.UniProtPdbMap m where m.uniProtAc = :acc ");
        q.setParameter("acc", accession);
        List<String> data = q.getResultList();

        SortedSet<String> pdbs = new TreeSet<>(data);

        em.close();
        return pdbs;

    }

    public SortedSet<String> getUniProtForPDB(String pdbId){
        EntityManager em = JpaUtilsUniProt.getEntityManager();
        Query q = em.createQuery("Select pdbId from org.rcsb.uniprot.auto.or.UniProtPdbMap m where m.pdbId = :pdbId ");
        q.setParameter("pdbId", pdbId);
        List<String> data = q.getResultList();

        SortedSet<String> pdbs = new TreeSet<>(data);

        em.close();
        return pdbs;

    }

    public void addToPdbUniProtMapping(String accession, SortedSet<String> pdbIds){
        EntityManager em = JpaUtilsUniProt.getEntityManager();
        em.getTransaction().begin();
        for ( String p : pdbIds){
            UniProtPdbMap m = new UniProtPdbMap();
            m.setPdbId(p);
            m.setUniProtAc(accession);
            em.persist(m);
        }
        em.getTransaction().commit();

    }



}