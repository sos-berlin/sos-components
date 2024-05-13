package com.sos.joc.db.inventory;

import java.util.Date;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.type.NumericBooleanConverter;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_INV_JS_INSTANCES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[SECURITY_LEVEL]", "[URI]" }) })
@SequenceGenerator(name = DBLayer.TABLE_INV_JS_INSTANCES_SEQUENCE, sequenceName = DBLayer.TABLE_INV_JS_INSTANCES_SEQUENCE, allocationSize = 1)
public class DBItemInventoryJSInstance extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_INV_JS_INSTANCES_SEQUENCE)
    @GenericGenerator(name = DBLayer.TABLE_INV_JS_INSTANCES_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[SECURITY_LEVEL]", nullable = false)
    private Integer securityLevel;

    @Column(name = "[URI]", nullable = false)
    private String uri;

    @Column(name = "[CLUSTER_URI]", nullable = false)
    private String clusterUri;

    /* foreign key INVENTORY_OPERTATION_SYSTEM.ID */
    @Column(name = "[OS_ID]", nullable = false)
    private Long osId;

    @Column(name = "[VERSION]", nullable = true)
    private String version;

    @Column(name = "[JAVA_VERSION]", nullable = true)
    private String javaVersion;

    @Column(name = "[STARTED_AT]", nullable = true)
    private Date startedAt;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    /* 0=Single, 1=Cluster */
    @Column(name = "[IS_CLUSTER]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean isCluster;

    /* 0=Backup, 1=Primary */
    @Column(name = "[IS_PRIMARY]", nullable = false)
    @Convert(converter = NumericBooleanConverter.class)
    private boolean isPrimary;

    @Column(name = "[CERTIFICATE]", nullable = true)
    private String certificate;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;

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

    public Integer getSecurityLevel() {
        return securityLevel;
    }

    public void setSecurityLevel(Integer val) {
        securityLevel = val;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String val) {
        if (val != null && !"/".equals(val)) {
            val = val.replaceFirst("/$", "");
        }
        uri = val;
    }

    public String getClusterUri() {
        return clusterUri;
    }

    public void setClusterUri(String val) {
        clusterUri = val;
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String val) {
        if (val != null && val.length() > 30) {
            val = val.substring(0, 30);
        }
        version = val;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String val) {
        if (val != null && val.length() > 30) {
            val = val.substring(0, 30);
        }
        javaVersion = val;
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

    public boolean getIsCluster() {
        return isCluster;
    }

    public void setIsCluster(boolean val) {
        isCluster = val;
    }

    public boolean getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(boolean val) {
        isPrimary = val;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public Date getModified() {
        return modified;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

}