package com.sos.jobscheduler.event.master.fatevent.bean;

import com.sos.commons.util.SOSString;

public class Problem {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        message = val;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
