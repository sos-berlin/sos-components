package com.sos.webservices.db.inventory.os;

import java.io.Serializable;
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

import com.sos.webservices.db.inventory.InventoryDBItemConstants;

@Entity
@Table(name = InventoryDBItemConstants.TABLE_INVENTORY_OPERATING_SYSTEMS)
@SequenceGenerator(
		name = InventoryDBItemConstants.TABLE_INVENTORY_OPERATING_SYSTEMS_SEQUENCE, 
		sequenceName = InventoryDBItemConstants.TABLE_INVENTORY_OPERATING_SYSTEMS_SEQUENCE,
		allocationSize = 1)
public class DBItemInventoryOperatingSystem implements Serializable {

    private static final long serialVersionUID = 6639624402069204129L;

    /** Primary Key */
    private Long id;
    
    /** Unique Index */
    private String hostname;
    
    /** Others */
    private String name;
    private String architecture;
    private String distribution;
    private Date created;
    private Date modified;
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = InventoryDBItemConstants.TABLE_INVENTORY_OPERATING_SYSTEMS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }
    
    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = InventoryDBItemConstants.TABLE_INVENTORY_OPERATING_SYSTEMS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long id) {
        this.id = id;
    }
    
    /** Unique Index */
    @Column(name = "`HOSTNAME`", nullable = false)
    public String getHostname() {
        return hostname;
    }
    
    /** Unique Index */
    @Column(name = "`HOSTNAME`", nullable = false)
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
    
    @Column(name = "`ARCHITECTURE`", nullable = true)
    public String getArchitecture() {
        return architecture;
    }
    
    @Column(name = "`ARCHITECTURE`", nullable = true)
    public void setArchitecture(String architecture) {
        this.architecture = architecture;
    }
    
    @Column(name = "`DISTRIBUTION`", nullable = true)
    public String getDistribution() {
        return distribution;
    }
    
    @Column(name = "`DISTRIBUTION`", nullable = true)
    public void setDistribution(String distribution) {
        this.distribution = distribution;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date created) {
        this.created = created;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date modified) {
        this.modified = modified;
    }
    
    public String toDebugString() {
        StringBuilder strb = new StringBuilder();
        strb.append("ID:").append(getId()).append("|");
        strb.append("HOSTNAME:").append(getHostname()).append("|");
        strb.append("NAME:").append(getName()).append("|");
        strb.append("ARCHITECTURE:").append(getArchitecture()).append("|");
        strb.append("DISTRIBUTION:").append(getDistribution()).append("|");
        strb.append("CREATED:").append(getCreated()).append("|");
        strb.append("MODIFIED:").append(getModified());
        return strb.toString();
    }
    
}