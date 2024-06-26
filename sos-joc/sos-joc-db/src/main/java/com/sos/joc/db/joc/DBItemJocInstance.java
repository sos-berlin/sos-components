package com.sos.joc.db.joc;

import java.util.Date;

import org.hibernate.annotations.Proxy;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_JOC_INSTANCES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[MEMBER_ID]" }) })
@Proxy(lazy = false)
public class DBItemJocInstance extends DBItem {

    private static final long serialVersionUID = 1L;

    private static final String DEFAULT_SECURITY_LEVEL = "low";

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_JOC_INSTANCES_SEQUENCE)
    private Long id;

    @Column(name = "[CLUSTER_ID]", nullable = false)
    private String clusterId;

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

    @Column(name = "[ORDERING]", nullable = false)
    private Integer ordering;

    @Column(name = "[URI]", nullable = true)
    private String uri;

    @Column(name = "[HEART_BEAT]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date heartBeat;

    @Column(name = "[API_SERVER]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean apiServer;

    @Column(name = "[VERSION]", nullable = true)
    private String version;

    @Column(name = "[CERTIFICATE]", nullable = true)
    private String certificate;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getClusterId() {
        return clusterId;
    }

    public void setClusterId(String val) {
        clusterId = val;
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
        if (val != null && val.length() > 30) {
            val = val.substring(0, 30);
        }
        title = val;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer val) {
        if (val == null) {
            val = 0;
        }
        // crop to a tiny int between [-128, 127]
        Math.min(Math.max(val, -128), 127);
        ordering = val;
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

    public void setApiServer(boolean val) {
        apiServer = val;
    }

    public boolean getApiServer() {
        return apiServer;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String val) {
        if (val != null && val.length() > 30) {
            val = val.substring(0, 30);
        }
        version = val;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

}