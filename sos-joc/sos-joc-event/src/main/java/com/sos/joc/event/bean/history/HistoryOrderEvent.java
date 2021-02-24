package com.sos.joc.event.bean.history;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(HistoryOrderTerminated.class), 
    @JsonSubTypes.Type(HistoryOrderStarted.class),
    @JsonSubTypes.Type(HistoryOrderUpdated.class)
})

public abstract class HistoryOrderEvent extends HistoryEvent {

    public HistoryOrderEvent() {
    }

    public HistoryOrderEvent(String key, String controllerId, String orderId, Long historyId, Long historyParentId) {
        super(key, controllerId, null);
        putVariable("orderId", orderId); // HISTORY_ORDER_STEPS.ORDER_ID
        putVariable("historyId", String.valueOf(historyId)); // HISTORY_ORDER_STEPS.ID
        putVariable("historyParentId", String.valueOf(historyParentId));// HISTORY_ORDER_STEPS.HO_ID
    }
    
    @JsonIgnore
    public String getOrderId() {
        return getVariables().get("orderId");
    }

    @JsonIgnore
    public Long getHistoryId() {
        try {
            return Long.parseLong(getVariables().get("historyId"));
        } catch (Throwable e) {
            return null;
        }
    }

    // parentId = 0 - mainOrder
    @JsonIgnore
    public Long getHistoryParentId() {
        try {
            return Long.parseLong(getVariables().get("historyParentId"));
        } catch (Throwable e) {
            return null;
        }
    }
}
