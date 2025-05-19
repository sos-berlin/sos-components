package com.sos.joc.event.bean.yade;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.yade.history.YADETransferHistoryTerminated;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(YADETransferHistoryTerminated.class), @JsonSubTypes.Type(YADEConfigurationDeployed.class) })

public abstract class YADEEvent extends JOCEvent {

    public YADEEvent() {
    }

    public YADEEvent(String key, String controllerId, Map<String, Object> variables) {
        super(key, controllerId, variables);
    }
}
