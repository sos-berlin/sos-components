package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(HistoryOrderTaskStarted.class),
    @JsonSubTypes.Type(HistoryOrderTaskTerminated.class)
})

public abstract class HistoryTaskEvent extends HistoryEvent {

    public HistoryTaskEvent() {
    }

    public HistoryTaskEvent(String key, String controllerId, String orderId, String jobName, Long historyId, Long historyOrderId) {
        super(key, controllerId, null);
        putVariable("orderId", orderId); // HISTORY_ORDER_STEPS.ORDER_ID
        putVariable("jobName", jobName); // HISTORY_ORDER_STEPS.ORDER_ID
        putVariable("historyId", String.valueOf(historyId)); // HISTORY_ORDER_STEPS.ID
        putVariable("historyOrderId", String.valueOf(historyOrderId));// HISTORY_ORDER_STEPS.HO_ID
    }
    
    @JsonIgnore
    public String getOrderId() {
        return getVariables().get("orderId");
    }
    
    @JsonIgnore
    public String getJobName() {
        return getVariables().get("jobName");
    }

    @JsonIgnore
    public Long getHistoryId() {
        try {
            return Long.parseLong(getVariables().get("historyId"));
        } catch (Throwable e) {
            return null;
        }
    }

    @JsonIgnore
    public Long getHistoryOrderId() {
        try {
            return Long.parseLong(getVariables().get("historyOrderId"));
        } catch (Throwable e) {
            return null;
        }
    }
}
