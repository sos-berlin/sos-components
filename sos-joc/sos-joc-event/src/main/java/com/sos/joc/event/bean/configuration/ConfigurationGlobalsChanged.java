package com.sos.joc.event.bean.configuration;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class ConfigurationGlobalsChanged extends ConfigurationChangedEvent {

    public ConfigurationGlobalsChanged(String controllerId, String configurationType, List<String> sections) {
        super(ConfigurationGlobalsChanged.class.getSimpleName(), controllerId, configurationType);
        putVariable("sections", String.join(",", sections));
    }

    @JsonIgnore
    public List<String> getSections() {
        return Stream.of(getVariables().get("sections").split(",")).collect(Collectors.toList());
    }

}
