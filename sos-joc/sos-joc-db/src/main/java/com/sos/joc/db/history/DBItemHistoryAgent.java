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

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_HISTORY_AGENTS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[READY_EVENT_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_HISTORY_AGENTS_SEQUENCE, sequenceName = DBLayer.TABLE_HISTORY_AGENTS_SEQUENCE, allocationSize = 1)
public class DBItemHistoryAgent extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_HISTORY_AGENTS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId; // HISTORY_TABLE_CONTROLLERS.CONTROLLER_ID

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

    @Column(name = "[READY_EVENT_ID]", nullable = false)
    private String readyEventId;

    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    public DBItemHistoryAgent() {
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
