package com.sos.commons.hibernate;

public class MyJoinEntity {

    private Long stepId;
    private String orderKey;
    private String jobName;

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long val) {
        stepId = val;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String val) {
        orderKey = val;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String val) {
        jobName = val;
    }

}
