package org.rcsb.uniprot.auto.dao;

import java.io.*;
import java.math.BigInteger;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.biojava.nbio.core.util.InputStreamProvider;
import org.biojava.nbio.core.util.SoftHashMap;
import org.hibernate.HibernateException;
import org.rcsb.uniprot.auto.or.UniProtPdbMap;

import org.rcsb.uniprot.auto.tools.JpaUtilsUniProt;

import org.rcsb.uniprot.auto.*;
import org.rcsb.uniprot.auto.tools.UniProtTools;
import org.rcsb.uniprot.config.RCSBUniProtMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import javax.xml.bind.JAXBException;


public class UniprotDAOImpl implements UniprotDAO {

    private static final Logger logger = LoggerFactory.getLogger(UniprotDAOImpl.class);

    private  static List<String> allUniprotIDs;

    private static Map<String, String> uniprotNameMap = null;
    private static SortedSet<String> geneNames = null;
    private static Map<String, List<String>> uniprotGeneMap = null;
    private static Map<String, List<String>> uniprotGenePrimaryAccessionMap = null;
    private static Map<String, List<String>> ac2geneName;

    private static Map<String,String> organismNameMap = null;
    private static Map<String,List<String>> organismAcMap = null;
    private static Map<String,String> diseaseMap = null;
    private static Map<String,Integer> sequenceLengthsMap = null;
    private static Map<String,String> displayNameMap = null;

    private static SortedSet<String> mopedIds;
    private static AtomicBoolean busyWithInit = new AtomicBoolean(false);
    private static AtomicBoolean initialized  = new AtomicBoolean(false);

    public static final String MOPED_LOCATION = "http://www.proteinspire.org/MOPED/services/referencedata/proteinNames";

    public UniprotDAOImpl (){
        UniprotDAOImpl.init();
    }

    public static void main(String[] args) {

        UniprotDAOImpl me = new UniprotDAOImpl();

        logger.info("all recommended name map size :" + me.getRecommendedNameMap().size());

        System.out.println("all alternative name map size :" + me.getAlternativeNameMap().size());

//        for (Object[] data: me.getPdbReferencesFromUniProt() ) {
//            System.out.println(Arrays.toString(data));
//        }

        System.out.println(me.getShortNameMap().get("B6WQE2"));


        SortedSet<String> acs = new TreeSet<>();
        acs.add("B6WQE2");
        System.out.println(me.getAC2NameMap(acs));


        //System.out.println("All gene names:" + me.getAllGeneNames().size());


//        for (Object[] data: me.getRecommendedNames4Components() ) {
//
//            if ( data[0].equals("Q8V5E0")) {
//                System.out.println(Arrays.toString(data));
//
//            }
//            // System.out.println(Arrays.toString(data));
//        }
//
//
//        System.out.println(me.hasPdbUniProtMapping());
//
//        for (Object[] data: me.getECNumbers4Components()) {
//            System.out.println("EC nuymber for components:" + Arrays.toString(data));
//        }
        System.exit(0);

        for (Object[] data: me.getECNumbers()) {
            System.out.println(Arrays.toString(data));
        }

        System.out.println("# UP ids:" + me.getAllUniProtIDs().size());

        EntityManager em = JpaUtilsUniProt.getEntityManager();

        System.out.println("P50225 header:" + me.getUniProtHeader(em,"P50225"));


        Uniprot up = me.getUniProt(em, "P50225");
        System.out.println(up.getEntry().get(0).getAccession().get(0));
        em.close();
        System.out.println(me.getAllUniProtIDs().size());

        System.exit(0);

    }

    public static void init() {

        if ( initialized.get()){
            return;
        }


        long timeS = System.currentTimeMillis();



        while (busyWithInit.get()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {

                logger.error(e.getMessage(),e);

            }
            return;
        }

        busyWithInit.set(true);

        initAllUniprotIDs();

        long time1 = System.currentTimeMillis();


        logger.debug("Time to initAllUniprotIDs " + (time1 - timeS));


        // new
        initDisplayNameMap();

        initOrganisms();

        initDiseases();

        initSequenceLengths();

        initGeneNames();

        initPrimaryAccessions();

        long time2 = System.currentTimeMillis();

        logger.debug("Time to initGeneNames " + (time2 - time1));


        initUniprotNameMap();
        long time3 = System.currentTimeMillis();

        logger.debug("Time to initUniprotNameMap " + (time3 - time2));

        initMoped();
        long time4 = System.currentTimeMillis();

        logger.debug("Time to initMoped " + (time4 - time3));

//        initComponents();
//        long time5 = System.currentTimeMillis();
//        if ( profiling){
//            System.out.println("Time to init UniProt components: " + (time5 - time4));
//        }


        initialized.set(true);
        busyWithInit.set(false);

        long timeE = System.currentTimeMillis();
        logger.info("Time to init UniprotDAO: " + (timeE - timeS));

    }

