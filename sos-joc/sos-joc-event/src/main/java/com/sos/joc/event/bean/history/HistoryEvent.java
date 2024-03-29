package com.sos.joc.event.bean.history;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(HistoryOrderTerminated.class), 
    @JsonSubTypes.Type(HistoryOrderStarted.class),
    @JsonSubTypes.Type(HistoryOrderUpdated.class),
    @JsonSubTypes.Type(HistoryOrderTaskStarted.class),
    @JsonSubTypes.Type(HistoryOrderTaskTerminated.class),
    @JsonSubTypes.Type(HistoryOrderTaskLog.class),
    @JsonSubTypes.Type(HistoryOrderLog.class),
    @JsonSubTypes.Type(HistoryOrderTaskLogArrived.class),
    @JsonSubTypes.Type(HistoryOrderLogArrived.class),
    @JsonSubTypes.Type(HistoryOrderTaskLogFirstStderr.class)
})

public abstract class HistoryEvent extends JOCEvent {

    private final Object payload;

    public HistoryEvent(String key, String controllerId, Map<String, Object> variables, Object payload) {
        super(key, controllerId, variables);
        this.payload = payload;
    }
    
    public HistoryEvent(String key, String controllerId, Map<String, Object> variables) {
        super(key, controllerId, variables);
        this.payload = null;
    }

    @JsonIgnore
    public Object getPayload() {
        return payload;
    }

}
