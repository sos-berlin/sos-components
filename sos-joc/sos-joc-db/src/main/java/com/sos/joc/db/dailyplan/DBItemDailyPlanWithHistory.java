package com.sos.joc.db.dailyplan;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;

public class DBItemDailyPlanWithHistory {

    private final int toleranceUnit = Calendar.SECOND;
    private static final int DAILY_PLAN_LATE_TOLERANCE = 60;

    private Long plannedOrderId;
    private Long submissionHistoryId;
    private String controllerId;
    private String workflowPath;
    private String workflowName;
    private String orderId;
    private String schedulePath;
    private String scheduleName;
    private String orderName;
    private Long calendarId;
    private boolean submitted;
    private Date submitTime;
    private Date periodBegin;
    private Date periodEnd;
    private Long repeatInterval;
    private Date plannedStart;
    private Date expectedEnd;
    private Date plannedOrderCreated;

    private Long orderHistoryId;
    private Date startTime;
    private Date endTime;
    private Integer state;

    public Boolean isLate() {
        Date planned = getPlannedStart();
        Date start = getStartTime();

        if (start == null || start.getTime() == new Date(0).getTime()) {
            return planned.before(new Date());
        } else {
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(planned);
            calendar.add(toleranceUnit, DAILY_PLAN_LATE_TOLERANCE);
            Date scheduleToleranz = calendar.getTime();
            return start.after(scheduleToleranz);
        }
    }

    public DailyPlanOrderStateText getStateText() {
        DailyPlanOrderStateText state;
        try {
            state = DailyPlanOrderStateText.fromValue(getState());
        } catch (IllegalArgumentException e) {
            state = null;
        }
        if (submitted) {
            if (state != null && state.equals(DailyPlanOrderStateText.FINISHED)) {
                return DailyPlanOrderStateText.FINISHED;
            } else {
                return DailyPlanOrderStateText.SUBMITTED;
            }
        } else {
            return DailyPlanOrderStateText.PLANNED;
        }
    }

    public Integer getStartMode() {
        if (getPeriodBegin() == null) {
            return 0;
        } else {
            return 1;
        }
    }

    public Long getPlannedOrderId() {
        return plannedOrderId;
    }

    public void setPlannedOrderId(Long val) {
        plannedOrderId = val;
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

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String val) {
        workflowPath = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public void setSchedulePath(String val) {
        schedulePath = val;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setCalendarId(Long val) {
        calendarId = val;
    }

    public boolean isSubmitted() {
        return submitted;
    }

    public void setSubmitted(boolean val) {
        submitted = val;
    }

    public Date getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(Date val) {
        submitTime = val;
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

    public Long getRepeatInterval() {
        return repeatInterval;
    }

    public void setRepeatInterval(Long val) {
        repeatInterval = val;
    }

    public Date getPlannedStart() {
        return plannedStart;
    }

    public void setPlannedStart(Date val) {
        plannedStart = val;
    }

    public Date getExpectedEnd() {
        return expectedEnd;
    }

    public void setExpectedEnd(Date val) {
        this.expectedEnd = val;
    }

    public Date getPlannedOrderCreated() {
        return plannedOrderCreated;
    }

    public void setPlannedOrderCreated(Date val) {
        plannedOrderCreated = val;
    }

    public Long getOrderHistoryId() {
        return orderHistoryId;
    }

    public void setOrderHistoryId(Long val) {
        orderHistoryId = val;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date val) {
        startTime = val;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer val) {
        state = val;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date val) {
        endTime = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String val) {
        scheduleName = val;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String val) {
        orderName = val;
    }

}