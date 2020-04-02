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

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JS_OBJECTS, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]", "[PATH]", "[VERSION]", "[OBJECT_TYPE]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_JS_OBJECTS_SEQUENCE, 
		sequenceName = DBLayer.TABLE_JS_OBJECTS_SEQUENCE, 
		allocationSize = 1)
public class DBItemJSObject extends DBItem {

	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JS_OBJECTS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;

    @Column(name = "[EDIT_ACCOUNT]", nullable = false)
    private String editAccount;

    @Column(name = "[PUBLISH_ACCOUNT]", nullable = true)
    private String publishAccount;

    @Column(name = "[OBJECT_TYPE]", nullable = false)
    private String objectType;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;
    
    @Column(name = "[PATH]", nullable = false)
    private String path;
    
    @Column(name = "[CONTENT]", nullable = true)
    private String content;

    @Column(name = "[SIGNED_CONTENT]", nullable = true)
    private String signedContent;

    @Column(name = "[URI]", nullable = false)
    private String uri;
    
    @Column(name = "[VERSION_ID]", nullable = false)
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

	public String getSchedulerId() {
		return schedulerId;
	}
	public void setSchedulerId(String val) {
		this.schedulerId = val;
	}

	public String getEditAccount() {
		return editAccount;
	}
	public void setEditAccount(String val) {
		this.editAccount = val;
	}

	public String getPublishAccount() {
		return publishAccount;
	}
	public void setPublishAccount(String val) {
		this.publishAccount = val;
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

	public String getObjectType() {
		return objectType;
	}
	public void setObjectType(String val) {
		this.objectType = val;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public String getSignedContent() {
		return content;
	}
	public void setSignedContent(String signedContent) {
		this.content = signedContent;
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String val) {
		this.uri = val;
	}

	public String getVersionId() {
		return versionId;
	}
	public void setVersionId(String val) {
		this.versionId = val;
	}

	public String getVersion() {
		return version;
	}
	public void setVersion(String val) {
		this.version = val;
	}

	public String getParentVersion() {
		return parentVersion;
	}
	public void setParentVersion(String val) {
		this.parentVersion = val;
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
