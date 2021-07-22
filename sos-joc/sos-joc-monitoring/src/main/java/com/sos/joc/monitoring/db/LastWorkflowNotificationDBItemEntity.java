package com.sos.joc.monitoring.db;

public class LastWorkflowNotificationDBItemEntity {

    private Long id;
    private Integer type;
    private String notificationId;
    private Long stepId;

    public Long getId() {
        return id;
    }

    public void setId(Long val) {
        id = val;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer val) {
        type = val;
    }

    public String getNotificationId() {
        return notificationId;
    }

    public void setNotificationId(String val) {
        notificationId = val;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long val) {
        stepId = val;
    }

}
