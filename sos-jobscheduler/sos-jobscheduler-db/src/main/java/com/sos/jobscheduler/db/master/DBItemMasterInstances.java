package com.sos.jobscheduler.db.master;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.MASTER_INSTANCES_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SCHEDULER_ID]", "[URI]" }) })
@SequenceGenerator(name = DBLayer.MASTER_INSTANCES_TABLE_SEQUENCE, sequenceName = DBLayer.MASTER_INSTANCES_TABLE_SEQUENCE, allocationSize = 1)
public class DBItemMasterInstances extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.MASTER_INSTANCES_TABLE_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[SCHEDULER_ID]", nullable = false)
    private String schedulerId;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    /* foreign key INVENTORY_OPERTATION_SYSTEM.ID */
    @Column(name = "[OS_ID]", nullable = false)
    private Long osId;

    @Column(name = "[VERSION]", nullable = true)
    private String version;

    @Column(name = "[TIMEZONE]", nullable = true)
    private String timezone;

    @Column(name = "[STARTED_AT]", nullable = true)
    private Date startedAt;

    /* 0=Single, 1=Primary, 2=Backup */
    @Column(name = "[CLUSTER_TYPE]", nullable = false)
    private Integer clusterType;

    @Column(name = "[MODIFIED]", nullable = false)
    private Date modified;

    public DBItemMasterInstances() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getSchedulerId() {
        return schedulerId;
    }

    public void setSchedulerIdId(String val) {
    	schedulerId = val;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        uri = val;
    }

    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long val) {
    	if (val == null) {
            val = 0L;
        }
    	osId = val;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String val) {
    	version = val;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String val) {
        timezone = val;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date val) {
    	startedAt = val;
    }

    public void setClusterType(Integer val) {
    	if (val == null) {
            val = 0;
        }
    	clusterType = val;
    }

    public Integer getClusterType() {
        return clusterType;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }
}
