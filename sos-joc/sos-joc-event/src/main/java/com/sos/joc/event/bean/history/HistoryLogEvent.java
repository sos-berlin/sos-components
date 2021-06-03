package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(HistoryOrderTaskLog.class),
    @JsonSubTypes.Type(HistoryOrderLog.class)
})

public abstract class HistoryLogEvent extends HistoryEvent {

    public HistoryLogEvent(String key, Long historyOrderId, Long historyOrderStepId, String content) {
        super(key, null, null);
        if (historyOrderId == null) {
            putVariable("historyOrderId", null);
        } else {
            putVariable("historyOrderId", historyOrderId);
        }
        if (historyOrderStepId == null) {
            putVariable("historyOrderStepId", null);
        } else {
            putVariable("historyOrderStepId", historyOrderStepId);
        }
        putVariable("content", content);
    }
    
    @JsonIgnore
    public Long getHistoryOrderId() {
        return (Long) getVariables().get("historyOrderId");
    }

    @JsonIgnore
    public Long getHistoryOrderStepId() {
        return (Long) getVariables().get("historyOrderStepId");
    }
    
    @JsonIgnore
    public String getContent() {
        return (String) getVariables().get("content");
    }
}
