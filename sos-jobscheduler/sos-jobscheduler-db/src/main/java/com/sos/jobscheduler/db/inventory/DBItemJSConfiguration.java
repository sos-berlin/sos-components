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

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table( name = DBLayer.TABLE_JS_CONFIGURATION, 
		uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]", "[VERSION]" }) })
@SequenceGenerator(
		name = DBLayer.TABLE_JS_CONFIGURATION_SEQUENCE, 
		sequenceName = DBLayer.TABLE_JS_CONFIGURATION_SEQUENCE, 
		allocationSize = 1)
public class DBItemJSConfiguration extends DBItem {

	private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JS_CONFIGURATION_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;
    
    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;

    @Column(name = "[STATE]", nullable = false)
    private String state;

	@Column(name = "[ACCOUNT]", nullable = false)
	private String account;

    @Column(name = "[COMMENT]", nullable = false)
    private String comment;
    
    @Column(name = "[VERSION]", nullable = false)
    private Integer version;

    @Column(name = "[PARENT_VERSION]", nullable = true)
    private Integer parentVersion;

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

	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}

	public String getAccount() {
		return account;
	}
	public void setAccount(String account) {
		this.account = account;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
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

	public Date getModified() {
		return modified;
	}
	public void setModified(Date modified) {
		this.modified = modified;
	}

	@Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemJSConfiguration)) {
            return false;
        }
        DBItemJSConfiguration rhs = ((DBItemJSConfiguration) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(version, rhs.version).isEquals();
    }

}
