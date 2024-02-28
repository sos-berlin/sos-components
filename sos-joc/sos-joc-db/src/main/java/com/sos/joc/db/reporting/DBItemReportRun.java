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
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.reporting.ReportRunStateText;

@Entity
@Table(name = DBLayer.TABLE_REPORT_RUNS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_REPORT_RUNS_SEQUENCE, sequenceName = DBLayer.TABLE_REPORT_RUNS_SEQUENCE, allocationSize = 1)
public class DBItemReportRun extends DBItem {

    private static final long serialVersionUID = 1L;
    private static final int MAX_LEN_ERROR_TEXT = 500;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_RUNS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[TEMPLATE_ID]", nullable = false)
    private Integer templateId;

    @Column(name = "[FREQUENCIES]", nullable = false)
    private String frequencies;

    @Column(name = "[HITS]", nullable = false)
    private Integer hits;

    @Column(name = "[MONTH_FROM]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFrom;

    @Column(name = "[MONTH_TO]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateTo;

    @Column(name = "[CONTROLLER_ID]", nullable = true)
    private String controllerId;

    @Column(name = "[STATE]", nullable = false)
    private Integer state;
    
    @Column(name = "[ERROR_TEXT]", nullable = true)
    private String errorText;
    
    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    @Column(name = "[MODIFIED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date modified;
    
    public DBItemReportRun() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        path = val;
    }
    
    public String getFolder() {
        return folder;
    }

    public void setFolder(String val) {
        folder = val;
    }
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String val) {
        title = val;
    }
    
    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer val) {
        templateId = val;
    }
    
    public String getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(String val) {
        frequencies = val;
    }

    public Integer getHits() {
        return hits;
    }

    public void setHits(Integer val) {
        hits = val;
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
    
    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }
    
    public Integer getState() {
        return state;
    }
    
    @Transient
    public ReportRunStateText getStateAsEnum() {
        try {
            return ReportRunStateText.fromValue(state);
        } catch (Exception e) {
            return ReportRunStateText.UNKNOWN;
        }
    }
    
    public void setState(Integer val) {
        state = val;
    }
    
    public String getErrorText() {
        return errorText;
    }
    
    public void setErrorText(String val) {
        errorText = normalizeErrorText(val);
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
    
    @Transient
    private static String normalizeErrorText(String val) {
        return normalizeValue(val, MAX_LEN_ERROR_TEXT);
    }

}
