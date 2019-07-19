package com.sos.jobscheduler.db.orders;

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

import com.sos.jobscheduler.db.DBItem;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.DAYS_PLANNED_TABLE,
uniqueConstraints = { @UniqueConstraint(columnNames = { "[MASTER_ID]","[DAY]","[YEAR]" }) })
@SequenceGenerator(name = DBLayer.DAYS_PLANNED_TABLE_SEQUENCE, sequenceName = DBLayer.DAYS_PLANNED_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemDaysPlanned extends DBItem {

    private static final long serialVersionUID = 1L;
	
    private Long id;
    private String masterId;
    private Integer day;
    private Integer year;
    private Date created;
    private Date modified;

    public DBItemDaysPlanned() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.DAYS_PLANNED_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    public Long getId() {
        return id;
    }

    @Id
    @Column(name = "[ID]")
    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "[MASTER_ID]", nullable = false)
    public String getMasterId() {
        return masterId;
    }

    public void setMasterId(String masterId) {
        this.masterId = masterId;
    }

    public void setDay(Integer day) {
        this.day = day;
    }

    @Column(name = "[DAY]", nullable = true)
    public Integer getDay() {
        return day;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    @Column(name = "[YEAR]", nullable = false)
    public Integer getYear() {
        return year;
    }

   
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = true)
    public Date getModified() {
        return modified;
    }

    public void setModified(Date modified) {
        this.modified = modified;
    }

}