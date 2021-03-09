package com.sos.joc.event.bean.configuration;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.joc.event.bean.JOCEvent;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(ConfigurationGlobalsChanged.class), 
})

public abstract class ConfigurationEvent extends JOCEvent {

    public ConfigurationEvent() {
    }

    public ConfigurationEvent(String key, String controllerId, Map<String, String> variables) {
        super(key, controllerId, variables);
    }
}
