package com.sos.joc.db.joc;

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

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOC_CONFIGURATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[ACCOUNT]",
        "[OBJECT_TYPE]", "[CONFIGURATION_TYPE]", "[NAME]" }) })
@SequenceGenerator(name = DBLayer.TABLE_JOC_CONFIGURATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_JOC_CONFIGURATIONS_SEQUENCE, allocationSize = 1)
public class DBItemJocConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOC_CONFIGURATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    /** Foreign key INVENTORY_INSTANCES.ID */
    @Column(name = "[INSTANCE_ID]", nullable = false)
    private Long instanceId;

    @Column(name = "[CONTROLLER_ID]", nullable = true)
    private String controllerId;

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

    public DBItemJocConfiguration() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(Long val) {
        instanceId = val;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setConfigurationItem(String val) {
        configurationItem = val;
    }

    public String getConfigurationItem() {
        return configurationItem;
    }

    public void setAccount(String val) {
        account = val;
    }

    public String getAccount() {
        return account;
    }

    public void setObjectType(String val) {
        objectType = val;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setConfigurationType(String val) {
        configurationType = val;
    }

    public String getConfigurationType() {
        return configurationType;
    }

    public void setName(String val) {
        name = val;
    }

    public String getName() {
        return name;
    }

    public void setShared(Boolean val) {
        shared = val;
    }

    public Boolean getShared() {
        return shared;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

}