package com.sos.js7.job;

import java.util.HashMap;
import java.util.Map;

import com.sos.commons.util.SOSString;

public class OrderProcessStepOutcome {

    private final Map<String, Object> variables;

    private Integer returnCode;
    private String message;
    private boolean failed;

    protected OrderProcessStepOutcome() {
        this.variables = new HashMap<>();
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    public void putVariable(OrderProcessStepOutcomeVariable<?> var) {
        if (var == null) {
            return;
        }
        variables.put(var.getName(), var.getValue());
    }

    public void putVariable(String name, Object val) {
        variables.put(name, val);
    }

    public void putVariables(Map<String, Object> val) {
        if (val != null) {
            variables.putAll(val);
        }
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

    public void setFailed() {
        failed = true;
    }

    public boolean isFailed() {
        return failed;
    }

    protected boolean hasVariables() {
        return variables != null && variables.size() > 0;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("returnCode=").append(returnCode == null ? "" : returnCode);
        sb.append(",failed=").append(failed);
        if (variables != null) {
            sb.append(",variables=").append(SOSString.toString(variables));
        }
        if (message != null) {
            sb.append(",message=").append(message);
        }
        return sb.toString();
    }

}
