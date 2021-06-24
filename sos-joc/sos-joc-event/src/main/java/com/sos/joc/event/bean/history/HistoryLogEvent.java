package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(HistoryOrderTaskLog.class),
    @JsonSubTypes.Type(HistoryOrderLog.class),
    @JsonSubTypes.Type(HistoryOrderTaskLogArrived.class),
    @JsonSubTypes.Type(HistoryOrderLogArrived.class)
})

public abstract class HistoryLogEvent extends HistoryEvent {

    public HistoryLogEvent(String key, Long historyOrderId, Long historyOrderStepId, String content, boolean newline) {
        super(key, null, null);
        if (historyOrderId == null) {
            putVariable("historyOrderId", 0L);
        } else {
            putVariable("historyOrderId", historyOrderId);
        }
        if (historyOrderStepId == null) {
            putVariable("historyOrderStepId", 0L);
        } else {
            putVariable("historyOrderStepId", historyOrderStepId);
        }
        if (newline) {
            putVariable("content", content + "\r\n");
        } else {
            putVariable("content", content);
        }
    }
    
    public HistoryLogEvent(String key, Long historyOrderId, Object content) {
        super(key, null, null);
        if (historyOrderId == null) {
            putVariable("historyOrderId", 0L);
        } else {
            putVariable("historyOrderId", historyOrderId);
        }
        putVariable("historyOrderStepId", 0L);
        putVariable("orderLogEntry", content);
    }
    
    @JsonIgnore
    public Long getHistoryOrderId() {
        return (Long) getVariables().get("historyOrderId");
    }

    @JsonIgnore
    public long getHistoryOrderStepId() {
        return ((Long) getVariables().get("historyOrderStepId")).longValue();
    }
    
    @JsonIgnore
    public String getContent() {
        return (String) getVariables().get("content");
    }
    
    @JsonIgnore
    public Object getOrderLogEntry() {
        return getVariables().get("orderLogEntry");
    }
}
