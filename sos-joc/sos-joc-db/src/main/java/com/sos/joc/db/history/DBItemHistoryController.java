package com.sos.joc.db.history;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_CONTROLLERS)
public class DBItemHistoryController extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[READY_EVENT_ID]", nullable = false)
    private Long readyEventId;

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

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemHistoryController() {
    }

    public Long getReadyEventId() {
        return readyEventId;
    }

    public void setReadyEventId(Long val) {
        readyEventId = val;
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

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }
}
