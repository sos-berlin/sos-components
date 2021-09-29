package com.sos.joc.db.orders;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import com.sos.joc.model.dailyplan.DailyPlanOrderState;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.order.OrderStateText;

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
		Date start = null;

		start = getStartTime();

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
			state = DailyPlanOrderStateText.fromValue(this.getState());
		} catch (IllegalArgumentException e) {
			state = null;
		}
		if (submitted) {
			if (this.getState() != null && state != null
					&& DailyPlanOrderStateText.fromValue(this.getState()).equals(DailyPlanOrderStateText.FINISHED)) {
				return DailyPlanOrderStateText.FINISHED;
			} else {
				return DailyPlanOrderStateText.SUBMITTED;
			}
		} else {
			return DailyPlanOrderStateText.PLANNED;
		}
	}

	public Integer getStartMode() {
		if (this.getPeriodBegin() == null) {
			return 0;
		} else {
			return 1;
		}
	}

	public Long getPlannedOrderId() {
		return plannedOrderId;
	}

	public void setPlannedOrderId(Long plannedOrderId) {
		this.plannedOrderId = plannedOrderId;
	}

	public Long getSubmissionHistoryId() {
		return submissionHistoryId;
	}

	public void setSubmissionHistoryId(Long submissionHistoryId) {
		this.submissionHistoryId = submissionHistoryId;
	}

	public String getControllerId() {
		return controllerId;
	}

	public void setControllerId(String controllerId) {
		this.controllerId = controllerId;
	}

	public String getWorkflowPath() {
		return workflowPath;
	}

	public void setWorkflowPath(String workflowPath) {
		this.workflowPath = workflowPath;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getSchedulePath() {
		return schedulePath;
	}

	public void setSchedulePath(String schedulePath) {
		this.schedulePath = schedulePath;
	}

	public Long getCalendarId() {
		return calendarId;
	}

	public void setCalendarId(Long calendarId) {
		this.calendarId = calendarId;
	}

	public boolean isSubmitted() {
		return submitted;
	}

	public void setSubmitted(boolean submitted) {
		this.submitted = submitted;
	}

	public Date getSubmitTime() {
		return submitTime;
	}

	public void setSubmitTime(Date submitTime) {
		this.submitTime = submitTime;
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

	public Long getRepeatInterval() {
		return repeatInterval;
	}

	public void setRepeatInterval(Long repeatInterval) {
		this.repeatInterval = repeatInterval;
	}

	public Date getPlannedStart() {
		return plannedStart;
	}

	public void setPlannedStart(Date plannedStart) {
		this.plannedStart = plannedStart;
	}

	public Date getExpectedEnd() {
		return expectedEnd;
	}

	public void setExpectedEnd(Date expectedEnd) {
		this.expectedEnd = expectedEnd;
	}

	public Date getPlannedOrderCreated() {
		return plannedOrderCreated;
	}

	public void setPlannedOrderCreated(Date plannedOrderCreated) {
		this.plannedOrderCreated = plannedOrderCreated;
	}

	public Long getOrderHistoryId() {
		return orderHistoryId;
	}

	public void setOrderHistoryId(Long orderHistoryId) {
		this.orderHistoryId = orderHistoryId;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Integer getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	public String getWorkflowName() {
		return workflowName;
	}

	public void setWorkflowName(String workflowName) {
		this.workflowName = workflowName;
	}

	public String getScheduleName() {
		return scheduleName;
	}

	public void setScheduleName(String scheduleName) {
		this.scheduleName = scheduleName;
	}

	public String getOrderName() {
		return orderName;
	}

	public void setOrderName(String orderName) {
		this.orderName = orderName;
	}

}