    public List<Object[]> getRecommendedNames4Components() {


        // get recommended names for components and start, stop positions..


        String sql = "select  a.hjvalue,est.value_, bp.POSITION_ as ps, ep.POSITION_ as es "+
                "from entry_accession a "+
                "join entry en on en.HJID = a.HJID  " +
                "join component c on c.component_protein_type_hjid= en.PROTEIN_ENTRY_HJID "+
                //"join proteintype p on p.HJID = en.PROTEIN_ENTRY_HJID "+
                //"join evidencedstringtype est on  est.shortname_RECOMMENDEDNAME_HJ_0 = p.RECOMMENDEDNAME_PROTEINTYPE__0 " +
                //"join evidencedstringtype est on  est.shortname_RECOMMENDEDNAME_HJ_0 = en.PROTEIN_ENTRY_HJID  " +
                "join evidenced_string_type est on  est.short_name_RECOMMENDED_NAME__0 = c.recommended_name_component_h_0  " +
                "join feature_type f on f.FEATURE_ENTRY_HJID=en.HJID "+
                "join location_type l on l.HJID=f.LOCATION__FEATURE_TYPE_HJID " +
                "join position_type bp on bp.HJID = l.BEGIN__LOCATION_TYPE_HJID " +
                "join position_type ep on ep.HJID = l.END__LOCATION_TYPE_HJID ";



        EntityManager em = JpaUtilsUniProt.getEntityManager();

        Query q = em.createNativeQuery(sql);
        List<Object[]> data = new ArrayList<Object[]>();
        List<Object[]> rows = q.getResultList();
        data.addAll(rows);
        em.close();

        return data;
    }

    private static List<String> initAllUniprotIDs() {
        long timeS = System.currentTimeMillis();
        allUniprotIDs = new ArrayList<String>();
        List<String> ups = new ArrayList<String>();
        EntityManager entityManager = null;

        try {
            entityManager = JpaUtilsUniProt.getEntityManager();
            String sql = "select distinct(HJVALUE) from entry_accession";
            Query q = entityManager.createNativeQuery(sql);
            List l = q.getResultList();
            for (Object obj : l) {

                //org.rcsb.external.uniprot.Entry upentry = (org.rcsb.external.uniprot.Entry) obj;

                ups.add((String) obj);

            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);

        } finally {
            if (entityManager !=null) entityManager.close();
        }
        allUniprotIDs = ups;

        long timeE = System.currentTimeMillis();

        logger.info("time required to initialize all " + allUniprotIDs.size() + " uniprot IDs: " + (timeE - timeS));
        return ups;
    }

    private static void initMoped() {
        mopedIds = new TreeSet<>();

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

            logger.info("loaded " + mopedIds.size() + " IDs from MOPED ("+MOPED_LOCATION+")");

        } catch (Exception e){
            logger.error("Could not load moped data from {}, error: {}", MOPED_LOCATION, e.getMessage());

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

    @Override
    public List<Object[]> getECNumbers() {

        // check the EC assignments in
        //"recommended","alternative","submitted" name


// FOR DEBUGGING

//        String sql = "select a.hjvalue, est.value_  " +
//                " from entry_accession as a, entry as en , proteintype as p , " +
//                " recommendedname as r , evidencedstringtype est " +
//                " where en.HJID = a.HJID and p.HJID = en.PROTEIN_ENTRY_HJID and  "  +
//                " p.RECOMMENDEDNAME_PROTEINTYPE__0 = r.HJID and "+
//                " est.ECNUMBER_RECOMMENDEDNAME_HJID = r.HJID and " +
//                " a.HJVALUE='P50225'";

        // CAN BE SIMPLIFIED TO

        List<Object[]> data = new ArrayList<Object[]>();

        // only selects this for UniProts that are linked to PDB...

        String sql = "select a.hjvalue, est.value_  " +
                " from entry_accession as a, entry as en , protein_type as p , " +
                "  evidenced_string_type est , uniprotpdbmap updb " +
                " where updb.uniProtAc = a.HJVALUE and en.HJID = a.HJID and p.HJID = en.PROTEIN_ENTRY_HJID and  " +
                " p.RECOMMENDED_NAME_PROTEIN_TYP_0 = est.EC_NUMBER_RECOMMENDED_NAME_H_0 " +
                " group by a.hjvalue,est.value_ having count(*) > 1";

        EntityManager em = JpaUtilsUniProt.getEntityManager();

        Query q = em.createNativeQuery(sql);

        List<Object[]> rows = q.getResultList();
        data.addAll(rows);
        em.close();


        String sql2 = "select a.hjvalue, est.value_  " +
                " from entry_accession as a, entry as en , " +
                "  evidenced_string_type est , uniprotpdbmap updb , submitted_name sn " +
                " where updb.uniProtAc = a.HJVALUE and en.HJID = a.HJID and" +
                " sn.SUBMITTED_NAME_PROTEIN_TYPE__0 = en.PROTEIN_ENTRY_HJID and " +
                " sn.SUBMITTED_NAME_PROTEIN_TYPE__0 = est.EC_NUMBER_SUBMITTED_NAME_HJID " +
                " group by a.hjvalue,est.value_ having count(*) > 1";

        em = JpaUtilsUniProt.getEntityManager();

        Query q2 = em.createNativeQuery(sql2);

        List<Object[]> rows2 = q2.getResultList();
        data.addAll(rows2);
        em.close();

        String sql3 = "select a.hjvalue, est.value_  " +
                " from entry_accession as a, entry as en , " +
                "  evidenced_string_type est , uniprotpdbmap updb , alternative_name an " +
                " where updb.uniProtAc = a.HJVALUE and en.HJID = a.HJID and  " +
                " an.ALTERNATIVE_NAME_PROTEIN_TYP_0 = en.PROTEIN_ENTRY_HJID and " +
                " an.ALTERNATIVE_NAME_PROTEIN_TYP_0 = est.EC_NUMBER_ALTERNATIVE_NAME_H_0 " +
                " group by a.hjvalue,est.value_ having count(*) > 1";


        em = JpaUtilsUniProt.getEntityManager();

        Query q3 = em.createNativeQuery(sql3);

        List<Object[]> rows3 = q3.getResultList();
        data.addAll(rows3);
        em.close();

        return data;
    }

    public List<Object[]> getECNumbers4Components(){

        String sql = "select  a.hjvalue,est.value_, bp.POSITION_ as ps, ep.POSITION_ as es "+
                "from entry_accession a "+
                "join entry en on en.HJID = a.HJID  " +
                "join component c on c.component_protein_type_hjid= en.PROTEIN_ENTRY_HJID "+
                "join protein_type p on p.HJID = en.PROTEIN_ENTRY_HJID "+
                "join evidenced_string_type est on  est.EC_NUMBER_RECOMMENDED_NAME_H_0 = p.RECOMMENDED_NAME_PROTEIN_TYP_0 " +
                "join feature_type f on f.FEATURE_ENTRY_HJID=en.HJID "+
                "join location_type l on l.HJID=f.LOCATION__FEATURE_TYPE_HJID " +
                "join position_type bp on bp.HJID = l.BEGIN__LOCATION_TYPE_HJID " +
                "join position_type ep on ep.HJID = l.END__LOCATION_TYPE_HJID ";



        EntityManager em = JpaUtilsUniProt.getEntityManager();

        Query q = em.createNativeQuery(sql);
        List<Object[]> data = new ArrayList<Object[]>();
        List<Object[]> rows = q.getResultList();
        data.addAll(rows);
        em.close();

        return data;

    }



    public String checkForPrimaryAccession(String uniprotAccession) {
        if ( organismAcMap == null)
            init();

        // the map has only primary accessions as key..
        if ( organismAcMap.keySet().contains(uniprotAccession))
            return uniprotAccession;


        EntityManager em = JpaUtilsUniProt.getEntityManager();
        Uniprot up = getUniProt(em, uniprotAccession);

        if (up == null) {
            em.close();
            return null;
        }

        for (Entry e : up.getEntry()) {
            List<String> accessions = e.getAccession();
            if (accessions.contains(uniprotAccession)) {
                em.close();
                return accessions.get(0);
            }
        }

        em.close();
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
            logger.error("Could not initialise the UniProt name map. Error: {}", e.getMessage());


        }
        emf.close();
        long timeE = System.currentTimeMillis();


        logger.debug("init uniprotName map for " + uniprotNameMap.keySet().size() + " names in : " + (timeE - timeS) + " ms.");
//      System.out.println(uniprotNameMap.get("ELNE_HUMAN"));
//      System.out.println(uniprotNameMap.get("P08246"));


    }



