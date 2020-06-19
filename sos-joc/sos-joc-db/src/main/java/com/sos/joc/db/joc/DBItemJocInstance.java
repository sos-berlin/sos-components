package com.sos.joc.db.joc;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_JOC_INSTANCES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[MEMBER_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_JOC_INSTANCES_SEQUENCE, sequenceName = DBLayer.TABLE_JOC_INSTANCES_SEQUENCE, allocationSize = 1)
public class DBItemJocInstance extends DBItem {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_SECURITY_LEVEL = "low";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOC_INSTANCES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[MEMBER_ID]", nullable = false)
    private String memberId;// host:appData

    /* foreign key INVENTORY_OPERTATION_SYSTEM.ID */
    @Column(name = "[OS_ID]", nullable = false)
    private Long osId;

    @Column(name = "[DATA_DIRECTORY]", nullable = false)
    private String dataDirectory;

    @Column(name = "[SECURITY_LEVEL]", nullable = false)
    private String securityLevel;

    @Column(name = "[TIMEZONE]", nullable = false)
    private String timezone;

    @Column(name = "[STARTED_AT]", nullable = false)
    private Date startedAt;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[URI]", nullable = true)
    private String uri;

    @Column(name = "[HEART_BEAT]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date heartBeat;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String val) {
        memberId = val;
    }

    public Long getOsId() {
        return osId;
    }

    public void setOsId(Long val) {
        if (val == null) {
            val = 0L;
        }
        osId = val;
    }

    public String getDataDirectory() {
        return dataDirectory;
    }

    public void setDataDirectory(String val) {
        dataDirectory = val;
    }

    public String getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(String val) {
        if (SOSString.isEmpty(val)) {
            val = DEFAULT_SECURITY_LEVEL;
        }
        securityLevel = val;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String val) {
        timezone = val;
    }

    public Date getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Date val) {
        startedAt = val;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        uri = val;
    }

    public void setHeartBeat(Date val) {
        heartBeat = val;
    }

    public Date getHeartBeat() {
        return heartBeat;
    }

}