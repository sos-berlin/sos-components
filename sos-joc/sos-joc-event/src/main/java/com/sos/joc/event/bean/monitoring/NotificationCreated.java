package com.sos.joc.event.bean.monitoring;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class NotificationCreated extends MonitoringEvent {

    public NotificationCreated(String controllerId, Long notificationId, Integer type, String workflowName, String orderId, String jobName,
            Date created, String messsage) {
        super(NotificationCreated.class.getSimpleName(), controllerId, null);
        putVariables(notificationId, type, workflowName, orderId, jobName, created, messsage);
    }

    @JsonIgnore
    private void putVariables(Long notificationId, Integer type, String workflowName, String orderId, String jobName, Date created, String messsage) {
        putVariable("notificationId", notificationId);
        putVariable("created", created);
        putVariable("level", type);
        putVariable("messsage", messsage);
        putVariable("workflowName", workflowName);
        putVariable("orderId", orderId);
        putVariable("jobName", jobName);
    }

    @JsonIgnore
    public Long getNotificationId() {
        return (Long) getVariables().get("notificationId");
    }
    
    @JsonIgnore
    public Integer getLevel() {
        return (Integer) getVariables().get("level");
    }
    
    @JsonIgnore
    public Date getDate() {
        return (Date) getVariables().get("created");
    }
    
    @JsonIgnore
    public String getWorkflowName() {
        return (String) getVariables().get("workflowName");
    }
    
    @JsonIgnore
    public String getOrderId() {
        return (String) getVariables().get("orderId");
    }
    
    @JsonIgnore
    public String getJobName() {
        return (String) getVariables().get("jobName");
    }
    
    @JsonIgnore
    public String getMessage() {
        return (String) getVariables().get("messsage");
    }
}
