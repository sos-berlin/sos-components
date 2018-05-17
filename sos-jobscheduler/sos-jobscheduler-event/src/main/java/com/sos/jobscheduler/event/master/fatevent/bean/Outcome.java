package com.sos.jobscheduler.event.master.fatevent.bean;

import com.sos.commons.util.SOSString;

public class Outcome {

    private String type;
    private Long returnCode;

    public String getType() {
        return type;
    }

    public void setType(String val) {
        type = val;
    }

    public Long getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Long val) {
        returnCode = val;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
