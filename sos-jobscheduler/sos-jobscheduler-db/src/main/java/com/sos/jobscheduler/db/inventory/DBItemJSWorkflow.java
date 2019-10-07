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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JS_WORKFLOW, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]", "[PATH]", "[VERSION]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_JS_WORKFLOW_SEQUENCE, 
		sequenceName = DBLayer.TABLE_JS_WORKFLOW_SEQUENCE, 
		allocationSize = 1)
public class DBItemJSWorkflow extends DBItem {

	private static final long serialVersionUID = 1L;
	
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JS_WORKFLOW_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;

    @Column(name = "[EDIT_ACCOUNT]", nullable = false)
    private String editAccount;

    @Column(name = "[PUBLISH_ACCOUNT]", nullable = false)
    private String publishAccount;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[STATE]", nullable = false)
    private String state;

    @Column(name = "[VALID]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean valid;
    
    @Column(name = "[VERSION]", nullable = true)
    private Integer version;

    @Column(name = "[PARENT_VERSION]", nullable = true)
    private Integer parentVersion;

    @Column(name = "[COMMENT]", nullable = true)
    private String comment;
    
    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
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

	public String getEditAccount() {
		return editAccount;
	}
	public void setEditAccount(String editAccount) {
		this.editAccount = editAccount;
	}

	public String getPublishAccount() {
		return publishAccount;
	}
	public void setPublishAccount(String publishAccount) {
		this.publishAccount = publishAccount;
	}

	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}

	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	public boolean isValid() {
		return valid;
	}
	public void setValid(boolean valid) {
		this.valid = valid;
	}

	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}

	public Integer getParentVersion() {
		return parentVersion;
	}
	public void setParentVersion(Integer parentVersion) {
		this.parentVersion = parentVersion;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}

    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(path).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemJSWorkflow)) {
            return false;
        }
        DBItemJSWorkflow rhs = ((DBItemJSWorkflow) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(path, rhs.path).append(version, rhs.version).isEquals();
    }

}
