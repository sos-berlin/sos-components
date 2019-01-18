package com.sos.jobscheduler.db.inventory;

import java.beans.Transient;
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

@Entity
@Table(name = InventoryDBItemConstants.TABLE_INVENTORY_INSTANCES)
@SequenceGenerator(
		name = InventoryDBItemConstants.TABLE_INVENTORY_INSTANCES_SEQUENCE,
		sequenceName = InventoryDBItemConstants.TABLE_INVENTORY_INSTANCES_SEQUENCE,
		allocationSize = 1)
public class DBItemInventoryInstance implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;

    /** Others */
    private String schedulerId;
    private String hostname;
    private Integer port;
    private String liveDirectory;
    private Date created;
    private Date modified;

    /** new Fields starting release 1.11 */
    private String commandUrl;
    private String url;
    private String clusterType;
    private Integer precedence;
    private String dbmsName;
    private String dbmsVersion;
    private Long supervisorId;
    private Date startedAt;
    private String version;
    private String timeZone;
    private String auth;
    private String origUrl;

    /** foreign key INVENTORY_OPERTATION_SYSTEM.ID*/
    private Long osId;

    public DBItemInventoryInstance() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = InventoryDBItemConstants.TABLE_INVENTORY_INSTANCES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return this.id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = InventoryDBItemConstants.TABLE_INVENTORY_INSTANCES_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        this.id = val;
    }

    /** Others */
    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
        this.schedulerId = val;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return this.schedulerId;
    }

    @Column(name = "`HOSTNAME`", nullable = false)
    public void setHostname(String val) {
        this.hostname = val;
    }

    @Column(name = "`HOSTNAME`", nullable = false)
    public String getHostname() {
        return this.hostname;
    }

    @Column(name = "`PORT`", nullable = false)
    public Integer getPort() {
        return this.port;
    }

    @Column(name = "`PORT`", nullable = false)
    public void setPort(Integer val) {
        this.port = val;
    }

    @Column(name = "`LIVE_DIRECTORY`", nullable = false)
    public void setLiveDirectory(String val) {
        this.liveDirectory = val;
    }

    @Column(name = "`LIVE_DIRECTORY`", nullable = false)
    public String getLiveDirectory() {
        return this.liveDirectory;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        this.created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return this.created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        this.modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return this.modified;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STARTED_AT`", nullable = false)
    public Date getStartedAt() {
        return this.startedAt;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`STARTED_AT`", nullable = false)
    public void setStartedAt(Date startedAt) {
        this.startedAt = startedAt;
    }

    @Column(name = "`URL`", nullable = false)
    public String getUrl() {
        return this.url;
    }

    @Column(name = "`URL`", nullable = false)
    public void setUrl(String url) {
        this.url = url;
    }

    @Column(name = "`COMMAND_URL`", nullable = false)
    public String getCommandUrl() {
        return commandUrl;
    }

    @Column(name = "`COMMAND_URL`", nullable = false)
    public void setCommandUrl(String commandUrl) {
        this.commandUrl = commandUrl;
    }

    @Column(name = "`DBMS_NAME`", nullable = false)
    public String getDbmsName() {
        return dbmsName;
    }

    @Column(name = "`DBMS_NAME`", nullable = false)
    public void setDbmsName(String dbmsName) {
        this.dbmsName = dbmsName;
    }

    @Column(name = "`DBMS_VERSION`", nullable = true)
    public String getDbmsVersion() {
        return dbmsVersion;
    }

    @Column(name = "`DBMS_VERSION`", nullable = true)
    public void setDbmsVersion(String dbmsVersion) {
        this.dbmsVersion = dbmsVersion;
    }

    @Column(name = "`OS_ID`", nullable = false)
    public Long getOsId() {
        return osId;
    }

    @Column(name = "`OS_ID`", nullable = false)
    public void setOsId(Long osId) {
        if (osId == null) {
            osId = InventoryDBItemConstants.DEFAULT_ID;
        }
        this.osId = osId;
    }

    @Column(name = "`VERSION`", nullable = false)
    public String getVersion() {
        return this.version;
    }

    @Column(name = "`VERSION`", nullable = false)
    public void setVersion(String version) {
        this.version = version;
    }

    @Column(name = "`PRECEDENCE`", nullable = true)
    public Integer getPrecedence() {
        return this.precedence;
    }

    @Column(name = "`PRECEDENCE`", nullable = true)
    public void setPrecedence(Integer precedence) {
        this.precedence = precedence;
    }

    @Column(name = "`CLUSTER_TYPE`", nullable = false)
    public String getClusterType() {
        return this.clusterType;
    }

    @Column(name = "`CLUSTER_TYPE`", nullable = false)
    public void setClusterType(String clusterType) {
        this.clusterType = clusterType;
    }

    @Column(name = "`SUPERVISOR_ID`", nullable = false)
    public Long getSupervisorId() {
        return this.supervisorId;
    }

    @Column(name = "`SUPERVISOR_ID`", nullable = false)
    public void setSupervisorId(Long supervisorId) {
        if (supervisorId == null) {
            supervisorId = InventoryDBItemConstants.DEFAULT_ID;
        }
        this.supervisorId = supervisorId;
    }

    @Column(name = "`TIMEZONE`", nullable = false)
    public String getTimeZone() {
        return this.timeZone;
    }

    @Column(name = "`TIMEZONE`", nullable = false)
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }
    
    @Column(name = "`AUTH`", nullable = true)
    public String getAuth() {
        return this.auth;
    }

    @Column(name = "`AUTH`", nullable = true)
    public void setAuth(String auth) {
        this.auth = auth;
    }
    
    @Transient
    public String originalUrl() {
        return this.origUrl;
    }

    @Transient
    public void setOriginalUrl(String origUrl) {
        this.origUrl = origUrl;
    }

    public String toDebugString() {
        StringBuilder strb = new StringBuilder();
        strb.append("ID:").append(getId()).append("|");
        strb.append("SCHEDULER_ID:").append(getSchedulerId()).append("|");
        strb.append("HOSTNAME:").append(getHostname()).append("|");
        strb.append("PORT:").append(getPort()).append("|");
        strb.append("OS_ID:").append(getOsId()).append("|");
        strb.append("LIVE_DIRECTORY:").append(getLiveDirectory()).append("|");
        strb.append("VERSION:").append(getVersion()).append("|");
        strb.append("COMMAND_URL:").append(getCommandUrl()).append("|");
        strb.append("URL:").append(getUrl()).append("|");
        strb.append("AUTH:").append("***").append("|");
        strb.append("TIMEZONE:").append(getTimeZone()).append("|");
        strb.append("CLUSTER_TYPE:").append(getClusterType()).append("|");
        strb.append("PRECEDENCE:").append(getPrecedence()).append("|");
        strb.append("DBMS_NAME:").append(getDbmsName()).append("|");
        strb.append("DBMS_VERSION:").append(getDbmsVersion()).append("|");
        strb.append("STARTED_AT:").append(getStartedAt()).append("|");
        strb.append("SUPERVISOR_ID:").append(getSupervisorId()).append("|");
        strb.append("CREATED:").append(getCreated()).append("|");
        strb.append("MODIFIED:").append(getModified());
        return strb.toString();
    }
   
    @Transient
    public String clusterMemberId() {
        if (origUrl != null) {
            return String.format("%s/%s:%s", schedulerId, hostname, origUrl.replaceFirst(".*:(\\d+)$", "$1"));
        } else {
            return String.format("%s/%s:%s", schedulerId, hostname, url.replaceFirst(".*:(\\d+)$", "$1"));
        }
    }
   
    @Override
    public int hashCode() {
        // always build on unique constraint
        return new HashCodeBuilder().append(schedulerId).append(hostname).append(port).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        // always compare on unique constraint
        if (other == this) {
            return true;
        }
        if (!(other instanceof DBItemInventoryInstance)) {
            return false;
        }
        DBItemInventoryInstance rhs = ((DBItemInventoryInstance) other);
        return new EqualsBuilder().append(schedulerId, rhs.schedulerId).append(hostname, rhs.hostname).append(port, rhs.port).isEquals();
    }

}