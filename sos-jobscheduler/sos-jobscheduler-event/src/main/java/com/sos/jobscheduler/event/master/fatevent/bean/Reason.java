package com.sos.jobscheduler.event.master.fatevent.bean;

import com.sos.commons.util.SOSString;

public class Reason {

    private String type;
    private Problem problem;

    public String getType() {
        return type;
    }

    public void setType(String val) {
        type = val;
    }

    public Problem getProblem() {
        return problem;
    }

    public void setProblem(Problem val) {
        problem = val;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
