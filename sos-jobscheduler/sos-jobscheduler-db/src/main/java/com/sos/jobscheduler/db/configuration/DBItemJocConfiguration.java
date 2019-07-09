package com.sos.jobscheduler.db.configuration;

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
@Table(name = DBLayer.TABLE_JOC_CONFIGURATIONS,
       uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]","[ACCOUNT]","[OBJECT_TYPE]","[CONFIGURATION_TYPE]","[NAME]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_JOC_CONFIGURATIONS_SEQUENCE,
		sequenceName = DBLayer.TABLE_JOC_CONFIGURATIONS_SEQUENCE,
		allocationSize = 1)
public class DBItemJocConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOC_CONFIGURATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    /** Foreign key INVENTORY_INSTANCES.ID */
    @Column(name = "[INSTANCE_ID]", nullable = false)
    private Long instanceId;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;
    
    @Column(name = "[OBJECT_TYPE]", nullable = false)
    private String objectType;
    
    @Column(name = "[CONFIGURATION_TYPE]", nullable = false)
    private String configurationType;
    
    @Column(name = "[NAME]", nullable = true)
    private String name;
    
    @Column(name = "[SHARED]", nullable = false)
    @Type(type = "numeric_boolean")
    private Boolean shared;
    
    @Column(name = "[CONFIGURATION_ITEM]", nullable = false)
    private String configurationItem;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;
    
    @Column(name = "[SCHEDULER_ID]", nullable = true)
    private String schedulerId;

    public DBItemJocConfiguration() {
    }

    public Long getId() {
        return this.id;
    }
    public void setId(Long val) {
        this.id = val;
    }

    public Long getInstanceId() {
        return this.instanceId;
    }
    public void setInstanceId(Long val) {
        this.instanceId = val;
    }

    public void setConfigurationItem(String val) {
        this.configurationItem = val;
    }
    public String getConfigurationItem() {
        return this.configurationItem;
    }

    public void setAccount(String val) {
        this.account = val;
    }
    public String getAccount() {
        return this.account;
    }

    public void setObjectType(String val) {
        this.objectType = val;
    }
    public String getObjectType() {
        return this.objectType;
    }

    public void setConfigurationType(String val) {
        this.configurationType = val;
    }
    public String getConfigurationType() {
        return this.configurationType;
    }

    public void setName(String val) {
        this.name = val;
    }
    public String getName() {
        return this.name;
    }

    public void setShared(Boolean val) {
        this.shared = val;
    }
    public Boolean getShared() {
        return this.shared;
    }

    public void setModified(Date val) {
        this.modified = val;
    }
    public Date getModified() {
        return this.modified;
    }

    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }
    public String getSchedulerId() {
        return this.schedulerId;
    }

}