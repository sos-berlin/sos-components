package com.sos.joc.db.dailyplan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
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

import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.common.DailyPlanDate;

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

    @Column(name = "[WORKFLOW_NAME]", nullable = false)
    private String workflowName;

    @Column(name = "[WORKFLOW_PATH]", nullable = false)
    private String workflowPath;

    @Column(name = "[WORKFLOW_FOLDER]", nullable = false)
    private String workflowFolder;

    @Column(name = "[ORDER_ID]", nullable = false)
    private String orderId;

    @Column(name = "[ORDER_NAME]", nullable = false)
    private String orderName;

    @Column(name = "[SCHEDULE_NAME]", nullable = false)
    private String scheduleName;

    @Column(name = "[SCHEDULE_PATH]", nullable = false)
    private String schedulePath;

    @Column(name = "[SCHEDULE_FOLDER]", nullable = false)
    private String scheduleFolder;

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

    @Column(name = "[ORDER_PARAMETERISATION]", nullable = true)
    private String orderParameterisation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[CREATED]", nullable = false)
    private Date created;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "[MODIFIED]", nullable = true)
    private Date modified;
    
    @Transient
    private String  dailyPlanDate;
    
    @Transient
    private boolean isLastOfCycle = false;

    public DBItemDailyPlanOrder() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Long getSubmissionHistoryId() {
        return submissionHistoryId;
    }

    public void setSubmissionHistoryId(Long val) {
        submissionHistoryId = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public String getWorkflowFolder() {
        return workflowFolder;
    }

    public void setWorkflowFolder(String val) {
        workflowFolder = val;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String val) {
        orderName = val;
    }

    public void setScheduleName(String val) {
        scheduleName = val;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setSchedulePath(String val) {
        schedulePath = val;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public String getScheduleFolder() {
        return scheduleFolder;
    }

    public void setScheduleFolder(String val) {
        scheduleFolder = val;
    }

    public void setCalendarId(Long val) {
        calendarId = val;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setStartMode(Integer val) {
        startMode = val;
    }

    public Integer getStartMode() {
        return startMode;
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

    public void setRepeatInterval(Long val) {
        repeatInterval = val;
    }

    public Long getRepeatInterval() {
        return repeatInterval;
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

    public void setOrderParameterisation(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        orderParameterisation = val;
    }

    public String getOrderParameterisation() {
        return orderParameterisation;
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

    @Transient
    public boolean isCyclic() {
        return startMode != null && startMode.equals(Integer.valueOf(1));
    }
    
    @Transient
    public boolean isLastOfCyclic() {
        return isLastOfCycle;
    }
    
    @Transient
    public void setIsLastOfCyclic(boolean val) {
        isLastOfCycle = val;;
    }

    @Transient
    public void setPeriodBegin(Date dailyPlanDate, String periodBegin) throws ParseException {
        this.periodBegin = DailyPlanDate.getPeriodAsDate(dailyPlanDate, periodBegin);
    }

    @Transient
    public void setPeriodEnd(Date dailyPlanDate, String periodEnd) throws ParseException {
        this.periodEnd = DailyPlanDate.getPeriodAsDate(dailyPlanDate, periodEnd);
    }

    @Transient
    public void setRepeatInterval(String repeat) throws ParseException {
        setRepeatInterval(DailyPlanDate.getRepeatInterval(repeat));
    }
    
    @Transient
    public void setDailyPlanDate(String yyyyMMdd) {
        dailyPlanDate = yyyyMMdd;
    }
    
    @Transient
    public String getDailyPlanDate() {
        return dailyPlanDate;
    }

    @Transient
    public String getDailyPlanDate(String timeZone, String periodBegin) {
        if (dailyPlanDate != null) {
            return dailyPlanDate;
        }
        return getDailyPlanDate(timeZone, Instant.parse("1970-01-01T" + periodBegin + "Z").getEpochSecond());
    }
    
    @Transient
    public String getDailyPlanDate(String timeZone, long periodBeginSeconds) {
        if (dailyPlanDate != null) {
            return dailyPlanDate;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        format.setTimeZone(TimeZone.getTimeZone(timeZone));
        if (periodBeginSeconds <= 0) {
            return format.format(plannedStart);
        }
        dailyPlanDate = format.format(Date.from(plannedStart.toInstant().minusSeconds(periodBeginSeconds)));
        return dailyPlanDate;
    }

}