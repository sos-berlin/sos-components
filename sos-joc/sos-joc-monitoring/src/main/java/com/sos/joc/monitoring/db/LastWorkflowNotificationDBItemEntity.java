package com.sos.joc.monitoring.db;

public class LastWorkflowNotificationDBItemEntity {

    private Long id;
    private Integer type;
    private String name;
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

    public String getName() {
        return name;
    }

    public void setName(String val) {
        name = val;
    }

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long val) {
        stepId = val;
    }

}
