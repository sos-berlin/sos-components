package com.sos.joc.event.bean.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "TYPE", visible = true)
@JsonSubTypes({ 
    @JsonSubTypes.Type(ConfigurationGlobalsChanged.class) 
})

public abstract class ConfigurationChangedEvent extends ConfigurationEvent {

    public ConfigurationChangedEvent() {
    }

    public ConfigurationChangedEvent(String key, String controllerId, String configurationType) {
        super(key, controllerId, null);
        putVariable("configurationType", configurationType);
    }

    @JsonIgnore
    public String getConfigurationType() {
        return (String) getVariables().get("configurationType");
    }

}