    private static void initOrganisms(){

        organismNameMap = new HashMap<String,String>();
        organismAcMap   = new HashMap<String,List<String>>();

        String sql = "SELECT ot.name__organism_type_hjid, ot.type_, ot.value_, ea.HJVALUE " +
                " FROM organism_name_type ot, entry_accession ea , entry e " +
                " where ot.name__organism_type_hjid = e.organism_entry_hjid and e.HJID = ea.HJID";

        long timeS = System.currentTimeMillis();

        EntityManager emf = null;

        try {
             emf = JpaUtilsUniProt.getEntityManager();

            Query q = emf.createNativeQuery(sql);


            List<Object[]> l = (List<Object[]>) q.getResultList();


            logger.debug("Got " + l.size() + " uniprot organism mappings");

            Map<BigInteger,String> orgIdx = new HashMap<BigInteger,String>();

            for (Object[] obj : l) {

                BigInteger i   = (BigInteger) obj[0];
                String type = (String) obj[1];
                String name = (String) obj[2];
                String ac   = (String) obj[3];

                if (type.equals("scientific") ){
                    orgIdx.put(i, name);
                    if (! organismAcMap.containsKey(ac)) {
                        List<String> d = new ArrayList<String>();
                        d.add(name);
                        organismAcMap.put(ac,d);

                    } else {
                        List<String> d = organismAcMap.get(ac);
                        if ( ! d.contains(name))
                            d.add(name);
                        //if ( ! organismAcMap.get(ac).equals(name))
                        //System.err.println("ORGANISM MAP ALREADY HAS: " + organismAcMap.get(ac) + " new: " + name + " | " + ac);
                    }
                } else if (type.equals("common")){
                    String scientific = orgIdx.get(i);
                    if ( scientific != null && (! organismNameMap.containsKey(scientific))) {
                        organismNameMap.put(scientific,name);
                    }

                }

            }


        } catch (Exception e) {
            logger.error("Could not initialise the organism map. Error: {}", e.getMessage());


        } finally {
            if (emf!=null) emf.close();
        }
        long timeE = System.currentTimeMillis();


        logger.info("Time to init " + organismNameMap.keySet().size() + " entries in organism map (" + organismAcMap.keySet().size()+" ACs) : " + (timeE - timeS) + " ms.");


    }

