package com.sos.joc.db.inventory;

import java.util.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.Type;

import com.sos.commons.hibernate.type.SOSHibernateJsonType;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.inventory.common.ConfigurationType;

@Entity
@Table(name = DBLayer.TABLE_INV_RELEASED_CONFIGURATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[TYPE]", "[PATH]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_RELEASED_CONFIGURATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_RELEASED_CONFIGURATIONS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryReleasedConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_RELEASED_CONFIGURATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[CID]", nullable = false)
    private Long cid;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[JSON_CONTENT]", nullable = false)
    @Type(value = SOSHibernateJsonType.class)
    @ColumnTransformer(write = SOSHibernateJsonType.COLUMN_TRANSFORMER_WRITE_DEFAULT)
    private String jsonContent;

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
        return ConfigurationType.fromValue(type);
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

    public Long getCid() {
        return cid;
    }

    public void setCid(Long val) {
        cid = val;
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

    public Long getAuditLogId() {
        return auditLogId;
    }

    public void setAuditLogId(Long val) {
        auditLogId = val;
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
