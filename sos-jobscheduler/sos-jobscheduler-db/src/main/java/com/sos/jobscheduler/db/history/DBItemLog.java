package com.sos.jobscheduler.db.history;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.HISTORY_TABLE_LOGS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
@SequenceGenerator(name = DBLayer.HISTORY_TABLE_LOGS_SEQUENCE, sequenceName = DBLayer.HISTORY_TABLE_LOGS_SEQUENCE, allocationSize = 1)
public class DBItemLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_LOGS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;// db id

    @Column(name = "[MASTER_ID]", nullable = false)
    private String masterId;

    @Column(name = "[ORDER_KEY`", nullable = false)
    private String orderKey;// event

    /** Foreign key - HISTORY_TABLE_ORDERS.ID */
    @Column(name = "[MAIN_ORDER_HISTORY_ID]", nullable = false)
    private Long mainOrderHistoryId;

    @Column(name = "[ORDER_HISTORY_ID]", nullable = false)
    private Long orderHistoryId;// db

    /** Foreign key - HISTORY_TABLE_ORDER_STEPS.ID */
    @Column(name = "[ORDER_STEP_HISTORY_ID]", nullable = false)
    private Long orderStepHistoryId;// db

    /** Others */
    @Column(name = "[LOG_TYPE]", nullable = false)
    private Long logType; // see enum LogType

    @Column(name = "[OUT_TYPE]", nullable = false)
    private Long outType; // see enum OutType

    @Column(name = "[LOG_LEVEL]", nullable = false)
    private Long logLevel; // see enum LogLevel

    @Column(name = "[JOB_PATH]", nullable = false)
    private String jobPath;

    @Column(name = "[AGENT_URI]", nullable = false)
    private String agentUri;

    @Column(name = "[TIMEZONE]", nullable = false)
    private String timezone;

    @Column(name = "[EVENT_ID]", nullable = false)
    private String eventId;

    @Column(name = "[EVENT_TIMESTAMP]", nullable = true)
    private String eventTimestamp;

    @Column(name = "[CHUNK_DATETIME]", nullable = false)
    private Date chunkDatetime;

    @Column(name = "[CHUNK]", nullable = false)
    private String chunk;

    @Column(name = "[CONSTRAINT_HASH]", nullable = false)
    private String constraintHash; // hash from masterId, eventId, logType, row number for db unique constraint

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public static enum LogType {
        MasterReady(0), AgentReady(1), OrderAdded(2), OrderStart(3), OrderForked(4), OrderStepStart(5), OrderStepStd(6), OrderStepEnd(7), OrderJoined(
                8), OrderEnd(9);

        private int value;

        private LogType(int val) {
            value = val;
        }

        public Long getValue() {
            return new Long(value);
        }
    }

    public static enum OutType {
        Stdout(0), Stderr(1);

        private int value;

        private OutType(int val) {
            value = val;
        }

        public Long getValue() {
            return new Long(value);
        }
    }

    public static enum LogLevel {
        Info(0), Debug(1), Error(2), Warn(3), Trace(4);

        private int value;

        private LogLevel(int val) {
            value = val;
        }

        public Long getValue() {
            return new Long(value);
        }
    }

    public DBItemLog() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String val) {
        masterId = val;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String val) {
        orderKey = val;
    }

    public Long getMainOrderHistoryId() {
        return mainOrderHistoryId;
    }

    public void setMainOrderHistoryId(Long val) {
        mainOrderHistoryId = val;
    }

    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    public Long getOrderStepHistoryId() {
        return orderStepHistoryId;
    }

    public void setOrderStepHistoryId(Long val) {
        orderStepHistoryId = val;
    }

    /** Others */
    public Long getLogType() {
        return logType;
    }

    public void setLogType(Long val) {
        logType = val;
    }

    public String getJobPath() {
        return jobPath;
    }

    public void setJobPath(String val) {
        jobPath = val;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public void setAgentUri(String val) {
        agentUri = val;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String val) {
        timezone = val;
    }

    public Long getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(Long val) {
        logLevel = val;
    }

    public Long getOutType() {
        return outType;
    }

    public void setOutType(Long val) {
        outType = val;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String val) {
        eventId = val;
    }

    public String getEventTimestamp() {
        return eventTimestamp;
    }

    public void setEventTimestamp(String val) {
        eventTimestamp = val;
    }

    public Date getChunkDatetime() {
        return chunkDatetime;
    }

    public void setChunkDatetime(Date val) {
        chunkDatetime = val;
    }

    public String getChunk() {
        return chunk;
    }

    public void setChunk(String val) {
        chunk = val;
    }

    public String getConstraintHash() {
        return constraintHash;
    }

    public void setConstraintHash(String val) {
        constraintHash = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof DBItemLog)) {
            return false;
        }
        DBItemLog item = (DBItemLog) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
