package com.sos.reports.classes;

import java.time.LocalDateTime;

public class OrderRecord {

    private Integer id;
    private String controllerId;
    private String orderId;
    private String workflowPath;
    private String workflowVersionId;
    private String workflowName;
    private LocalDateTime startTime;
    private LocalDateTime plannedTime;
    private LocalDateTime endTime;
    private Boolean error;
    private Integer orderState;
    private Integer state;
    private LocalDateTime created;
    private LocalDateTime modified;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setId(String id) {
        this.id = ReportHelper.string2Integer(id);
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

    public String getWorkflowVersionId() {
        return workflowVersionId;
    }

    public void setWorkflowVersionId(String workflowVersionId) {
        this.workflowVersionId = workflowVersionId;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public void setStartTime(String startTime) {  
        startTime = startTime.replace(' ', 'T');
        this.startTime = LocalDateTime.parse(startTime);
    }

    public LocalDateTime getPlannedTime() {
        return plannedTime;
    }

    public void setPlannedTime(LocalDateTime plannedTime) {
        this.plannedTime = plannedTime;
    }

    public void setPlannedTime(String plannedTime) {
        plannedTime = plannedTime.replace(' ', 'T');
        this.plannedTime = LocalDateTime.parse(plannedTime);
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        if (!endTime.isEmpty()) {
            endTime = endTime.replace(' ', 'T');
            this.endTime = LocalDateTime.parse(endTime);
        }
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public void setError(String error) {
        this.error = error.equals("1");
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public void setCreated(LocalDateTime created) {
        this.created = created;
    }

    public void setCreated(String created) {
        created = created.replace(' ', 'T');
        this.created = LocalDateTime.parse(created);
    }

    public LocalDateTime getModified() {
        return modified;
    }

    public void setModified(LocalDateTime modified) {
        this.modified = modified;
    }

    public void setModified(String modified) {
        modified = modified.replace(' ', 'T');
        this.modified = LocalDateTime.parse(modified);
    }

    public Integer getOrderState() {
        return orderState;
    }

    public void setOrderState(Integer orderState) {
        this.orderState = orderState;
    }

    public void setOrderState(String orderState) {
        this.state = ReportHelper.string2Integer(orderState);
    }

    public Integer getState() {
        return state;
    }

    public void setState(String state) {
        this.state = ReportHelper.string2Integer(state);
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public void setState(Integer state) {
        this.state = state;
    }

}
