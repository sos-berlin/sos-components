package com.sos.joc.history.controller.proxy.fatevent;

import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OutcomeInfo;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.OutcomeType;
import com.sos.joc.history.helper.HistoryUtil;

import js7.data.value.Value;

public class FatOutcome {

    private final OutcomeType type;
    private final Integer returnCode;
    private final boolean isSucceeded;
    private final boolean isFailed;
    private final Map<String, Value> namedValues;
    private final String errorCode;
    private final String errorMessage;

    private String errorReason;

    public FatOutcome(OutcomeInfo oi) {
        this.type = oi.getType();
        this.returnCode = oi.getReturnCode();
        this.isSucceeded = oi.isSucceeded();
        this.isFailed = oi.isFailed();
        this.namedValues = oi.getNamedValues();
        this.errorCode = oi.getErrorCode();
        this.errorMessage = oi.getErrorMessage();
        if (isFailed) {
            if (oi.getErrorReason() == null) {
                this.errorReason = type == null ? null : type.name();
            } else {
                this.errorReason = oi.getErrorReason().name();
            }
        }
    }

    public OutcomeType getType() {
        return type;
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

    public String getNamedValuesAsJsonString() throws JsonProcessingException {
        return HistoryUtil.toJsonString(namedValues);
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
