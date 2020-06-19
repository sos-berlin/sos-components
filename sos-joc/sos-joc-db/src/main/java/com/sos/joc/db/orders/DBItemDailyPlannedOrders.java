package com.sos.joc.db.orders;

import java.text.ParseException;
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

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.orders.classes.DailyPlanDate;

@Entity
@Table(name = DBLayer.DAILY_PLANNED_ORDERS_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { "[JOBSCHEDULER_ID]", "[WORKFLOW]", "[ORDER_KEY]" }) })
@SequenceGenerator(name = DBLayer.DAILY_PLANNED_ORDERS_TABLE_SEQUENCE, sequenceName = DBLayer.DAILY_PLANNED_ORDERS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlannedOrders extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.DAILY_PLANNED_ORDERS_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[PLAN_ID]", nullable = false)
    private Long planId;

    @Column(name = "[JOBSCHEDULER_ID]", nullable = false)
    private String jobschedulerId;

    @Column(name = "[WORKFLOW]", nullable = false)
    private String workflow;

    @Column(name = "[ORDER_KEY]", nullable = false)
    private String orderKey;

    @Column(name = "[ORDER_TEMPLATE_NAME]", nullable = false)
    private String orderTemplateName;

    @Column(name = "[CALENDAR_ID]", nullable = false)
    private Long calendarId;

    @Column(name = "[SUBMITTED]", nullable = false)
    @Type(type = "numeric_boolean")
    private boolean submitted;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[SUBMIT_TIME]", nullable = true)
    private Date submitTime;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[PERIOD_BEGIN]", nullable = true)
    private Date periodBegin;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[PERIOD_END]", nullable = true)
    private Date periodEnd;

    @Column(name = "[REPEAT_INTERVAL]", nullable = true)
    private Long repeatInterval;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[PLANNED_START]", nullable = false)
    private Date plannedStart;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[EXPECTED_END]", nullable = true)
    private Date expectedEnd;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = true)
    private Date modified;

    public DBItemDailyPlannedOrders() {

    }

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

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public Long getPlanId() {
        return planId;
    }

    public void setPlanId(Long planId) {
        this.planId = planId;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderTemplateName(String orderTemplateName) {
        this.orderTemplateName = orderTemplateName;
    }

    public String getOrderTemplateName() {
        return orderTemplateName;
    }

    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setSubmitted(boolean submitted) {
        this.submitted = submitted;
    }

    public boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitTime(Date submitTime) {
        this.submitTime = submitTime;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setPlannedStart(Date plannedStart) {
        this.plannedStart = plannedStart;
    }

    public Date getPlannedStart() {
        return plannedStart;
    }

    public void setExpectedEnd(Date expectedEnd) {
        this.expectedEnd = expectedEnd;
    }

    public Date getExpectedEnd() {
        return expectedEnd;
    }

    public void setRepeatInterval(Long repeatInterval) {
        this.repeatInterval = repeatInterval;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public Date getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(Date periodBegin) {
        this.periodBegin = periodBegin;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date periodEnd) {
        this.periodEnd = periodEnd;
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

    @Transient
    public void setPeriodBegin(Date start, String periodBegin) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate();
        daysScheduleDate.setSchedule(start, periodBegin);
        this.setPeriodBegin(daysScheduleDate.getSchedule());
    }

    @Transient
    public void setPeriodEnd(Date start, String periodEnd) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate();
        daysScheduleDate.setSchedule(start, periodEnd);
        this.setPeriodEnd(daysScheduleDate.getSchedule());
    }

    @Transient
    public void setRepeatInterval(String repeat) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate();
        daysScheduleDate.setSchedule("HH:mm:ss", repeat);
        Date to = daysScheduleDate.getSchedule();

        if (repeat != null) {
            this.repeatInterval = to.getTime() / 1000;
        }
    }

}