package com.sos.joc.db.dailyplan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.classes.DailyPlanDate;

@Entity
@Table(name = DBLayer.TABLE_DPL_ORDERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[WORKFLOW_NAME]",
        "[ORDER_ID]" }) })
@SequenceGenerator(name = DBLayer.TABLE_DPL_ORDERS_SEQUENCE, sequenceName = DBLayer.TABLE_DPL_ORDERS_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanOrder extends DBItem {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(DBItemDailyPlanOrder.class);

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = DBLayer.TABLE_DPL_ORDERS_SEQUENCE)
    @Column(name = "[ID]")
    private Long id;

    @Column(name = "[SUBMISSION_HISTORY_ID]", nullable = false)
    private Long submissionHistoryId;

    @Column(name = "[CONTROLLER_ID]", nullable = false)
    private String controllerId;

    @Column(name = "[SCHEDULE_FOLDER]", nullable = false)
    private String scheduleFolder;

    @Column(name = "[WORKFLOW_FOLDER]", nullable = false)
    private String workflowFolder;

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

    @Column(name = "[ORDER_NAME]", nullable = false)
    private String orderName;

    @Column(name = "[CALENDAR_ID]", nullable = false)
    private Long calendarId;

    @Column(name = "[START_MODE]", nullable = false)
    private Integer startMode;

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

    public DBItemDailyPlanOrder() {

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

    public String getScheduleFolder() {
        return scheduleFolder;
    }

    public void setScheduleFolder(String scheduleFolder) {
        this.scheduleFolder = scheduleFolder;
    }

    public String getWorkflowFolder() {
        return workflowFolder;
    }

    public void setWorkflowFolder(String workflowFolder) {
        this.workflowFolder = workflowFolder;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    public void setStartMode(Integer startMode) {
        this.startMode = startMode;
    }

    public Integer getStartMode() {
        return startMode;
    }

    @Transient
    public void setPeriodBegin(Date start, String periodBegin) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate();
        daysScheduleDate.setPeriod(start, periodBegin);
        this.setPeriodBegin(daysScheduleDate.getSchedule());
    }

    @Transient
    public void setPeriodEnd(Date start, String periodEnd) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate();
        daysScheduleDate.setPeriod(start, periodEnd);
        this.setPeriodEnd(daysScheduleDate.getSchedule());
    }

    @Transient
    public void setRepeatInterval(String repeat) throws ParseException {
        DailyPlanDate daysScheduleDate = new DailyPlanDate();
        daysScheduleDate.setSchedule("HH:mm:ss", repeat);
        Calendar c = Calendar.getInstance();
        c.setTime(daysScheduleDate.getSchedule());

        if (repeat != null) {
            Integer i = c.get(Calendar.HOUR) * 60 * 60 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
            repeatInterval = Long.valueOf(i);
        }
    }

    @Transient
    public String getDailyPlanDate(String timeZone) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
        String dailyPlanDate = format.format(plannedStart);
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("plannedStart=%s, timeZone=%s, dailyPlanDate=%s", plannedStart, timeZone, dailyPlanDate));
        }
        return dailyPlanDate;
    }

}