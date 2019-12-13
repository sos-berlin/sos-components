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
    private String version;

    @Column(name = "[PARENT_VERSION]", nullable = true)
    private String parentVersion;

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

	public String getState() {
		return state;
	}
	public void setState(String val) {
		this.state = val;
	}

	public String getAccount() {
		return account;
	}
	public void setAccount(String val) {
		this.account = val;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String val) {
		this.comment = val;
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

	public Date getModified() {
		return modified;
	}
	public void setModified(Date val) {
		this.modified = val;
	}
}
