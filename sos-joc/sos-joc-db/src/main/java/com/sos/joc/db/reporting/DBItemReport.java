package com.sos.joc.db.reporting;

import java.nio.file.Path;
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
import javax.persistence.UniqueConstraint;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.report.Frequency;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_REPORTS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
@SequenceGenerator(name = DBLayer.TABLE_REPORTS_SEQUENCE, sequenceName = DBLayer.TABLE_REPORTS_SEQUENCE, allocationSize = 1)
public class DBItemReport extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORTS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    // ID from REPORT_RUN_HISTORY
    @Column(name = "[RUN_ID]", nullable = false)
    private Long runId;

    @Column(name = "[TEMPLATE_ID]", nullable = false)
    private Integer templateId;

    @Column(name = "[FREQUENCY]", nullable = false)
    private Integer frequency;
    
    @Column(name = "[HITS]", nullable = false)
    private Integer hits;

    @Column(name = "[DATE_FROM]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFrom;

    @Column(name = "[DATE_TO]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTo;

    @Column(name = "[CONTENT]", nullable = false)
    private byte[] content;

    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    
    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    @Column(name = "[CONSTRAINT_HASH]", nullable = false)
    private String constraintHash; // hash from templateId, frequency, dateFrom, hits

    
    @Transient
    private Path reportFile;
    
    public DBItemReport() {
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

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer val) {
        hits = val;
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

    public Date getModified() {
        return modified;
    }
    
    public void setModified(Date val) {
        modified = val;
    }

    public Date getCreated() {
        return created;
    }
    
    public void setCreated(Date val) {
        created = val;
    }
    
    public String getConstraintHash() {
        return constraintHash;
    }

    public void setConstraintHash(String val) {
        constraintHash = val;
    }
    
    @Transient
    public Path getReportFile() {
        return reportFile;
    }
    
    @Transient
    public void setReportFile(Path val) {
        reportFile = val;
    }
    
    @Transient
    public String hashConstraint() {
        return SOSString.hash256(new StringBuilder().append(templateId).append(frequency).append(dateFrom).append(hits).toString());
    }

}
