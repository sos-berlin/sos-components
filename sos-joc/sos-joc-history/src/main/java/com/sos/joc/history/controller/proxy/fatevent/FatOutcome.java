package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Map;

import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OutcomeInfo;

import js7.data.value.Value;

public class FatOutcome {

    private final Integer returnCode;
    private final boolean isSucceeded;
    private final boolean isFailed;
    private final Map<String, Value> namedValues;
    private final String errorCode;
    private final String errorMessage;

    private String errorReason;

    public FatOutcome(OutcomeInfo oi) {
        this.returnCode = oi.getReturnCode();
        this.isSucceeded = oi.isSucceeded();
        this.isFailed = oi.isFailed();
        this.namedValues = oi.getNamedValues();
        this.errorCode = oi.getErrorCode();
        this.errorMessage = oi.getErrorMessage();
        if (isFailed) {
            if (oi.getNonJobInstructionFailureReason() == null) {
                this.errorReason = oi.getType() == null ? null : oi.getType().name();
            } else {
                this.errorReason = oi.getNonJobInstructionFailureReason().name();
            }
        }
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public boolean isSucceeded() {
        return isSucceeded;
    }

    public boolean isFailed() {
        return isFailed;
    }

    public Map<String, Value> getNamedValues() {
        return namedValues;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getErrorReason() {
        return errorReason;
    }

}
