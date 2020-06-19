package com.sos.joc.db.orders;

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
import javax.persistence.UniqueConstraint;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;

@Entity
@Table(name = DBLayer.DAILY_PLAN_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { "[JOBSCHEDULER_ID]", "[DAY]", "[YEAR]" }) })
@SequenceGenerator(name = DBLayer.DAILY_PLAN_TABLE_SEQUENCE, sequenceName = DBLayer.DAILY_PLAN_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlan extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.DAILY_PLAN_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[JOBSCHEDULER_ID]", nullable = false)
    private String jobschedulerId;

    @Column(name = "[DAY]", nullable = true)
    private Integer day;

    @Column(name = "[YEAR]", nullable = false)
    private Integer year;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = true)
    private Date modified;

   

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getJobschedulerId() {
        return jobschedulerId;
    }

    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    public Integer getDay() {
        return day;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getYear() {
        return year;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

}