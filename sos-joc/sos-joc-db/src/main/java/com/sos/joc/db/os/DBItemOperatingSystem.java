package com.sos.joc.db.os;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_JS_OPERATING_SYSTEMS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[HOSTNAME]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_JS_OPERATING_SYSTEMS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_JS_OPERATING_SYSTEMS_SEQUENCE, allocationSize = 1)
public class DBItemOperatingSystem extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_JS_OPERATING_SYSTEMS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[HOSTNAME]", nullable = false)
    private String hostname;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[ARCHITECTURE]", nullable = false)
    private String architecture;

    @Column(name = "[DISTRIBUTION]", nullable = false)
    private String distribution;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    @Column(name = "`NAME`", nullable = true)
    public String getName() {
        return name;
    }

    @Column(name = "`NAME`", nullable = true)
    public void setName(String name) {
        this.name = name;
    }

    public String getArchitecture() {
        return architecture;
    }

    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }

    public String getDistribution() {
        return distribution;
    }

    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

    @Transient
    public String toDebugString() {
        StringBuilder strb = new StringBuilder();
        strb.append("ID:").append(getId()).append("|");
        strb.append("HOSTNAME:").append(getHostname()).append("|");
        strb.append("NAME:").append(getName()).append("|");
        strb.append("ARCHITECTURE:").append(getArchitecture()).append("|");
        strb.append("DISTRIBUTION:").append(getDistribution()).append("|");
        strb.append("MODIFIED:").append(getModified());
        return strb.toString();
    }

}