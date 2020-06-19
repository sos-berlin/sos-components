package com.sos.js7.event.controller.fatevent.bean;

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
