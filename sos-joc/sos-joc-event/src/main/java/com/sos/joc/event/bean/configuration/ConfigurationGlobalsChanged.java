package com.sos.joc.event.bean.configuration;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ConfigurationGlobalsChanged extends ConfigurationChangedEvent {

    public ConfigurationGlobalsChanged(String controllerId, String configurationType, List<String> sections) {
        super(ConfigurationGlobalsChanged.class.getSimpleName(), controllerId, configurationType);
        putVariable("sections", sections);
    }

    @SuppressWarnings("unchecked")
    @JsonIgnore
    public List<String> getSections() {
        return (List<String>) getVariables().get("sections");
    }

}
