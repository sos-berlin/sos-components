package com.sos.joc.event.bean.history;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(HistoryOrderTerminated.class), @JsonSubTypes.Type(HistoryOrderStarted.class),
        @JsonSubTypes.Type(HistoryOrderTaskTerminated.class) })

public abstract class HistoryEvent extends JOCEvent {

    public HistoryEvent() {
    }

    public HistoryEvent(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
