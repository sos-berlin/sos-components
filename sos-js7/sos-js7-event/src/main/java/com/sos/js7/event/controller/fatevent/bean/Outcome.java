package com.sos.js7.event.controller.fatevent.bean;

import java.util.LinkedHashMap;

import com.sos.commons.util.SOSString;

public class Outcome {

    private String type;
    private Long returnCode;
    private Reason reason;
    // TODO master error?
    // e.g. when
    // echo %SCHEDULER_PARAM_JOB% running>&2\r\necho %SCHEDULER_PARAM_JOB% > %SCHEDULER_RETURN_VALUES% instead of
    // echo %SCHEDULER_PARAM_JOB% running>&2\r\necho jobName=%SCHEDULER_PARAM_JOB% > %SCHEDULER_RETURN_VALUES%
    private LinkedHashMap<String, String> keyValues;

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

    public Reason getReason() {
        return reason;
    }

    public void setReason(Reason val) {
        reason = val;
    }

    public LinkedHashMap<String, String> getKeyValues() {
        return keyValues;
    }

    public void setKeyValues(LinkedHashMap<String, String> val) {
        keyValues = val;
    }

    @Override
    public String toString() {
        return SOSString.toString(this);
    }
}
