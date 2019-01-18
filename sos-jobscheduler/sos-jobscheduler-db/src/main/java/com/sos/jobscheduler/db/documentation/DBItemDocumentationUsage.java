package com.sos.jobscheduler.db.documentation;

import java.io.Serializable;
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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.sos.jobscheduler.db.JocDBItemConstants;

@Entity
@Table(name = JocDBItemConstants.TABLE_DOCUMENTATION_USAGE)
@SequenceGenerator(
		name = JocDBItemConstants.TABLE_DOCUMENTATION_USAGE_SEQUENCE, 
		sequenceName = JocDBItemConstants.TABLE_DOCUMENTATION_USAGE_SEQUENCE, 
		allocationSize = 1)
public class DBItemDocumentationUsage implements Serializable {

	private static final long serialVersionUID = 1L;
	private Long id;
	private String schedulerId;
	private Long documentationId;
	private String objectType;
	private String path;
	private Date created;
	private Date modified;

	/** Primary key */
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_DOCUMENTATION_USAGE_SEQUENCE)
	@Column(name = "[ID]", nullable = false)
	public Long getId() {
		return id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO, generator = JocDBItemConstants.TABLE_DOCUMENTATION_USAGE_SEQUENCE)
	@Column(name = "[ID]", nullable = false)
	public void setId(Long id) {
		this.id = id;
	}

	/** Others */
	@Column(name = "[SCHEDULER_ID]", nullable = false)
	public String getSchedulerId() {
		return schedulerId;
	}

	@Column(name = "[SCHEDULER_ID]", nullable = false)
	public void setSchedulerId(String schedulerId) {
		this.schedulerId = schedulerId;
	}

	@Column(name = "[DOCUMENTATION_ID]", nullable = false)
	public Long getDocumentationId() {
		return documentationId;
	}

	@Column(name = "[DOCUMENTATION_ID]", nullable = false)
	public void setDocumentationId(Long documentationId) {
		this.documentationId = documentationId;
	}

	@Column(name = "[OBJECT_TYPE]", nullable = false)
	public String getObjectType() {
		return objectType;
	}

	@Column(name = "[OBJECT_TYPE]", nullable = false)
	public void setObjectType(String objectType) {
		this.objectType = objectType;
	}

	@Column(name = "[PATH]", nullable = false)
	public String getPath() {
		return path;
	}

	@Column(name = "[PATH]", nullable = false)
	public void setPath(String path) {
		this.path = path;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "[CREATED]", nullable = false)
	public Date getCreated() {
		return created;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "[CREATED]", nullable = false)
	public void setCreated(Date created) {
		this.created = created;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "[MODIFIED]", nullable = false)
	public Date getModified() {
		return modified;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "[MODIFIED]", nullable = false)
	public void setModified(Date modified) {
		this.modified = modified;
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(schedulerId).append(documentationId).append(objectType).append(path)
				.toHashCode();
	}

	@Override
	public boolean equals(Object other) {
		// always compare on unique constraint
		if (other == this) {
			return true;
		}
		if (!(other instanceof DBItemDocumentationUsage)) {
			return false;
		}
		DBItemDocumentationUsage rhs = ((DBItemDocumentationUsage) other);
		return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(documentationId, rhs.documentationId)
				.append(objectType, rhs.objectType).append(path, rhs.path).isEquals();
	}

}
