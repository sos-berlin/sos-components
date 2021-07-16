package com.sos.joc.event.bean.monitoring;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ @JsonSubTypes.Type(NotificationConfigurationReleased.class), @JsonSubTypes.Type(NotificationConfigurationRemoved.class),
        @JsonSubTypes.Type(NotificationCreated.class) })

public abstract class MonitoringEvent extends JOCEvent {

    public MonitoringEvent(String key, String controllerId, Map<String, Object> variables) {
        super(key, controllerId, variables);
    }

}
