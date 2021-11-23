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

import org.hibernate.annotations.Type;

import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.classes.DailyPlanDate;

// @Table(name = DBLayer.TABLE_DPL_ORDERS, uniqueConstraints = { @UniqueConstraint(columnNames = { "[CONTROLLER_ID]", "[WORKFLOW_NAME]",
// "[ORDER_ID]" }) })

@Entity
@Table(name = DBLayer.TABLE_DPL_ORDERS)
@SequenceGenerator(name = DBLayer.TABLE_DPL_ORDERS_SEQUENCE, sequenceName = DBLayer.TABLE_DPL_ORDERS_SEQUENCE, allocationSize = 1)

public class DBItemDailyPlanOrder extends DBItem {

    private static final long serialVersionUID = 1L;

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

    public void setId(Long val) {
        id = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public void setCalendarId(Long val) {
        calendarId = val;
    }

    public Long getSubmissionHistoryId() {
        return submissionHistoryId;
    }

    public void setSubmissionHistoryId(Long val) {
        submissionHistoryId = val;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setScheduleName(String val) {
        scheduleName = val;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setSchedulePath(String val) {
        schedulePath = val;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setSubmitted(boolean val) {
        submitted = val;
    }

    public boolean getSubmitted() {
        return submitted;
    }

    public void setSubmitTime(Date val) {
        submitTime = val;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setPlannedStart(Date val) {
        plannedStart = val;
    }

    public Date getPlannedStart() {
        return plannedStart;
    }

    public void setExpectedEnd(Date val) {
        expectedEnd = val;
    }

    public Date getExpectedEnd() {
        return expectedEnd;
    }

    public void setRepeatInterval(Long val) {
        repeatInterval = val;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public Date getPeriodBegin() {
        return periodBegin;
    }

    public void setPeriodBegin(Date val) {
        periodBegin = val;
    }

    public Date getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(Date val) {
        periodEnd = val;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date val) {
        created = val;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date val) {
        modified = val;
    }

    public String getScheduleFolder() {
        return scheduleFolder;
    }

    public void setScheduleFolder(String val) {
        scheduleFolder = val;
    }

    public String getWorkflowFolder() {
        return workflowFolder;
    }

    public void setWorkflowFolder(String val) {
        workflowFolder = val;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String val) {
        orderName = val;
    }

    public void setStartMode(Integer val) {
        startMode = val;
    }

    public Integer getStartMode() {
        return startMode;
    }

    @Transient
    public void setPeriodBegin(Date start, String periodBegin) throws ParseException {
        DailyPlanDate date = new DailyPlanDate();
        date.setPeriod(start, periodBegin);
        this.setPeriodBegin(date.getSchedule());
    }

    @Transient
    public void setPeriodEnd(Date start, String periodEnd) throws ParseException {
        DailyPlanDate date = new DailyPlanDate();
        date.setPeriod(start, periodEnd);
        this.setPeriodEnd(date.getSchedule());
    }

    @Transient
    public void setRepeatInterval(String repeat) throws ParseException {
        DailyPlanDate date = new DailyPlanDate();
        date.setSchedule("HH:mm:ss", repeat);
        Calendar c = Calendar.getInstance();
        c.setTime(date.getSchedule());

        if (repeat != null) {
            Integer i = c.get(Calendar.HOUR) * 60 * 60 + c.get(Calendar.MINUTE) * 60 + c.get(Calendar.SECOND);
            repeatInterval = Long.valueOf(i);
        }
    }

    @Transient
    public String getDailyPlanDate(String timeZone) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
        return format.format(plannedStart);
    }

}