package com.sos.joc.db.reporting;

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

import com.sos.inventory.model.report.Frequency;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_REPORT_HISTORY)
@SequenceGenerator(name = DBLayer.TABLE_REPORT_HISTORY_SEQUENCE, sequenceName = DBLayer.TABLE_REPORT_HISTORY_SEQUENCE, allocationSize = 1)
public class DBItemReportHistory extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_HISTORY_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[RUN_ID]", nullable = false)
    private Long runId;

    @Column(name = "[TEMPLATE_ID]", nullable = false)
    private Integer templateId;

    @Column(name = "[FREQUENCY]", nullable = false)
    private Integer frequency;

    @Column(name = "[SIZE]", nullable = false)
    private Integer size;

    @Column(name = "[DATE_FROM]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFrom;

    @Column(name = "[DATE_TO]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTo;

    @Column(name = "[CONTENT]", nullable = false)
    private byte[] content;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    public DBItemReportHistory() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }
    
    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long val) {
        runId = val;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer val) {
        templateId = val;
    }
    
    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer val) {
        frequency = val;
    }
    
    @Transient
    public Frequency getFrequencyAsEnum() {
        return Frequency.fromValue(frequency);
    }

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer val) {
        size = val;
    }
    
    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] val) {
        content = val;
    }
    
    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date val) {
        dateFrom = val;
    }
    
    public Date getDateTo() {
        return dateTo;
    }

    public void setDateTo(Date val) {
        dateTo = val;
    }

    public Date getCreated() {
        return created;
    }
    
    public void setCreated(Date val) {
        created = val;
    }

}
