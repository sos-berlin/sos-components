package com.sos.js7.history.controller.proxy.fatevent;

import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.js7.event.controller.EventMeta;
import com.sos.js7.history.controller.proxy.HistoryEventEntry.OutcomeType;

import js7.data.value.Value;

public class FatOutcome {

    private final OutcomeType type;
    private final Integer returnCode;
    private final boolean isSucceeded;
    private final boolean isFailed;
    private final Map<String, Value> namedValues;
    private final String errorCode;
    private final String errorMessage;

    public FatOutcome(OutcomeType type, Integer returnCode, boolean isSucceeded, boolean isFailed, Map<String, Value> namedValues, String errorCode,
            String errorMessage) {
        this.type = type;
        this.returnCode = returnCode;
        this.isSucceeded = isSucceeded;
        this.isFailed = isFailed;
        this.namedValues = namedValues;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
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
        Map<String, String> map = null;
        if (namedValues != null) {
            map = namedValues.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().toString()));
        }
        return EventMeta.map2Json(map);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

}
