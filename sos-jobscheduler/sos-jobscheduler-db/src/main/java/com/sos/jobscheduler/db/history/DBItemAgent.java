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

import org.hibernate.annotations.Type;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.HISTORY_TABLE_AGENTS)
@SequenceGenerator(name = DBLayer.HISTORY_TABLE_AGENTS_SEQUENCE, sequenceName = DBLayer.HISTORY_TABLE_AGENTS_SEQUENCE, allocationSize = 1)
public class DBItemAgent implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db id

    private String masterId; // HISTORY_TABLE_MASTERS.MASTER_ID
    private String agentKey;
    private String uri;
    private String timezone;
    private Date startTime;
    private boolean lastEntry;
    private Date created;

    public DBItemAgent() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_AGENTS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_AGENTS_SEQUENCE)
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

    @Column(name = "`AGENT_KEY`", nullable = false)
    public String getAgentKey() {
        return agentKey;
    }

    @Column(name = "`AGENT_KEY`", nullable = false)
    public void setAgentKey(String val) {
        agentKey = val;
    }

    @Column(name = "`URI`", nullable = false)
    public String getUri() {
        return uri;
    }

    @Column(name = "`URI`", nullable = false)
    public void setUri(String val) {
        uri = val;
    }

    @Column(name = "`TIMEZONE`", nullable = false)
    public String getTimezone() {
        return timezone;
    }

    @Column(name = "`TIMEZONE`", nullable = false)
    public void setTimezone(String val) {
        timezone = val;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public Date getStartTime() {
        return startTime;
    }

    @Column(name = "`START_TIME`", nullable = false)
    public void setStartTime(Date val) {
        startTime = val;
    }

    @Column(name = "`LAST_ENTRY`", nullable = false)
    @Type(type = "numeric_boolean")
    public void setLastEntry(boolean val) {
        lastEntry = val;
    }

    @Column(name = "`LAST_ENTRY`", nullable = false)
    @Type(type = "numeric_boolean")
    public boolean getLastEntry() {
        return lastEntry;
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
        if (o == null || !(o instanceof DBItemAgent)) {
            return false;
        }
        DBItemAgent item = (DBItemAgent) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
