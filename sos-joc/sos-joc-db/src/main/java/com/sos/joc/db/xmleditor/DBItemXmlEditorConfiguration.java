package com.sos.joc.db.xmleditor;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS)
@SequenceGenerator(name = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE, allocationSize = 1)
public class DBItemXmlEditorConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    /** Primary Key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_XML_EDITOR_CONFIGURATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    /** Others */
    @Column(name = "[TYPE]", nullable = false)
    private String type;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[SCHEMA_LOCATION]", nullable = false)
    private String schemaLocation;

    @Column(name = "[CONFIGURATION_DRAFT]", nullable = true)
    private String configurationDraft;

    @Column(name = "[CONFIGURATION_DRAFT_JSON]", nullable = true)
    private String configurationDraftJson;

    @Column(name = "[CONFIGURATION_RELEASED]", nullable = true)
    private String configurationReleased;

    @Column(name = "[CONFIGURATION_RELEASED_JSON]", nullable = true)
    private String configurationReleasedJson;

    @Column(name = "[AUDIT_LOG_ID]", nullable = false)
    private Long auditLogId;

    @Column(name = "[ACCOUNT]", nullable = false)
    private String account;

    @Column(name = "[RELEASED]", nullable = false)
    private Date released;

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

    public String getType() {
        return type;
    }

    public void setType(String val) {
        type = val;
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

    public String getConfigurationReleased() {
        return configurationReleased;
    }

    public void setConfigurationReleased(String val) {
        configurationReleased = val;
    }

    public String getConfigurationReleasedJson() {
        return configurationReleasedJson;
    }

    public void setConfigurationReleasedJson(String val) {
        configurationReleasedJson = val;
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

    public Date getReleased() {
        return released;
    }

    public void setReleased(Date val) {
        released = val;
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