    private static void initDiseases(){

        diseaseMap = new HashMap<String,String>();
        EntityManager emf = null;

        String sql = "select distinct d.acronym, d.name_ from comment_type ct " +
                " join disease d on d.hjid = ct.disease_comment_type_hjid " +
                " where type_ = 'disease' ";

        long timeS = System.currentTimeMillis();
        try {
            emf = JpaUtilsUniProt.getEntityManager();

            Query q = emf.createNativeQuery(sql);


            List<Object[]> l = (List<Object[]>) q.getResultList();


            for (Object[] obj : l) {

                String acronym = String.valueOf(obj[0]);
                String name = String.valueOf(obj[1]);

                if(!diseaseMap.containsKey(acronym)){
                    diseaseMap.put(acronym, name);
                }

            }

        } catch (Exception e) {
           logger.error("Could not initialise the disease map. Error: {}", e.getMessage());

        }finally{
            if (emf!=null) emf.close();
        }
        long timeE = System.currentTimeMillis();

        logger.debug("Time to init " + diseaseMap.keySet().size() + " entries in disease map: " + (timeE - timeS) + " ms.");


    }

    private static void initSequenceLengths(){

        sequenceLengthsMap = new HashMap<String,Integer>();
        EntityManager emf = null;

        String sql = " select  updb.uniProtAc, st.length_ " +
                "from entry en " +
                "join entry_accession a  on en.HJID = a.HJID " +
                "join uniprotpdbmap updb on updb.uniProtAc = a.HJVALUE " +
                "join sequence_type st on st.hjid = en.sequence__entry_hjid ";



        long timeS = System.currentTimeMillis();
        try {
            emf = JpaUtilsUniProt.getEntityManager();

            Query q = emf.createNativeQuery(sql);


            List<Object[]> l = (List<Object[]>) q.getResultList();


            for (Object[] obj : l) {

                String uniprotId = String.valueOf(obj[0]);
                int length = -1;
                try {
                    length = Integer.parseInt(String.valueOf(obj[1]));
                } catch (NumberFormatException e) {
                    length = -1;
                }
                if(!sequenceLengthsMap.containsKey(uniprotId)){
                    sequenceLengthsMap.put(uniprotId, length);
                }

            }

        } catch (Exception e) {
            logger.error("Could not initialise the sequence lengths map. Error: {}", e.getMessage());

        }finally{
            if (emf!=null) emf.close();
        }
        long timeE = System.currentTimeMillis();

        logger.debug("Time to init " + sequenceLengthsMap.keySet().size() + " entries in sequence lengths map: " + (timeE - timeS) + " ms.");


    }

    private static void initDisplayNameMap(){

        long timeS = System.currentTimeMillis();
        displayNameMap = new HashMap<String,String>();

        Set<String> uniProtIdSet = new HashSet<String>();
        Set<String> missingNamesSet = new HashSet<String>();
        uniProtIdSet.addAll(allUniprotIDs);

        if(uniProtIdSet == null || uniProtIdSet.size() == 0){
            return ;
        }

        //first: check recommended names
        Map<String,String> recommendedNameMap = UniProtDAOUtil.getRecommendedNameMap(null);

        for(String uniProtId : uniProtIdSet){
            if(recommendedNameMap.containsKey(uniProtId)) {
                displayNameMap.put(uniProtId,recommendedNameMap.get(uniProtId));
            }else{
                missingNamesSet.add(uniProtId);
            }
        }

        //second: check alternative names
        Map<String,List<String>> alternativeNamesMap = UniProtDAOUtil.getAlternativeNameMap(null);


        if(missingNamesSet != null && missingNamesSet.size() > 0){
            for (Iterator<String> i = missingNamesSet.iterator(); i.hasNext();) {
                String uniProtId = i.next();
                if(alternativeNamesMap.containsKey(uniProtId)){
                    List<String> altNamesList =  alternativeNamesMap.get(uniProtId);
                    if(altNamesList != null && altNamesList.size() > 0 && StringUtils.isNotBlank(altNamesList.get(0))){
                        displayNameMap.put(uniProtId,altNamesList.get(0));
                        i.remove();
                    }
                }
            }
        }

        //third: check submitted names
        Map<String,String> submittedNamesMap = UniProtDAOUtil.getSubmittedNameMap(null);

        if(missingNamesSet != null && missingNamesSet.size() > 0){
            for (Iterator<String> i = missingNamesSet.iterator(); i.hasNext();) {
                String uniProtId = i.next();
                if(submittedNamesMap.containsKey(uniProtId) && StringUtils.isNotBlank(submittedNamesMap.get(uniProtId))){
                    displayNameMap.put(uniProtId,submittedNamesMap.get(uniProtId));
                    i.remove();
                }
            }
        }

        long timeE = System.currentTimeMillis();

        logger.debug("Time to init " + displayNameMap.keySet().size() + " entries in descriptions map: " + (timeE - timeS) + " ms.");


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

        String sql ="SELECT gnt.VALUE_, ea.HJVALUE FROM gene_name_type gnt, gene_type gt, entry e, entry_accession ea " +
                " where e.HJID = gt.GENE_ENTRY_HJID and " +
                " gnt.NAME__GENE_TYPE_HJID = gt.HJID and " +
                " ea.HJID = e.HJID ";


        //System.out.println(sql);

        EntityManager emf = null;

        try {
             emf = JpaUtilsUniProt.getEntityManager();

            Query q = emf.createNativeQuery(sql);


            List<Object[]> l = (List<Object[]>) q.getResultList();

            logger.info("Got " + l.size() + " gene to UP mappings");
            for (Object[] obj : l) {

                String gn = (String) obj[0];
                String ac = (String) obj[1];

                // this checks if this is a primary DB identifier!
                if (! organismAcMap.containsKey(ac)){
                    continue;
                }


                // System.out.println(ac + "  " +gn);
//                if ( gn.equalsIgnoreCase("HBA1"))
//                    System.out.println(gn + " : " +ac);

                if (gn == null || ac == null)
                    continue;

                if (!geneNames.contains(gn)) {
                    geneNames.add(gn);
                }

                List<String> acs = uniprotGeneMap.get(gn.toUpperCase());
                if (acs == null) {
                    acs = new ArrayList<String>();
                    uniprotGeneMap.put(gn.toUpperCase(), acs);
                }
                acs.add(ac);

                List<String> gns = ac2geneName.get(ac);
                if (gns == null) {
                    gns = new ArrayList<String>();
                    ac2geneName.put(ac, gns);
                }
                if ( ! gns.contains(gn))
                    gns.add(gn);

            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);


        } finally {
            if (emf!=null) emf.close();
        }
        long timeE = System.currentTimeMillis();

        logger.info("time to init " + geneNames.size() + " gene names for uniprot: " + (timeE - timeS) + " ms. ");

    }

