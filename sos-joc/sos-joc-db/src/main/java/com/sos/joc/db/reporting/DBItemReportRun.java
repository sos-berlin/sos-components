package com.sos.joc.db.reporting;

import java.util.Date;

import org.hibernate.annotations.Proxy;

import com.sos.commons.hibernate.id.SOSHibernateIdGenerator;
import com.sos.inventory.model.report.ReportOrder;
import com.sos.inventory.model.report.TemplateId;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.reporting.ReportRunStateText;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.Transient;

@SuppressWarnings("deprecation")
@Entity
@Table(name = DBLayer.TABLE_REPORT_RUNS)
@Proxy(lazy = false)
public class DBItemReportRun extends DBItem {

    private static final long serialVersionUID = 1L;
    private static final int MAX_LEN_ERROR_TEXT = 500;

    @Id
    @Column(name = "[ID]", nullable = false)
    @SOSHibernateIdGenerator(sequenceName = DBLayer.TABLE_REPORT_RUNS_SEQUENCE)
    private Long id;

    @Column(name = "[PATH]", nullable = false)
    private String path;

    @Column(name = "[FOLDER]", nullable = false)
    private String folder;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[TEMPLATE_ID]", nullable = false)
    private Integer templateId;

    @Column(name = "[FREQUENCIES]", nullable = false)
    private String frequencies;

    @Column(name = "[SORT]", nullable = false)
    private Integer sort;

    @Column(name = "[PERIOD_LENGTH]", nullable = false)
    private Integer periodLength;

    @Column(name = "[PERIOD_STEP]", nullable = false)
    private Integer periodStep;

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

    public void setName(String val) {
        name = val;
    }

    public String getName() {
        return name;
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
    
    @Transient
    public TemplateId getTemplateIdAsEnum() {
        try {
            return TemplateId.fromValue(templateId);
        } catch (Exception e) {
            return null;
        }
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

    public Integer getSort() {
        return sort;
    }
    
    @Transient
    public ReportOrder getSortAsEnum() {
        try {
            return ReportOrder.fromValue(state);
        } catch (Exception e) {
            return null;
        }
    }

    public void setSort(Integer sort) {
        this.sort = sort;
    }

    public Integer getPeriodLength() {
        return periodLength;
    }

    public void setPeriodLength(Integer periodLength) {
        this.periodLength = periodLength;
    }

    public Integer getPeriodStep() {
        return periodStep;
    }

    public void setPeriodStep(Integer periodStep) {
        this.periodStep = periodStep;
    }

    @Transient
    private static String normalizeErrorText(String val) {
        return normalizeValue(val, MAX_LEN_ERROR_TEXT);
    }

}
