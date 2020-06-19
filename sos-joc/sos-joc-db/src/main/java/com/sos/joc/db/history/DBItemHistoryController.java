package com.sos.joc.db.history;

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

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_CONTROLLERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[EVENT_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_HISTORY_CONTROLLERS_SEQUENCE, sequenceName = DBLayer.TABLE_HISTORY_CONTROLLERS_SEQUENCE, allocationSize = 1)
public class DBItemHistoryController extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_HISTORY_CONTROLLERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[JOBSCHEDULER_ID]", nullable = false)
    private String jobSchedulerId;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[TIMEZONE]", nullable = false)
    private String timezone;

    @Column(name = "[START_TIME]", nullable = false)
    private Date startTime;

    @Column(name = "[PRIMARY_MASTER]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean primaryMaster;

    @Column(name = "[EVENT_ID]", nullable = false)
    private String eventId;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemHistoryController() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getJobSchedulerId() {
        return jobSchedulerId;
    }

    public void setJobSchedulerId(String val) {
        jobSchedulerId = val;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        uri = val;
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

    public void setPrimaryMaster(boolean val) {
        primaryMaster = val;
    }

    public boolean getPrimaryMaster() {
        return primaryMaster;
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
}
