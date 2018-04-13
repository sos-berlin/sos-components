package com.sos.jobscheduler.history.master.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY)
@SequenceGenerator(name = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE, allocationSize = 1)
public class DBItemSchedulerOrderStepHistory implements Serializable {

    private static final long serialVersionUID = 1L;

    /** primary key */
    private Long id;
    /** foreign key - SCHEDULER_ORDER_HISTORY.ID */
    private Long orderHistoryId;
    /** identifier */
    private String key;
    private String position;
    /** others */
    private String jobPath;
    private String agentUri;
    private String startCause;
    private Date startTime;
    private Date endTime;
    private String startParameters;
    private String endParameters;
    private String status;
    private Long returnCode;
    private byte[] log;

    private Date created;
    private Date modified;

    public DBItemSchedulerOrderStepHistory() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_ORDER_STEP_HISTORY_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    @Column(name = "`KEY`", nullable = false)
    public String getKey() {
        return key;
    }

    @Column(name = "`KEY`", nullable = false)
    public void setKey(String val) {
        key = val;
    }

    @Column(name = "`POSITION`", nullable = false)
    public String getPosition() {
        return position;
    }

    @Column(name = "`POSITION`", nullable = false)
    public void setPosition(String val) {
        position = val;
    }

    @Column(name = "`JOB_PATH`", nullable = false)
    public String getJobPath() {
        return jobPath;
    }

    @Column(name = "`JOB_PATH`", nullable = false)
    public void setJobPath(String val) {
        jobPath = val;
    }

    @Column(name = "`AGENT_URI`", nullable = false)
    public String getAgentUri() {
        return agentUri;
    }

    @Column(name = "`AGENT_URI`", nullable = false)
    public void setAgentUri(String val) {
        agentUri = val;
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

    @Column(name = "`START_PARAMETERS`", nullable = true)
    public String getStartParameters() {
        return startParameters;
    }

    @Column(name = "`START_PARAMETERS`", nullable = true)
    public void setStartParameters(String val) {
        startParameters = val;
    }

    @Column(name = "`END_PARAMETERS`", nullable = true)
    public String getEndParameters() {
        return endParameters;
    }

    @Column(name = "`END_PARAMETERS`", nullable = true)
    public void setEndParameters(String val) {
        endParameters = val;
    }

    @Column(name = "`STATUS`", nullable = false)
    public String getStatus() {
        return status;
    }

    @Column(name = "`STATUS`", nullable = false)
    public void setStatus(String val) {
        status = val;
    }

    @Column(name = "`RETURN_CODE`", nullable = false)
    public Long getReturnCode() {
        return returnCode;
    }

    @Column(name = "`RETURN_CODE`", nullable = false)
    public void setReturnCode(Long val) {
        if (val == null) {
            val = new Long(0);
        }
        returnCode = val;
    }

    @Lob
    @Column(name = "`LOG`", nullable = true)
    public byte[] getLog() {
        return log;
    }

    @Lob
    @Column(name = "`LOG`", nullable = true)
    public void setLog(byte[] val) {
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
