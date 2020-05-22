package com.sos.jobscheduler.db.xmleditor;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS)
@SequenceGenerator(name = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE, allocationSize = 1)
public class DBItemXmlEditorConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    /** Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    /** Others */
    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    private String objectType;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[SCHEMA_LOCATION]", nullable = false)
    private String schemaLocation;

    @Column(name = "[CONFIGURATION_DRAFT]", nullable = true)
    private String configurationDraft;

    @Column(name = "[CONFIGURATION_DRAFT_JSON]", nullable = true)
    private String configurationDraftJson;

    @Column(name = "[CONFIGURATION_DEPLOYED]", nullable = true)
    private String configurationDeployed;

    @Column(name = "[CONFIGURATION_DEPLOYED_JSON]", nullable = true)
    private String configurationDeployedJson;

    @Column(name = "[AUDIT_LOG_ID]", nullable = false)
    private Long auditLogId;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[DEPLOYED]", nullable = false)
    private Date deployed;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

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

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String val) {
        objectType = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }

    public void setSchemaLocation(String val) {
        schemaLocation = val;
    }

    public String getConfigurationDraft() {
        return configurationDraft;
    }

    public void setConfigurationDraft(String val) {
        configurationDraft = val;
    }

    public String getConfigurationDraftJson() {
        return configurationDraftJson;
    }

    public void setConfigurationDraftJson(String val) {
        configurationDraftJson = val;
    }

    public String getConfigurationDeployed() {
        return configurationDeployed;
    }

    public void setConfigurationDeployed(String val) {
        configurationDeployed = val;
    }

    public String getConfigurationDeployedJson() {
        return configurationDeployedJson;
    }

    public void setConfigurationDeployedJson(String val) {
        configurationDeployedJson = val;
    }

    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long val) {
        auditLogId = val;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String val) {
        account = val;
    }

    public Date getDeployed() {
        return deployed;
    }

    public void setDeployed(Date val) {
        deployed = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

}