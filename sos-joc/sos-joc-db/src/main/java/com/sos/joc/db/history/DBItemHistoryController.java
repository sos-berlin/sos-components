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
@Table(name = DBLayer.TABLE_HISTORY_CONTROLLERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[READY_EVENT_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_HISTORY_CONTROLLERS_SEQUENCE, sequenceName = DBLayer.TABLE_HISTORY_CONTROLLERS_SEQUENCE, allocationSize = 1)
public class DBItemHistoryController extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_HISTORY_CONTROLLERS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[TIMEZONE]", nullable = false)
    private String timezone;

    @Column(name = "[READY_TIME]", nullable = false)
    private Date readyTime;

    @Column(name = "[SHUTDOWN_TIME]", nullable = true)
    private Date shutdownTime;

    @Column(name = "[IS_PRIMARY]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean isPrimary;

    @Column(name = "[READY_EVENT_ID]", nullable = false)
    private String readyEventId;

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

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
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

    public Date getReadyTime() {
        return readyTime;
    }

    public void setReadyTime(Date val) {
        readyTime = val;
    }

    public Date getShutdownTime() {
        return shutdownTime;
    }

    public void setShutdownTime(Date val) {
        shutdownTime = val;
    }

    public void setIsPrimary(boolean val) {
        isPrimary = val;
    }

    public boolean getIsPrimary() {
        return isPrimary;
    }

    public String getReadyEventId() {
        return readyEventId;
    }

    public void setReadyEventId(String val) {
        readyEventId = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }
}
