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
@Table(name = DBLayer.HISTORY_TABLE_MASTERS)
@SequenceGenerator(name = DBLayer.HISTORY_TABLE_MASTERS_SEQUENCE, sequenceName = DBLayer.HISTORY_TABLE_MASTERS_SEQUENCE, allocationSize = 1)
public class DBItemMaster implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    private Long id;// db id

    private String masterId;
    private String hostname;
    private Long port;
    private String timezone;
    private Date startTime;
    private boolean lastEntry;
    private String eventId;
    private Date created;

    public DBItemMaster() {
    }

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_MASTERS_SEQUENCE)
    @Column(name = "`ID`", nullable = false)
    public Long getId() {
        return id;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_MASTERS_SEQUENCE)
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

    @Column(name = "`HOSTNAME`", nullable = false)
    public String getHostname() {
        return hostname;
    }

    @Column(name = "`HOSTNAME`", nullable = false)
    public void setHostname(String val) {
        hostname = val;
    }

    @Column(name = "`PORT`", nullable = false)
    public Long getPort() {
        return port;
    }

    @Column(name = "`PORT`", nullable = false)
    public void setPort(Long val) {
        port = val;
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

    @Column(name = "`EVENT_ID`", nullable = false)
    public String getEventId() {
        return eventId;
    }

    @Column(name = "`EVENT_ID`", nullable = false)
    public void setEventId(String val) {
        eventId = val;
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
        if (o == null || !(o instanceof DBItemMaster)) {
            return false;
        }
        DBItemMaster item = (DBItemMaster) o;
        if (!getId().equals(item.getId())) {
            return false;
        }
        return true;
    }

    public int hashCode() {
        return getId() == null ? new Long(0).hashCode() : getId().hashCode();
    }
}
