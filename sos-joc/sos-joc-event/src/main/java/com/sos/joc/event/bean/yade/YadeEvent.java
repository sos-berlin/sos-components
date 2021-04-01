package com.sos.joc.event.bean.yade;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.yade.history.YadeTransferHistoryTerminated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(YadeTransferHistoryTerminated.class) })

public abstract class YadeEvent extends JOCEvent {

    public YadeEvent() {
    }

    public YadeEvent(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
