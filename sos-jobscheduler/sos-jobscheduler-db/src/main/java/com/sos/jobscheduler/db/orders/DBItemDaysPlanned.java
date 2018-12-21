package com.sos.jobscheduler.db.orders;

import java.util.Date;
import javax.persistence.*;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.DAYS_PLANNED_TABLE)
@SequenceGenerator(name = DBLayer.DAYS_PLANNED_TABLE_SEQUENCE, sequenceName = DBLayer.DAYS_PLANNED_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemDaysPlanned {

    private Long id;
    private String masterId;
    private Integer day;
    private Integer year;
    private Date created;
    private Date modified;

    public DBItemDaysPlanned() {

    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.DAILY_PLAN_TABLE_SEQUENCE)
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