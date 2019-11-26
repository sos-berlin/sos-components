package com.sos.jobscheduler.db.inventory;

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
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Type;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_INVENTORY_INSTANCES, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]", "[URI]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE, 
		sequenceName = DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE, 
		allocationSize = 1)
public class DBItemInventoryInstance extends DBItem {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INVENTORY_INSTANCES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[CLUSTER_URI]", nullable = false)
    private String clusterUri;

    /* foreign key INVENTORY_OPERTATION_SYSTEM.ID */
    @Column(name = "[OS_ID]", nullable = false)
    private Long osId;

    @Column(name = "[VERSION]", nullable = true)
    private String version;

    @Column(name = "[TIMEZONE]", nullable = true)
    private String timezone;

    @Column(name = "[STARTED_AT]", nullable = true)
    private Date startedAt;

    /* 0=passive, 1=active */
    @Column(name = "[IS_ACTIVE]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean isActive;

    /* 0=Single, 1=Cluster */
    @Column(name = "[IS_CLUSTER]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean isCluster;
    
    /* 0=Backup, 1=Primary */
    @Column(name = "[IS_PRIMARY_MASTER]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean isPrimaryMaster;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerId(String val) {
    	schedulerId = val;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        uri = val;
    }
    
    public String getClusterUri() {
        return clusterUri;
    }

    public void setClusterUri(String val) {
        clusterUri = val;
    }

    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long val) {
    	if (val == null) {
            val = 0L;
        }
    	osId = val;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String val) {
    	version = val;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String val) {
        timezone = val;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date val) {
    	startedAt = val;
    }
    
    public boolean getIsActive() {
        return isActive;
    }
    
    public void setIsActive(boolean val) {
        isActive = val;
    }

    public boolean getIsCluster() {
        return isCluster;
    }

    public void setIsCluster(boolean val) {
    	isCluster = val;
    }

    public boolean getIsPrimaryMaster() {
        return isPrimaryMaster;
    }
    
    public void setIsPrimaryMaster(boolean val) {
    	isPrimaryMaster = val;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

}