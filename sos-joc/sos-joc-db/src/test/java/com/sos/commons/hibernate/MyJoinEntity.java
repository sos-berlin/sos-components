package com.sos.commons.hibernate;

public class MyJoinEntity extends MyJoinEntityParent {

    private String stepId;
    private String jobName;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(Long val) {
        stepId = val == null ? null : String.valueOf(val);
    }

    public void setNumberValue(Number val) {

    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String val) {
        jobName = val;
    }

}
