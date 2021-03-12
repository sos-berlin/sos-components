package com.sos.joc.cluster.configuration.globals.common;

import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationEntry {

    private final GlobalSettingsSectionValueType type;
    private final String name;
    private final String defaultValue;
    private String value;
    private int ordering;

    public ConfigurationEntry(String name, String defaultValue, GlobalSettingsSectionValueType type) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getDefault() {
        return defaultValue;
    }

    public GlobalSettingsSectionValueType getType() {
        return type;
    }

    public void setOrdering(int val) {
        ordering = val;
    }

    public int getOrdering() {
        return ordering;
    }

    public void setValue(String val) {
        value = val;
    }

    public String getValue() {
        return value;
    }

}
