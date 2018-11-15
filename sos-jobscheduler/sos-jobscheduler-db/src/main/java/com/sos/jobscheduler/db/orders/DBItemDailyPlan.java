package com.sos.jobscheduler.db.orders;

import java.util.Date;
import javax.persistence.*;
import com.sos.jobscheduler.db.DBLayer;

@Entity
@Table(name = DBLayer.DAILY_PLAN_TABLE)
@SequenceGenerator(name = DBLayer.DAILY_PLAN_TABLE_SEQUENCE, sequenceName = DBLayer.DAILY_PLAN_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlan {

    private Long id;
    private String masterId;
    private String workflow;
    private String orderKey;
    private String orderName;
    private Long calendarId;
    private Date plannedStart;
    private Date expectedEnd;
    private Date created;
    private Date modified;

    public DBItemDailyPlan() {

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

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    @Column(name = "[CALENDAR_ID]", nullable = true)
    public Long getCalendarId() {
        return calendarId;
    }


    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    @Column(name = "[ORDER_KEY]", nullable = false)
    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    @Column(name = "[ORDER_NAME]", nullable = false)
    public String getOrderName() {
        return orderName;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    @Column(name = "[WORKFLOW]", nullable = false)
    public String getWorkflow() {
        return workflow;
    }

    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }

    public void nullPlannedStart() {
        this.plannedStart = null;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[PLANNED_START]", nullable = false)
    public Date getPlannedStart() {
        return plannedStart;
    }

    public void setExpectedEnd(Date expectedEnd) {
        this.expectedEnd = expectedEnd;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[EXPECTED_END]", nullable = true)
    public Date getExpectedEnd() {
        return expectedEnd;
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