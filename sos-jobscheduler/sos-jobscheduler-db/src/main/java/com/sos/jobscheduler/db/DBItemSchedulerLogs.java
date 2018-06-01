package com.sos.jobscheduler.db;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = DBLayer.TABLE_SCHEDULER_LOGS)
@SequenceGenerator(name = DBLayer.TABLE_SCHEDULER_LOGS_SEQUENCE, sequenceName = DBLayer.TABLE_SCHEDULER_LOGS_SEQUENCE, allocationSize = 1)
public class DBItemSchedulerLogs implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db id
    /** Identifier */
    private String schedulerId;
    private String orderKey;// event
    /** Foreign key - TABLE_SCHEDULER_ORDER_HISTORY.ID */
    private Long mainOrderHistoryId;
    private Long orderHistoryId;// db
    /** Foreign key - TABLE_SCHEDULER_ORDER_STEP_HISTORY.ID */
    private Long orderStepHistoryId;// db
    /** Others */
    private Long logType; // see enum LogType
    private Long outType; // see enum OutType
    private Long logLevel; // see enum LogLevel
    private String jobPath;
    private String agentUri;
    private String agentTimezone;
    private Date chunkTimestamp;
    private String chunk;
    private String constraintHash; // hash from schedulerId, eventId, logType, row number for db unique constraint

    private Date created;

    public static enum LogType {
        OrderAdded(0), OrderStart(1), OrderStepStart(2), OrderStepStd(3), OrderStepEnd(4), OrderEnd(5);

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

    public DBItemSchedulerLogs() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_LOGS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_SCHEDULER_LOGS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public void setId(Long val) {
        id = val;
    }

    /** Identifier */
    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public String getSchedulerId() {
        return schedulerId;
    }

    @Column(name = "`SCHEDULER_ID`", nullable = false)
    public void setSchedulerId(String val) {
        schedulerId = val;
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

    @Column(name = "`AGENT_TIMEZONE`", nullable = false)
    public String getAgentTimezone() {
        return agentTimezone;
    }

    @Column(name = "`AGENT_TIMEZONE`", nullable = false)
    public void setAgentTimezone(String val) {
        agentTimezone = val;
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

    @Column(name = "`CHUNK_TIMESTAMP`", nullable = false)
    public Date getChunkTimestamp() {
        return chunkTimestamp;
    }

    @Column(name = "`CHUNK_TIMESTAMP`", nullable = false)
    public void setChunkTimestamp(Date val) {
        chunkTimestamp = val;
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
        if (o == null || !(o instanceof DBItemSchedulerLogs)) {
            return false;
        }
        DBItemSchedulerLogs item = (DBItemSchedulerLogs) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
