package com.sos.jitl.jobs.common;

import java.util.HashMap;
import java.util.Map;

public class JobStepOutcome {

    private Integer returnCode = JobHelper.DEFAULT_RETURN_CODE_SUCCEEDED;
    private Map<String, Object> variables = new HashMap<>();
    private String message;

    protected JobStepOutcome() {
    }

    protected JobStepOutcome(Map<String, Object> variables) {
        this.variables = variables;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    public void putVariable(String name, Object val) {
        variables.put(name, val);
    }

    public Map<String, Object> getVariables() {
        return variables;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String val) {
        message = val;
    }

}
