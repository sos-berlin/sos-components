package com.sos.joc.history.helper;

public class CachedError {

    private final String state;
    private final String reason;
    private final String code;
    private final String text;

    private Integer returnCode;

    public CachedError(String errorState, String errorReason, String errorCode, String errorText) {
        state = errorState;
        reason = errorReason;
        code = errorCode;
        text = errorText;
    }

    public String getState() {
        return state;
    }

    public String getReason() {
        return reason;
    }

    public String getCode() {
        return code;
    }

    public String getText() {
        return text;
    }

    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    public Integer getReturnCode() {
        return returnCode;
    }
}
