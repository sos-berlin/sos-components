package com.sos.jitl.jobs.checkhistory.classes;

import java.util.Date;

import com.sos.joc.model.common.HistoryState;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.model.job.TaskHistoryItem;
import com.sos.joc.model.order.OrderHistoryItem;

public class HistoryItem {

    
    public String getName() {
        return name;
    }

    private String controllerId;
    private String job;
    private String workflow;
    private String name;
    private String orderId;
    private Date startTime;
    private Date endTime;
    private String position;
    private HistoryState state;
    private Long taskId;
    private Integer exitCode;
    private Boolean historyItemFound;
    private Boolean result;

    public HistoryItem(OrderHistoryItem orderHistoryItem) {
        result = orderHistoryItem != null;
        historyItemFound = orderHistoryItem != null;

        if (result) {
            controllerId = orderHistoryItem.getControllerId();
            job = "";
            workflow = orderHistoryItem.getWorkflow();
            orderId = orderHistoryItem.getOrderId();
            startTime = orderHistoryItem.getStartTime();
            endTime = orderHistoryItem.getEndTime();
            position = orderHistoryItem.getPosition();
            state = orderHistoryItem.getState();
            taskId = 0L;
        }
    }

    public HistoryItem(TaskHistoryItem taskHistoryItem) {
        result = taskHistoryItem != null;
        historyItemFound = taskHistoryItem != null;

        if (result) {
            controllerId = taskHistoryItem.getControllerId();
            job = taskHistoryItem.getJob();
            workflow = taskHistoryItem.getWorkflow();
            orderId = taskHistoryItem.getOrderId();
            startTime = taskHistoryItem.getStartTime();
            endTime = taskHistoryItem.getEndTime();
            position = taskHistoryItem.getPosition();
            state = taskHistoryItem.getState();
            taskId = taskHistoryItem.getTaskId();
        }
    }

    public Boolean getHistoryItemFound() {
        return historyItemFound;
    }

    public void setHistoryItemFound(Boolean historyItemFound) {
        this.historyItemFound = historyItemFound;
    }

    public Integer getExitCode() {
        return exitCode;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.name = job;
        this.job = job;
    }

    public String getWorkflow() {
        return workflow;
    }

    public void setWorkflow(String workflow) {
        this.name = workflow;
        this.workflow = workflow;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public HistoryState getState() {
        return state;
    }

    public void setState(HistoryState state) {
        this.state = state;
    }

    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    public Boolean getResult() {
        return result;
    }

    public void setResult(Boolean result) {
        this.result = result;
    }

    public void setExitCode(Integer exitCode) {
        this.exitCode = exitCode;
    }
}
