package com.sos.joc.db.history;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.common.HistoryConstants;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_HISTORY_AGENTS)
@Proxy(lazy = false)
public class DBItemHistoryAgent extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[READY_EVENT_ID]", nullable = false)
    private Long readyEventId;

    @Id
    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId; // TABLE_HISTORY_CONTROLLERS.CONTROLLER_ID

    @Column(name = "[AGENT_ID]", nullable = false)
    private String agentId;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[TIMEZONE]", nullable = false)
    private String timezone;

    @Column(name = "[READY_TIME]", nullable = false)
    private Date readyTime;

    @Column(name = "[COUPLING_FAILED_TIME]", nullable = true)
    private Date couplingFailedTime;

    @Column(name = "[COUPLING_FAILED_MESSAGE]", nullable = true)
    private String couplingFailedMessage;

    @Column(name = "[SHUTDOWN_TIME]", nullable = true)
    private Date shutdownTime;

    @Column(name = "[LAST_KNOWN_TIME]", nullable = true)
    private Date lastKnownTime;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemHistoryAgent() {
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

    public String getAgentId() {
        return agentId;
    }

    public void setAgentId(String val) {
        agentId = val;
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

    public Date getCouplingFailedTime() {
        return couplingFailedTime;
    }

    public void setCouplingFailedTime(Date val) {
        couplingFailedTime = val;
    }

    public String getCouplingFailedMessage() {
        return couplingFailedMessage;
    }

    public void setCouplingFailedMessage(String val) {
        couplingFailedMessage = normalizeValue(val, HistoryConstants.MAX_LEN_ERROR_TEXT);
    }

    public Date getShutdownTime() {
        return shutdownTime;
    }

    public void setShutdownTime(Date val) {
        shutdownTime = val;
    }

    public Date getLastKnownTime() {
        return lastKnownTime;
    }

    public void setLastKnownTime(Date val) {
        lastKnownTime = val;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getCreated() {
        return created;
    }
}
