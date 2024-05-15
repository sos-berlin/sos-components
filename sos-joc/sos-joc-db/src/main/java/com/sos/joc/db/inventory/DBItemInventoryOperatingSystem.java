package com.sos.joc.db.inventory;

import java.util.Date;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_OPERATING_SYSTEMS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[HOSTNAME]" }) })
public class DBItemInventoryOperatingSystem extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_OPERATING_SYSTEMS_SEQUENCE)
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