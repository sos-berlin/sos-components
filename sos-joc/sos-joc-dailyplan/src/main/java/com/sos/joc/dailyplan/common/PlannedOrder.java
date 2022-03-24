package com.sos.joc.dailyplan.common;

import java.nio.file.Paths;

import com.sos.commons.util.SOSString;
import com.sos.controller.model.order.FreshOrder;
import com.sos.inventory.model.calendar.Period;

public class PlannedOrder {

    private final FreshOrder freshOrder;
    private final String controllerId;
    private final Long calendarId;

    private final String workflowName;
    private final String workflowPath;
    private final String scheduleName;
    private final String schedulePath;
    private final boolean submitOrderToControllerWhenPlanned;

    private Long submissionHistoryId;
    private Period period;
    private Long averageDuration = 0L;
    private boolean storedInDb = false;
    private String orderName;

    public PlannedOrder(String controllerId, FreshOrder freshOrder, DailyPlanSchedule dailyPlanSchedule, DailyPlanScheduleWorkflow scheduleWorkflow,
            Long calendarId) {
        this.controllerId = controllerId;
        this.freshOrder = freshOrder;
        this.calendarId = calendarId;

        this.workflowName = scheduleWorkflow.getName();
        this.workflowPath = scheduleWorkflow.getPath();
        this.scheduleName = Paths.get(dailyPlanSchedule.getSchedule().getPath()).getFileName().toString();
        this.schedulePath = dailyPlanSchedule.getSchedule().getPath();
        this.submitOrderToControllerWhenPlanned = dailyPlanSchedule.getSchedule().getSubmitOrderToControllerWhenPlanned() == null ? false
                : dailyPlanSchedule.getSchedule().getSubmitOrderToControllerWhenPlanned();
    }

    public PlannedOrderKey uniqueOrderKey() {
        return new PlannedOrderKey(controllerId, workflowName, scheduleName, freshOrder.getId());
    }

    public String getScheduleFolder() {
        return getParentFolder(schedulePath);
    }

    public String getWorkflowFolder() {
        return getParentFolder(workflowPath);
    }

    private String getParentFolder(String path) {
        if (SOSString.isEmpty(path) || path.equals("/")) {
            return "/";
        }
        return Paths.get(path).getParent().toString().replace('\\', '/');
    }

    public FreshOrder getFreshOrder() {
        return freshOrder;
    }

    public Period getPeriod() {
        return period;
    }

    public void setPeriod(Period val) {
        period = val;
    }

    public Long getCalendarId() {
        return calendarId;
    }

    public void setAverageDuration(Long val) {
        averageDuration = val;
    }

    public Long getAverageDuration() {
        return averageDuration;
    }

    public void setSubmissionHistoryId(Long val) {
        submissionHistoryId = val;
    }

    public Long getSubmissionHistoryId() {
        return submissionHistoryId;

    }

    public String getControllerId() {
        return controllerId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public String getSchedulePath() {
        return schedulePath;
    }

    public boolean getSubmitOrderToControllerWhenPlanned() {
        return submitOrderToControllerWhenPlanned;
    }

    public String getOrderName() {
        return orderName;
    }

    public void setOrderName(String val) {
        if (val.length() > 30) {
            orderName = val.substring(0, 30);
        } else {
            orderName = val;
        }
    }

    public boolean isStoredInDb() {
        return storedInDb;
    }

    public void setStoredInDb(boolean val) {
        storedInDb = val;
    }
}