    private static void initPrimaryAccessions() {

        uniprotGenePrimaryAccessionMap = new TreeMap<String, List<String>>();

        long timeS = System.currentTimeMillis();

        String sql ="SELECT gnt.VALUE_, ea.HJVALUE FROM gene_name_type gnt, gene_type gt, entry e, entry_accession ea " +
                " where e.HJID = gt.GENE_ENTRY_HJID and " +
                " gnt.NAME__GENE_TYPE_HJID = gt.HJID and " +
                " ea.HJID = e.HJID and ea.hjindex = 0 ";

        EntityManager emf = null;

        try {
            emf = JpaUtilsUniProt.getEntityManager();

            Query q = emf.createNativeQuery(sql);


            List<Object[]> l = (List<Object[]>) q.getResultList();

            logger.info("Got " + l.size() + " gene to UP primary accession mappings");
            for (Object[] obj : l) {

                String geneName = (String) obj[0];
                String primaryAccession = (String) obj[1];

                // this checks if this is a primary DB identifier!
                if (! organismAcMap.containsKey(primaryAccession)){
                    continue;
                }

                if (geneName == null || primaryAccession == null)
                    continue;

                List<String> acs = uniprotGenePrimaryAccessionMap.get(geneName.toUpperCase());
                if (acs == null) {
                    acs = new ArrayList<String>();
                }
                acs.add(primaryAccession);
                uniprotGenePrimaryAccessionMap.put(geneName.toUpperCase(), acs);

            }

        } catch (Exception e) {
            logger.error(e.getMessage(),e);


        } finally {
            if (emf!=null) emf.close();
        }
        long timeE = System.currentTimeMillis();

        logger.info("time to init " + geneNames.size() + " gene names for uniprot: " + (timeE - timeS) + " ms. ");

    }


    public SortedSet<String> getAllGeneNames(){
        if ( geneNames == null)
            init();

        while ( busyWithInit.get()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
        }

        return geneNames;
    }

    public List<String> getUniProtACsByGeneName(String gn){
        if ( uniprotGeneMap == null)
            init();

        while ( busyWithInit.get()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
        }

        List<String> acs = uniprotGeneMap.get(gn.toUpperCase());
        return acs;
    }

    public List<String> getUniProtPrimaryACsByGeneName(String gn){
        if ( uniprotGenePrimaryAccessionMap == null)
            init();

        while ( busyWithInit.get()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
        }

        List<String> acs = uniprotGenePrimaryAccessionMap.get(gn.toUpperCase());
        return acs;
    }

    public List<String> getAllUniProtIDs() {
        if (allUniprotIDs == null) {
            init();
        }

        while ( busyWithInit.get()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
        }

        return allUniprotIDs;
    }

