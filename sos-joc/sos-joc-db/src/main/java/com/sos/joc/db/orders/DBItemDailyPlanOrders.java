package com.sos.joc.db.orders;

import java.text.ParseException;
import java.util.Calendar;
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
@Table(name = DBLayer.DAILY_PLAN_ORDERS_TABLE, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[WORKFLOW_PATH]",
        "[ORDER_ID]" }) })
@SequenceGenerator(name = DBLayer.DAILY_PLAN_ORDERS_TABLE_SEQUENCE, sequenceName = DBLayer.DAILY_PLAN_ORDERS_TABLE_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanOrders extends DBItem {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.DAILY_PLAN_ORDERS_TABLE_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[SUBMISSION_HISTORY_ID]", nullable = false)
    private Long submissionHistoryId;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[WORKFLOW_PATH]", nullable = false)
    private String workflowPath;

    @Column(name = "[WORKFLOW_NAME]", nullable = false)
    private String workflowName;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;

    @Column(name = "[SCHEDULE_PATH]", nullable = false)
    private String schedulePath;

    @Column(name = "[SCHEDULE_NAME]", nullable = false)
    private String scheduleName;

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

    public DBItemDailyPlanOrders() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public void setCalendarId(Long calendarId) {
        this.calendarId = calendarId;
    }

    public Long getSubmissionHistoryId() {
        return submissionHistoryId;
    }

    public void setSubmissionHistoryId(Long submissionHistoryId) {
        this.submissionHistoryId = submissionHistoryId;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setSchedulePath(String schedulePath) {
        this.schedulePath = schedulePath;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    public String getWorkflowPath() {
        return workflowPath;
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
        Calendar c = Calendar.getInstance();
        c.setTime(daysScheduleDate.getSchedule());

        if (repeat != null) {
           Integer i=     c.get(Calendar.HOUR)*60*60 +  c.get(Calendar.MINUTE)*60 +  c.get(Calendar.SECOND);
           repeatInterval = new Long(i);
        }
    }

}