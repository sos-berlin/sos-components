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
import javax.persistence.Transient;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.audit.ObjectType;

@Entity
@Table(name = DBLayer.TABLE_JOC_AUDIT_LOG_DETAILS)
@SequenceGenerator(name = DBLayer.TABLE_JOC_AUDIT_LOG_DETAILS_SEQUENCE, sequenceName = DBLayer.TABLE_JOC_AUDIT_LOG_DETAILS_SEQUENCE, allocationSize = 1)
public class DBItemJocAuditLogDetails extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_JOC_AUDIT_LOG_DETAILS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[AUDITLOG_ID]", nullable = false)
    private Long auditLogId;

    @Column(name = "[TYPE]", nullable = false)
    private Integer type;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[ORDER_ID]", nullable = true)
    private String orderId;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public void setAuditLogId(Long val) {
        auditLogId = val;
    }

    public Long getAuditLogId() {
        return auditLogId;
    }
    
    public Integer getType() {
        return type;
    }
    
    @Transient
    public ObjectType getTypeAsEnum() {
        try {
            return ObjectType.fromValue(type);
        } catch (Exception e) {
            return null;
        }
    }

    public void setType(Integer val) {
        type = val;
    }

    @Transient
    public void setType(ObjectType val) {
        setType(val == null ? null : val.intValue());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        path = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        folder = val;
    }
    
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }
    
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }
}