package com.sos.jobscheduler.master.history.db;

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

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY)
@SequenceGenerator(name = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE, allocationSize = 1)
public class DBItemSchedulerOrderHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** primary key */
    private Long id;
    /** foreign keys - SCHEDULER_ORDER_HISTORY.ID, KEY */
    private Long parentId;
    private String parentKey;
    /** identifier */
    private String key;
    /** others */
    private String name;
    private String workflowPath;
    private String startCause;
    private Date startTime;
    private Date endTime;
    private Date plannedStartTime;
    private String status;
    private String log;

    private Date created;
    private Date modified;

    public DBItemSchedulerOrderHistory() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_ORDER_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    @Column(name = "`PARENT_ID`", nullable = false)
    public Long getParentId() {
        return parentId;
    }

    @Column(name = "`PARENT_ID`", nullable = false)
    public void setParentId(Long val) {
        parentId = val;
    }

    @Column(name = "`PARENT_KEY`", nullable = false)
    public String getParentKey() {
        return parentKey;
    }

    @Column(name = "`PARENT_KEY`", nullable = false)
    public void setParentKey(String val) {
        parentKey = val;
    }

    @Column(name = "`KEY`", nullable = false)
    public String getKey() {
        return key;
    }

    @Column(name = "`KEY`", nullable = false)
    public void setKey(String val) {
        key = val;
    }

    @Column(name = "`NAME`", nullable = false)
    public String getName() {
        return name;
    }

    @Column(name = "`NAME`", nullable = false)
    public void setName(String val) {
        name = val;
    }

    @Column(name = "`WORKFLOW_PATH`", nullable = false)
    public String getWorkflowPath() {
        return workflowPath;
    }

    @Column(name = "`WORKFLOW_PATH`", nullable = false)
    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    @Column(name = "`START_CAUSE`", nullable = false)
    public String getStartCause() {
        return startCause;
    }

    @Column(name = "`START_CAUSE`", nullable = false)
    public void setStartCause(String val) {
        startCause = val;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public void setStartTime(Date val) {
        startTime = val;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public Date getEndTime() {
        return endTime;
    }

    @Column(name = "`END_TIME`", nullable = true)
    public void setEndTime(Date val) {
        endTime = val;
    }

    @Column(name = "`PLANNED_START_TIME`", nullable = true)
    public Date getPlannedStartTime() {
        return plannedStartTime;
    }

    @Column(name = "`PLANNED_START_TIME`", nullable = true)
    public void setPlannedStartTime(Date val) {
        plannedStartTime = val;
    }

    @Column(name = "`STATUS`", nullable = false)
    public String getStatus() {
        return status;
    }

    @Column(name = "`STATUS`", nullable = false)
    public void setStatus(String val) {
        status = val;
    }

    @Column(name = "`LOG`", nullable = false)
    public String getLog() {
        return log;
    }

    @Column(name = "`LOG`", nullable = false)
    public void setLog(String val) {
        log = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`CREATED`", nullable = false)
    public Date getCreated() {
        return created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public void setModified(Date val) {
        modified = val;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "`MODIFIED`", nullable = false)
    public Date getModified() {
        return modified;
    }
}
