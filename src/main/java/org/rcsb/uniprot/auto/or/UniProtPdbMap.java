package org.rcsb.uniprot.auto.or;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlAttribute;
import java.io.Serializable;

/**
 * Created by andreas on 10/4/14.
 */
@Entity(name="UniProtPdbMap")
@Table(name = "UniProtPdbMap",
indexes = {@Index(columnList = "uniProtAc"),@Index(columnList = "pdbId")})

public class UniProtPdbMap implements Serializable {
    private final static long serialVersionUID = 100L;



    @XmlAttribute(name = "Hjid")
    protected Long hjid;

    @XmlAttribute
    protected String uniProtAc;
    @XmlAttribute
    protected String pdbId;

    /**
     * Gets the value of the hjid property.
     *
     * @return
     *     possible object is
     *     {@link Long }
     *
     */
    @Id
    @Column(name = "HJID")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public Long getHjid() {
        return hjid;
    }

    /**
     * Sets the value of the hjid property.
     *
     * @param value
     *     allowed object is
     *     {@link Long }
     *
     */
    public void setHjid(Long value) {
        this.hjid = value;
    }


    @Column()
    public String getUniProtAc() {
        return uniProtAc;
    }

    public void setUniProtAc(String uniProtAc) {
        this.uniProtAc = uniProtAc;
    }


    @Column()
    public String getPdbId() {
        return pdbId;
    }

    public void setPdbId(String pdbId) {
        this.pdbId = pdbId;
    }
}
