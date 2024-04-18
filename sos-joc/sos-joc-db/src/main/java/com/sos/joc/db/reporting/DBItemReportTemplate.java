package com.sos.joc.db.reporting;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_REPORT_TEMPLATES, uniqueConstraints = { @UniqueConstraint(columnNames = { "[TEMPLATE_ID]" }) })
public class DBItemReportTemplate extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[TEMPLATE_ID]", nullable = false)
    private Integer templateId;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    public DBItemReportTemplate() {
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer val) {
        templateId = val;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String val) {
        content = val;
    }
    
    @Transient
    public byte[] getContentBytes() {
        if (content != null) {
            return content.getBytes(StandardCharsets.UTF_8);
        }
        return new byte[] {};
    }

    @Transient
    public void setContent(byte[] val) {
        if (val != null) {
            content = new String(val, StandardCharsets.UTF_8);
        } else {
            content = null;
        }
    }

    public Date getCreated() {
        return created;
    }
    
    public void setCreated(Date val) {
        created = val;
    }

}
