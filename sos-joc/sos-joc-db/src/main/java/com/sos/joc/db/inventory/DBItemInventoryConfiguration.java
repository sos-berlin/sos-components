package com.sos.joc.db.inventory;

import java.util.Date;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Proxy;
import org.hibernate.annotations.Type;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.commons.hibernate.type.SOSHibernateJsonType;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.ConfigurationType;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_INV_CONFIGURATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[TYPE]", "[PATH]" }) })
@Proxy(lazy = false)
public class DBItemInventoryConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_INV_CONFIGURATIONS_SEQUENCE)
    private Long id;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[CONTENT]", nullable = true)
    private String content;

    @Column(name = "[JSON_CONTENT]", nullable = true)
    @Type(value = SOSHibernateJsonType.class)
    @ColumnTransformer(write = SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_DEFAULT)
    private String jsonContent;

    @Column(name = "[VALID]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean valid;

    @Column(name = "[DELETED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean deleted;

    @Column(name = "[DEPLOYED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean deployed;

    @Column(name = "[RELEASED]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean released;

    @Column(name = "[REPO_CTRL]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean repoControlled = false;

    @Column(name = "[AUDIT_LOG_ID]", nullable = false)
    private Long auditLogId;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Integer getType() {
        return type;
    }

    @Transient
    public ConfigurationType getTypeAsEnum() {
        try {
            return ConfigurationType.fromValue(type);
        } catch (Exception e) {
            return null;
        }
    }

    public void setType(Integer val) {
        type = val;
    }

    @Transient
    public void setType(ConfigurationType val) {
        setType(val == null ? null : val.intValue());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        path = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        folder = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String val) {
        content = val;
        jsonContent = val;
    }

    public String getJsonContent() {
        return jsonContent;
    }

    // public void setJsonContent(String val) {
    // jsonContent = val;
    // }

    public boolean getValid() {
        return valid;
    }

    public void setValid(boolean val) {
        valid = val;
    }

    public boolean getDeleted() {
        return deleted;
    }

    public void setDeleted(boolean val) {
        deleted = val;
    }

    public boolean getDeployed() {
        return deployed;
    }

    public void setDeployed(boolean val) {
        deployed = val;
    }

    public boolean getReleased() {
        return released;
    }

    public void setReleased(boolean val) {
        released = val;
    }

    public boolean getRepoControlled() {
        return repoControlled;
    }

    public void setRepoControlled(boolean repoControlled) {
        this.repoControlled = repoControlled;
    }

    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long val) {
        auditLogId = val == null ? 0L : val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }

}
