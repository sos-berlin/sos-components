package com.sos.jitl.jobs.checkhistory.classes;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistory;
import com.sos.joc.model.order.OrderHistoryItem;

public class HistoryItem {

    private String controllerId;
    private String job;
    private String workflow;
    private String orderId;
    private String startTime;
    private String endTime;
    private String position;
    private HistoryState state;
    private Long taskId;
    private Integer exitCode;
    private boolean historyItemFound = false;
    private boolean result = false;
    private int count = 0;
    private OrderHistory orderHistory;
    private TaskHistory taskHistory;

    private String getIso8601String(Date d) {
        if (d != null) {
            SimpleDateFormat sdf;
            sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.format(d);
        } else {
            return "1900-01-01T00:00:00.000";
        }
    }

    public HistoryItem(OrderHistory orderHistory) {
        this.orderHistory = orderHistory;
        result = orderHistory != null;
        historyItemFound = orderHistory != null;

        if (result) {
            count = orderHistory.getHistory().size();
            OrderHistoryItem orderHistoryItem = orderHistory.getHistory().get(0);
            controllerId = orderHistoryItem.getControllerId();
            job = "";
            workflow = orderHistoryItem.getWorkflow();
            orderId = orderHistoryItem.getOrderId();
            startTime = getIso8601String(orderHistoryItem.getStartTime());
            endTime = getIso8601String(orderHistoryItem.getEndTime());
            position = orderHistoryItem.getPosition();
            state = orderHistoryItem.getState();
            taskId = 0L;
        }
    }

    public HistoryItem(TaskHistory taskHistory) {
        this.taskHistory = taskHistory;
        result = taskHistory != null;
        historyItemFound = taskHistory != null;

        if (result) {
            count = taskHistory.getHistory().size();
            TaskHistoryItem taskHistoryItem = taskHistory.getHistory().get(0);
            controllerId = taskHistoryItem.getControllerId();
            job = taskHistoryItem.getJob();
            workflow = taskHistoryItem.getWorkflow();
            orderId = taskHistoryItem.getOrderId();
            startTime = getIso8601String(taskHistoryItem.getStartTime());
            endTime = getIso8601String(taskHistoryItem.getEndTime());
            position = taskHistoryItem.getPosition();
            state = taskHistoryItem.getState();
            taskId = taskHistoryItem.getTaskId();
        }
    }

    public boolean getHistoryItemFound() {
        return historyItemFound;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getControllerId() {
        return controllerId;
    }

    public String getJob() {
        return job;
    }

    public String getWorkflow() {
        return workflow;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public String getPosition() {
        return position;
    }

    public HistoryState getState() {
        return state;
    }

    public Long getTaskId() {
        return taskId;
    }

    public boolean getResult() {
        return result;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }

    public int getCount() {
        return count;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

}
