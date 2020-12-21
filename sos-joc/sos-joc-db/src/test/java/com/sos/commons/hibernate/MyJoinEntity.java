package com.sos.commons.hibernate;

public class MyJoinEntity {

    private Long stepId;
    private String orderId;
    private String jobName;

    public Long getStepId() {
        return stepId;
    }

    public void setStepId(Long val) {
        stepId = val;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String val) {
        orderId = val;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String val) {
        jobName = val;
    }

}
