package com.sos.joc.db.joc;

import java.util.Date;

import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_JOC_CONFIGURATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[ACCOUNT]",
        "[OBJECT_TYPE]", "[CONFIGURATION_TYPE]", "[NAME]" }) })
public class DBItemJocConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_JOC_CONFIGURATIONS_SEQUENCE)
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
    @Convert(converter = NumericBooleanConverter.class)
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