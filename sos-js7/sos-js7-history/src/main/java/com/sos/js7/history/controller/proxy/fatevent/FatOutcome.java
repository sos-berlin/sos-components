package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Map;

import com.sos.js7.history.controller.proxy.HistoryEventEntry.OutcomeType;

public class FatOutcome {

    private final OutcomeType type;
    private final int returnCode;
    private final boolean isSuccessReturnCode;
    private final boolean isSucceeded;
    private final boolean isFailed;
    private final Map<String, String> keyValues;
    private final String errorMessage;

    public FatOutcome(OutcomeType type, int returnCode, boolean isSuccessReturnCode, boolean isSucceeded, boolean isFailed,
            Map<String, String> keyValues, String errorMessage) {
        this.type = type;
        this.returnCode = returnCode;
        this.isSuccessReturnCode = isSuccessReturnCode;
        this.isSucceeded = isSucceeded;
        this.isFailed = isFailed;
        this.keyValues = keyValues;
        this.errorMessage = errorMessage;
    }

    public OutcomeType getType() {
        return type;
    }

    public int getReturnCode() {
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

    public String getErrorMessage() {
        return errorMessage;
    }

}
