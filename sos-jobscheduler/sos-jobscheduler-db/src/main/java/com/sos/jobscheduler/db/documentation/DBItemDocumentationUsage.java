package com.sos.jobscheduler.db.documentation;

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
@Table(name = DBLayer.TABLE_DOCUMENTATION_USAGE,
       uniqueConstraints = { @UniqueConstraint(columnNames = { "[JOBSCHEDULER_ID]","[DOCUMENTATION_ID]","[OBJECT_TYPE]","[PATH]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_DOCUMENTATION_USAGE_SEQUENCE, 
		sequenceName = DBLayer.TABLE_DOCUMENTATION_USAGE_SEQUENCE, 
		allocationSize = 1)
public class DBItemDocumentationUsage extends DBItem {

	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DOCUMENTATION_USAGE_SEQUENCE)
	@Column(name = "[ID]", nullable = false)
	private Long id;
	
	@Column(name = "[JOBSCHEDULER_ID]", nullable = false)
	private String schedulerId;
	
	@Column(name = "[DOCUMENTATION_ID]", nullable = false)
	private Long documentationId;
	
	@Column(name = "[OBJECT_TYPE]", nullable = false)
	private String objectType;
	
	@Column(name = "[PATH]", nullable = false)
	private String path;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "[CREATED]", nullable = false)
	private Date created;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "[MODIFIED]", nullable = false)
	private Date modified;

	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}

	public String getSchedulerId() {
		return schedulerId;
	}
	
	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

	public Long getDocumentationId() {
		return documentationId;
	}
	
	public void setDocumentationId(Long documentationId) {
		this.documentationId = documentationId;
	}

	public String getObjectType() {
		return objectType;
	}
	
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}

	public Date getCreated() {
		return created;
	}
	
	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getModified() {
		return modified;
	}
	
	public void setModified(Date modified) {
		this.modified = modified;
	}
}
