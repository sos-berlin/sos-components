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

import org.hibernate.annotations.Type;

import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.HISTORY_TABLE_MASTERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[EVENT_ID]" }) })
@SequenceGenerator(name = DBLayer.HISTORY_TABLE_MASTERS_SEQUENCE, sequenceName = DBLayer.HISTORY_TABLE_MASTERS_SEQUENCE, allocationSize = 1)
public class DBItemMaster implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Primary key */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.HISTORY_TABLE_MASTERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;// db id

    @Column(name = "[MASTER_ID]", nullable = false)
    private String masterId;

    @Column(name = "[HOSTNAME]", nullable = false)
    private String hostname;

    @Column(name = "[PORT]", nullable = false)
    private Long port;

    @Column(name = "[TIMEZONE]", nullable = false)
    private String timezone;

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;

    @Column(name = "[LAST_ENTRY]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean lastEntry;

    @Column(name = "[EVENT_ID]", nullable = false)
    private String eventId;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemMaster() {
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

    public String getHostname() {
        return hostname;
    }

    public void setHostname(String val) {
        hostname = val;
    }

    public Long getPort() {
        return port;
    }

    public void setPort(Long val) {
        port = val;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String val) {
        timezone = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

    public void setLastEntry(boolean val) {
        lastEntry = val;
    }

    public boolean getLastEntry() {
        return lastEntry;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String val) {
        eventId = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

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
