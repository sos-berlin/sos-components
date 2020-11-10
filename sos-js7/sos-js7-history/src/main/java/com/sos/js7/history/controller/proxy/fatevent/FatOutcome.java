package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Map;

import com.sos.js7.history.controller.proxy.HistoryEventEntry.OutcomeType;

public class FatOutcome {

    private final OutcomeType type;
    private final Integer returnCode;
    private final boolean isSuccessReturnCode;
    private final boolean isSucceeded;
    private final boolean isFailed;
    private final Map<String, String> keyValues;
    private final String errorCode;
    private final String errorMessage;

    public FatOutcome(OutcomeType type, Integer returnCode, boolean isSuccessReturnCode, boolean isSucceeded, boolean isFailed,
            Map<String, String> keyValues, String errorCode, String errorMessage) {
        this.type = type;
        this.returnCode = returnCode;
        this.isSuccessReturnCode = isSuccessReturnCode;
        this.isSucceeded = isSucceeded;
        this.isFailed = isFailed;
        this.keyValues = keyValues;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public OutcomeType getType() {
        return type;
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public boolean isSuccessReturnCode() {
        return isSuccessReturnCode;
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public Map<String, String> getKeyValues() {
        return keyValues;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