    public List<String> getGeneNames(String uniprotID) {
        if (ac2geneName == null)
            init();

        while ( busyWithInit.get()){
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(),e);
            }
        }


        return ac2geneName.get(uniprotID);
    }


    public int getLength(String uniprotAccession) {


//        String sql = "select ea.element,  us.length  " +
//                " from up_entry_accession ea, up_sequence  us, up_entry ue, up_entry_sequence ues " +
//                " where ea.element =? and ea.up_entry_objId = ue.objId " +
//                " and ues.objId = ue.objId and ues.sequence_objId = us.objId   ";

        int length = -1;

        EntityManager em = null;

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

            em = JpaUtilsUniProt.getEntityManager();

            Uniprot up = getUniProt(em, uniprotAccession);
            if ( up == null) {
                length = -1;
            } else {
                length = up.getEntry().get(0).getSequence().getLength();
            }


        } catch (Exception e) {
            logger.error(e.getMessage(),e);


        } finally {
            if (em!=null) em.close();
        }

        return length;
    }


    public  String cleanSequence(String sequence) {
        sequence = sequence.trim();
        sequence = sequence.replaceAll("\\s+", "");
        sequence = sequence.replaceAll("\\r\\n|\\r|\\n", "");
        return sequence;
    }


    private static SoftHashMap<String,Uniprot> softCache = new SoftHashMap<String,Uniprot>();
    public  synchronized Uniprot getUniProt(EntityManager em,String uniprotID) {

        if (uniprotID==null) return null;

        if (uniprotNameMap == null )
            init();

        long timeS = System.currentTimeMillis();

        // step 1: see if the entry is on the cache
        Uniprot up = softCache.get(uniprotID);
        if ( up != null) {

            long timeE = System.currentTimeMillis();
            logger.debug("UniProtDAOImpl got UP " + uniprotID + " from soft cache in " + (timeE - timeS) + " ms.");

            logger.debug("Got UP information from soft cache for UniProt id {}", uniprotID);
            return up;
        }

        // step 2: see if we have a local uniprot file
        try {
            File localFile = RCSBUniProtMirror.getLocalFile(uniprotID);

            if ( localFile != null){
                InputStream inStream = new FileInputStream(localFile);

                up = UniProtTools.readUniProtFromInputStream(inStream);
            }
            RCSBUniProtMirror.delete(uniprotID);
            if ( up != null) {
                softCache.put(uniprotID,up);


                long timeE = System.currentTimeMillis();
                logger.debug("UniProtDAOImpl got UP " + uniprotID + " from XML file in " + (timeE - timeS) + " ms.");

                return up;
            }
        } catch (IOException|JAXBException e){
            logger.error("Could not load UP information from local file for UniProt id {}. Will try from database next. Error: {}", uniprotID, e.getMessage());
        }



        // step 3: load UniProtEntry from DB..
        try {

            Query q = em.createQuery("select up from org.rcsb.uniprot.auto.Uniprot up where :element in elements (up.entry.accession)  ");

            q.setParameter("element", uniprotID);

            Iterator<?> it = q.getResultList().iterator();
            if (it.hasNext())
                up = (Uniprot) it.next();

            if (up==null) {
                logger.error("No results could be found in database for UniProt id {}", uniprotID);
                return null;
            }

        } catch (HibernateException e) {
            logger.error("Could not load UP information from database for UniProt id {}, error: {}", uniprotID , e.getMessage());
            return null;
        }

        long timeE = System.currentTimeMillis();

        logger.debug("UniProtDAOImpl got UP " + uniprotID + " from DB in " + (timeE-timeS) + " ms.");

        if( timeE - timeS > 500)
            logger.info("  UniProt DAO took " + (timeE-timeS) + " ms. to load " + uniprotID);


        softCache.put(uniprotID, up);
        return up;
    }



    public  synchronized Uniprot getUniProt(String uniprotID) {

        long timeS = System.currentTimeMillis();

        Uniprot up = null;
        EntityManager em = null;

        try {
            em = JpaUtilsUniProt.getEntityManager();


            up = getUniProt(em, uniprotID);


        } catch (Exception e) {
            logger.error("Could not get UP information from database for UniProt id {}. Error: {}",uniprotID, e.getMessage());

        } finally {
            if (em!=null) em.close();
        }


        return up;
    }






    public String getDescription(String uniprotID) {

        String header = null;

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        header = getUniProtHeader(em, uniprotID);

        em.close();

        return header;
    }

    public String getUniProtHeader(EntityManager em, String uniprotAccession){
        String header = null;

        Uniprot up = getUniProt(em, uniprotAccession);
        if (up == null) {
            return "Unknown";
        }
        StringWriter h = new StringWriter();
        for (Entry e : up.getEntry()) {
            if ( e == null)
                continue;
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



        Integer version = null;
        List<String> accList = null;
        Iterator<String> it2 = null;

        EntityManager em = null;

        try {
            em = JpaUtilsUniProt.getEntityManager();

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
            logger.error(e.getMessage(),e);
        } finally {
            if (em!=null) em.close();
        }
        return ans;
    }



    public void clearPdbUniProtMapping(){

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        em.getTransaction().begin();
        Query q = em.createNativeQuery("delete from uniprotpdbmap");
        q.executeUpdate();
        em.getTransaction().commit();
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



    public void addToPdbUniProtMapping(EntityManager em, String accession, SortedSet<String> pdbIds){
        for ( String p : pdbIds){
            UniProtPdbMap m = new UniProtPdbMap();
            m.setPdbId(p);
            m.setUniProtAc(accession);
            em.persist(m);
        }
    }

    public String getCommonName(String scientificName){
        if ( organismNameMap == null)
            init();

        return organismNameMap.get(scientificName);
    }

    public List<String> getOrganism(String uniprotAc) {
        if( organismAcMap == null)
            init();

        return organismAcMap.get(uniprotAc);

    }


    public List<Object[]> getPdbReferencesFromUniProt() {

        return getDbReferencesFromUniProt("PDB");
    }

    public List<Object[]> getDbReferencesFromUniProt(String dbType) {
        //String up_sql = "select a.up_entry_objId,a.element,r.id,p.value from up_entry_accession a "
//                    + "join up_entry_up_dbreference d on a.up_entry_objId=d.up_entry_objId "
//                    + "join up_dbreference r on d.dbReference_objId=r.objId "
//                    + "join up_dbreference_up_property u on r.objId=u.up_dbreference_objId "
//                    + "join up_property p on p.objId=u.property_objId "
//                    + "where r.type='PDB' and p.type='chains' and r.id not in ( '"
//                    + StringUtils.join(assignedEntries, "','") + "' )";



        String sql =
                " select en.HJID, a.hjvalue,  r.ID,r.TYPE_ , p.value_ "+
                        " from entry_accession a "+
                        " join entry en on en.HJID = a.HJID  " +
                        " join db_reference_type r on en.HJID = r.DB_REFERENCE_ENTRY_HJID " +
                        " join property_type p on p.PROPERTY_DB_REFERENCE_TYPE_H_0 = r.HJID " +
                        " where r.TYPE_ =:db_reference_type and p.type_='chains'";


        EntityManager em = JpaUtilsUniProt.getEntityManager();

        Query q = em.createNativeQuery(sql);
        q.setParameter("db_reference_type", dbType);
        List<Object[]> data = new ArrayList<Object[]>();
        List<Object[]> rows = q.getResultList();
        data.addAll(rows);
        em.close();

        return data;


    }

    public Map<String,List<String>> getShortNameMap(){
//        String sqlStatement="select a.element,rs.element from up_entry up join up_protein_up_recommendedname rn on rn.up_protein_objId= up.protein_objId "+
//                "join up_recommendedname r on r.objId=rn.recommendedName_objId join up_entry_accession a on a.up_entry_objId=up.objId "+
//                "join up_recommendedname_shortname rs on r.objId=rs.up_recommendedName_objId";

        String sql = "select a.hjvalue, est.value_  " +
                " from entry_accession as a, entry as en , protein_type as p , " +
                "  evidenced_string_type est , recommended_name as r " +
                " where  en.HJID = a.HJID and  " +
                " p.HJID = en.PROTEIN_ENTRY_HJID and  " +
                " p.RECOMMENDED_NAME_PROTEIN_TYP_0 = r.HJID and " +
                " r.HJID = est.short_name_RECOMMENDED_NAME__0  " ;



        EntityManager em = JpaUtilsUniProt.getEntityManager();
        List<Object[]> data = (List<Object[]>) em.createNativeQuery(sql).getResultList();

        Map<String,List<String>> results = new TreeMap<String,List<String>>();

        for ( Object[] d : data){
            String ac = d[0].toString();
            String shortName = d[1].toString();
            List<String> shorts = results.get(ac);
            if ( shorts == null){
                shorts = new ArrayList<>();
                results.put(ac,shorts);
            }
            if ( ! shorts.contains(shortName)) {
                shorts.add(shortName);
            }
        }
        em.close();
        return results;

    }

    public  Map<String,List<String>> getAlternativeNameMap(){
        return getAlternativeNameMap(null);
    }

    public  Map<String,List<String>> getAlternativeNameMap(Set<String> accessions){

        StringBuffer sb = new StringBuffer("select a.hjvalue, est.value_ ");
        sb.append(" from entry_accession as a ");
        sb.append(" join entry as en on en.HJID = a.HJID ");
        sb.append(" join alternative_name an on an.ALTERNATIVE_NAME_PROTEIN_TYP_0 = en.PROTEIN_ENTRY_HJID ");
        sb.append(" join evidenced_string_type as est on an.FULL_NAME_ALTERNATIVE_NAME_H_0 = est.HJID  ");
        if(accessions != null && accessions.size() > 0) {
            sb.append("where a.hjvalue in ( '" + StringUtils.join(accessions, "','")+ "') ");
        }

        String sql = sb.toString();

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        List<Object[]> data = (List<Object[]>) em.createNativeQuery(sql).getResultList();

        Map<String,List<String>> results = new TreeMap<String,List<String>>();

        for ( Object[] d : data){
            String ac = d[0].toString();
            String altname = d[1].toString();
            List<String> alternatives = results.get(ac);
            if ( alternatives == null){
                alternatives = new ArrayList<>();
                results.put(ac,alternatives);
            }
            if ( ! alternatives.contains(altname)) {
                alternatives.add(altname);
            }
        }
        em.close();
        return results;

    }

    public Map<String, String> getAC2NameMap(Set<String> aaccessions ){

        if (aaccessions.isEmpty()) return new HashMap<>();

        String sql = "select a.HJID from entry_accession a where a.HJVALUE in ('"
                + StringUtils.join(aaccessions, "','") + "')";

        EntityManager em = JpaUtilsUniProt.getEntityManager();
        SortedSet<String> objIds = new TreeSet<String> (em.createNativeQuery(sql).getResultList());

        String accSql = "select a.HJID,a.HJVALUE, a.HJINDEX from entry_accession a where a.HJID in ("
                + StringUtils.join(objIds, ",") + ")";

        Query entryNamesHJID = em.createNativeQuery(accSql);
        List<Object[]> results = entryNamesHJID.getResultList();

        Map<String, String> uniProtPrimaryAccessions = new HashMap<String, String>();

        Set<String> supercededIds = new HashSet<String>();

        for(Object[] data : results){
            String hjid = data[0].toString();
            String accession = (String)data[1];
            Integer hjindex = (Integer)data[2];

            if (supercededIds.contains(hjid))
                continue;

            if ( hjindex == 0) {

                uniProtPrimaryAccessions.put(hjid, accession);
            }

        }

        objIds.removeAll(supercededIds);

        accSql = " select n.HJID,n.HJVALUE from entry_name_ n where n.HJID in ("
                + StringUtils.join(objIds, ",") + ")";
        //pdbiddata = QueryLookupValues.querySql(accSql);

        javax.persistence.Query query3 = em.createNativeQuery(accSql);
        List<Object[]> results3 = query3.getResultList();

        Map<String, String> uniProtNames = new HashMap<String, String>();
        for (Object[] obj : results3){
            String hjid = (obj[0]).toString();
            String name = (String) obj[1];
            uniProtNames.put(uniProtPrimaryAccessions.get(hjid), name);
        }
        em.close();


        return uniProtNames;

    }

    public  Map<String,String> getRecommendedNameMap(){

        return getRecommendedNameMap(null);
    }

    public  Map<String, String> getRecommendedNameMap(Set<String> accessions ){

       return UniProtDAOUtil.getRecommendedNameMap(accessions);

    }

    /** returns a map that contains a mapping of uniprot accessions to submitted names
     *
     * @return
     */
    public Map<String, String> getSubmittedNameMap(){
        return getSubmittedNameMap(null);
    }

    /** returns a map that contains a mapping of uniprot accessions to submitted names
     *
     * @param accessions
     * @return
     */
    public Map<String, String> getSubmittedNameMap(Set<String> accessions ){

        return UniProtDAOUtil.getSubmittedNameMap(accessions);

    }

    public Map<String,List<String>> getOrganismMap(){
        if ( organismAcMap == null){
            init();
        }

        return organismAcMap;

    }
    @Override
    public Map<String,String> getDiseaseMap(){
        if ( diseaseMap == null){
            init();
        }

        return diseaseMap;

    }

    @Override
    public Map<String,Integer> getSequenceLengthsMap(){
        if ( sequenceLengthsMap == null){
            init();
        }

        return sequenceLengthsMap;

    }

    @Override
    public Set<String> getUniProtIDsForDiseaseName(String diseaseName) {

        if (StringUtils.isBlank(diseaseName)) {
            return null;
        }

        Set<String> results = new TreeSet<String>();

        long timeS = System.currentTimeMillis();

        EntityManager em = JpaUtilsUniProt.getEntityManager();

        String sql = "select distinct a.hjvalue from comment_type ct " +
                "join disease d on d.hjid = ct.disease_comment_type_hjid " +
                "join entry e on e.hjid = ct.comment__entry_hjid " +
                "join entry_accession a on a.hjid = e.hjid " +
                "where type_ = 'disease' " +
                "and lower(d.name_) = :name ";


        Query q = em.createNativeQuery(sql);
        q.setParameter("name", StringUtils.lowerCase(diseaseName));

        List data = q.getResultList();
        for (Object obj : data) {

            results.add((String) obj);

        }

        em.close();
        long timeE = System.currentTimeMillis();
        logger.info(" getUniProtIDsForDiseaseName for disease name: " + diseaseName  + " took " + (timeE-timeS) + " milliseconds" );

        return results;


    }

    @Override
    public Set<String> getUniProtIDsForDiseaseAcronym(String diseaseAcronym) {

        if (StringUtils.isBlank(diseaseAcronym)) {
            return null;
        }

        Set<String> results = new TreeSet<String>();

        long timeS = System.currentTimeMillis();

        EntityManager em = JpaUtilsUniProt.getEntityManager();

        String sql = "select distinct a.hjvalue from comment_type ct " +
                "join disease d on d.hjid = ct.disease_comment_type_hjid " +
                "join entry e on e.hjid = ct.comment__entry_hjid " +
                "join entry_accession a on a.hjid = e.hjid " +
                "where type_ = 'disease' " +
                "and upper(d.acronym) = :acronym ";


        Query q = em.createNativeQuery(sql);
        q.setParameter("acronym", StringUtils.upperCase(diseaseAcronym));

        List<String> rows = q.getResultList();
        if(rows != null && rows.size() > 0){
            results.addAll(rows);
        }

        long timeE = System.currentTimeMillis();
        logger.info(" getUniProtIDsForDiseaseAcronym for disease acronym: " + diseaseAcronym + " took " + (timeE-timeS) + " milliseconds" );

        em.close();
        return results;


    }


    public List<Object[]> getSequenceVariationInfo(String uniprotId) {


        // get sequence variation info, start and stop positions...

        List<Object[]> results = new ArrayList<Object[]>();

        if (StringUtils.isBlank(uniprotId)) {
            return results;
        }


        String sql = "select " +
                " distinct f.description, f.id, f.original, fv.hjvalue as variation, p.status, updb.uniProtAc, p.position_ as pos, bp.position_ as start_pos, ep.position_ as end_pos " +
                "from entry_accession ea " +
                "join entry en on en.HJID = ea.HJID  " +
                "left join feature_type f on  f.FEATURE_ENTRY_HJID = en.HJID " +
                "left join feature_type_variation fv on  fv.HJID = f.HJID " +
                "join uniprotpdbmap updb on updb.uniProtAc = ea.HJVALUE " +
                "join location_type l on l.HJID=f.LOCATION__FEATURE_TYPE_HJID " +
                "left join position_type p on p.HJID = l.position__LOCATION_TYPE_HJID  " +
                "left join position_type bp on bp.HJID = l.BEGIN__LOCATION_TYPE_HJID  " +
                "left join position_type ep on ep.HJID = l.END__LOCATION_TYPE_HJID  " +
                "where 1=1 " +
                "and f.type_ = 'sequence variant' " +
                "and updb.uniProtAc = :uniprotId";



        EntityManager em = JpaUtilsUniProt.getEntityManager();

        Query q = em.createNativeQuery(sql);
        q.setParameter("uniprotId", StringUtils.upperCase(uniprotId));

        List<Object[]> rows = q.getResultList();
        results.addAll(rows);
        em.close();

        return results;
    }


    // todo: get cellular location map
    // todo: get disease map


    public Map<String,String> getDisplayNameMap(){
        if ( displayNameMap == null){
            init();
        }

        return displayNameMap;
    }

}