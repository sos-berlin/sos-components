package com.sos.joc.db.reporting;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Date;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.report.Frequency;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = DBLayer.TABLE_REPORTS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONSTRAINT_HASH]" }) })
public class DBItemReport extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_REPORTS_SEQUENCE)
    private Long id;

    // ID from REPORT_RUN_HISTORY
    @Column(name = "[RUN_ID]", nullable = false)
    private Long runId;

    @Column(name = "[FREQUENCY]", nullable = false)
    private Integer frequency;

    @Column(name = "[DATE_FROM]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFrom;

    @Column(name = "[DATE_TO]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTo;

    @Column(name = "[CONTENT]", nullable = false)
    private String content;

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
    public String hashConstraint(Integer templateId, Integer hits, String controllerId) {
        return SOSString.hash256(new StringBuilder().append(templateId).append(frequency).append(dateFrom).append(hits).append(controllerId == null
                ? "" : controllerId).toString());
    }

}
