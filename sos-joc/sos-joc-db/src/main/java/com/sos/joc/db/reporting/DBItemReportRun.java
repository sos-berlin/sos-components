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

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.TABLE_REPORT_RUNS)
@SequenceGenerator(name = DBLayer.TABLE_REPORT_RUNS_SEQUENCE, sequenceName = DBLayer.TABLE_REPORT_RUNS_SEQUENCE, allocationSize = 1)
public class DBItemReportRun extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_REPORT_RUNS_SEQUENCE)
    @Column(name = "[ID]", nullable = false)
    private Long id;

    @Column(name = "[NAME]", nullable = false)
    private String name;

    @Column(name = "[TITLE]", nullable = true)
    private String title;

    @Column(name = "[TEMPLATE_ID]", nullable = false)
    private Integer templateId;

    @Column(name = "[FREQUENCIES]", nullable = false)
    private String frequencies;

    @Column(name = "[SIZE]", nullable = false)
    private Integer size;

    @Column(name = "[DATE_FROM]", nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date dateFrom;

    @Column(name = "[CREATED]", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;
    
    public DBItemReportRun() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
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

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer val) {
        size = val;
    }
    
    public Date getDateFrom() {
        return dateFrom;
    }

    public void setDateFrom(Date val) {
        dateFrom = val;
    }

    public Date getCreated() {
        return created;
    }
    
    public void setCreated(Date val) {
        created = val;
    }

}
