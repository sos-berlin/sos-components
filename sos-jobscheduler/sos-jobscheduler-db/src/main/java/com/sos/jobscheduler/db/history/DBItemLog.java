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

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.HISTORY_TABLE_LOGS)
@SequenceGenerator(name = DBLayer.HISTORY_TABLE_LOGS_SEQUENCE, sequenceName = DBLayer.HISTORY_TABLE_LOGS_SEQUENCE, allocationSize = 1)
public class DBItemLog implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db id

    private String masterId;
    private String orderKey;// event
    /** Foreign key - HISTORY_TABLE_ORDERS.ID */
    private Long mainOrderHistoryId;
    private Long orderHistoryId;// db
    /** Foreign key - HISTORY_TABLE_ORDER_STEPS.ID */
    private Long orderStepHistoryId;// db
    /** Others */
    private Long logType; // see enum LogType
    private Long outType; // see enum OutType
    private Long logLevel; // see enum LogLevel
    private String jobPath;
    private String agentUri;
    private String timezone;
    private String eventId;
    private String eventTimestamp;
    private Date chunkDatetime;
    private String chunk;
    private String constraintHash; // hash from masterId, eventId, logType, row number for db unique constraint

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

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_LOGS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_LOGS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    @Column(name = "`MASTER_ID`", nullable = false)
    public String getMasterId() {
        return masterId;
    }

    @Column(name = "`MASTER_ID`", nullable = false)
    public void setMasterId(String val) {
        masterId = val;
    }

    @Column(name = "`ORDER_KEY`", nullable = false)
    public String getOrderKey() {
        return orderKey;
    }

    @Column(name = "`ORDER_KEY`", nullable = false)
    public void setOrderKey(String val) {
        orderKey = val;
    }

    /** Foreign key */
    @Column(name = "`MAIN_ORDER_HISTORY_ID`", nullable = false)
    public Long getMainOrderHistoryId() {
        return mainOrderHistoryId;
    }

    @Column(name = "`MAIN_ORDER_HISTORY_ID`", nullable = false)
    public void setMainOrderHistoryId(Long val) {
        mainOrderHistoryId = val;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    @Column(name = "`ORDER_HISTORY_ID`", nullable = false)
    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    @Column(name = "`ORDER_STEP_HISTORY_ID`", nullable = false)
    public Long getOrderStepHistoryId() {
        return orderStepHistoryId;
    }

    @Column(name = "`ORDER_STEP_HISTORY_ID`", nullable = false)
    public void setOrderStepHistoryId(Long val) {
        orderStepHistoryId = val;
    }

    /** Others */
    @Column(name = "`LOG_TYPE`", nullable = false)
    public Long getLogType() {
        return logType;
    }

    @Column(name = "`LOG_TYPE`", nullable = false)
    public void setLogType(Long val) {
        logType = val;
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

    @Column(name = "`TIMEZONE`", nullable = false)
    public String getTimezone() {
        return timezone;
    }

    @Column(name = "`TIMEZONE`", nullable = false)
    public void setTimezone(String val) {
        timezone = val;
    }

    @Column(name = "`LOG_LEVEL`", nullable = false)
    public Long getLogLevel() {
        return logLevel;
    }

    @Column(name = "`LOG_LEVEL`", nullable = false)
    public void setLogLevel(Long val) {
        logLevel = val;
    }

    @Column(name = "`OUT_TYPE`", nullable = false)
    public Long getOutType() {
        return outType;
    }

    @Column(name = "`OUT_TYPE`", nullable = false)
    public void setOutType(Long val) {
        outType = val;
    }

    @Column(name = "`EVENT_ID`", nullable = false)
    public String getEventId() {
        return eventId;
    }

    @Column(name = "`EVENT_ID`", nullable = false)
    public void setEventId(String val) {
        eventId = val;
    }

    @Column(name = "`EVENT_TIMESTAMP`", nullable = true)
    public String getEventTimestamp() {
        return eventTimestamp;
    }

    @Column(name = "`EVENT_TIMESTAMP`", nullable = true)
    public void setEventTimestamp(String val) {
        eventTimestamp = val;
    }

    @Column(name = "`CHUNK_DATETIME`", nullable = false)
    public Date getChunkDatetime() {
        return chunkDatetime;
    }

    @Column(name = "`CHUNK_DATETIME`", nullable = false)
    public void setChunkDatetime(Date val) {
        chunkDatetime = val;
    }

    @Column(name = "`CHUNK`", nullable = false)
    public String getChunk() {
        return chunk;
    }

    @Column(name = "`CHUNK`", nullable = false)
    public void setChunk(String val) {
        chunk = val;
    }

    @Column(name = "`CONSTRAINT_HASH`", nullable = false)
    public String getConstraintHash() {
        return constraintHash;
    }

    @Column(name = "`CONSTRAINT_HASH`", nullable = false)
    public void setConstraintHash(String val) {
        constraintHash = val;
    }

    @Column(name = "`CREATED`", nullable = false)
    public void setCreated(Date val) {
        created = val;
    }

    @Column(name = "`CREATED`", nullable = false)
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
