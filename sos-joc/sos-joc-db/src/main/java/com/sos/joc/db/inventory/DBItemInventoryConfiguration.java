package com.sos.joc.db.inventory;

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

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_INV_CONFIGURATIONS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[PATH]", "[OBJECT_TYPE]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_CONFIGURATIONS_SEQUENCE, sequenceName = DBLayer.TABLE_INV_CONFIGURATIONS_SEQUENCE, allocationSize = 1)
public class DBItemInventoryConfiguration extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_CONFIGURATIONS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[EDIT_ACCOUNT]", nullable = false)
    private String editAccount;

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    private String objectType;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[OLD_PATH]", nullable = true)
    private String oldPath;

    @Column(name = "[URI]", nullable = true)
    private String uri;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[SIGNED_CONTENT]", nullable = true)
    private String signedContent;

    @Column(name = "[STATE]", nullable = true)
    private String state;

    @Column(name = "[OPERATION]", nullable = true)
    private String operation;

    @Column(name = "[VERSION_ID]", nullable = true)
    private String versionId;

    @Column(name = "[VERSION]", nullable = true)
    private String version;

    @Column(name = "[PARENT_VERSION]", nullable = true)
    private String parentVersion;

    @Column(name = "[COMMENT]", nullable = true)
    private String comment;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        this.id = val;
    }

    public String getEditAccount() {
        return editAccount;
    }

    public void setEditAccount(String val) {
        this.editAccount = val;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        this.folder = val;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        this.path = val;
    }

    public String getOldPath() {
        return path;
    }

    public void setOldPath(String val) {
        this.oldPath = val;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String val) {
        this.objectType = val;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String signedContent) {
        this.content = signedContent;
    }

    public String getSignedContent() {
        return signedContent;
    }

    public void setSignedContent(String signedContent) {
        this.signedContent = signedContent;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        this.uri = val;
    }

    public String getState() {
        return state;
    }

    public void setState(String val) {
        this.state = val;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String val) {
        this.operation = val;
    }

    public String getVersionId() {
        return versionId;
    }

    public void setVersionId(String versionId) {
        this.versionId = versionId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getParentVersion() {
        return parentVersion;
    }

    public void setParentVersion(String parentVersion) {
        this.parentVersion = parentVersion;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String val) {
        this.comment = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        this.modified = val;
    }
}
