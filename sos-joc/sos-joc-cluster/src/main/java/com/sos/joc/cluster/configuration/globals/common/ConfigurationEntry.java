package com.sos.joc.cluster.configuration.globals.common;

import java.util.List;

import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationEntry {

    private final GlobalSettingsSectionValueType type;
    private final List<String> values;
    private final String name;
    private final String defaultValue;
    private String value;
    private int ordering;

    public ConfigurationEntry(String name, String defaultValue, GlobalSettingsSectionValueType type) {
        this(name, defaultValue, null, type);
    }

    public ConfigurationEntry(String name, String defaultValue, List<String> values, GlobalSettingsSectionValueType type) {
        this.name = name;
        this.defaultValue = defaultValue;
        this.values = values;
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

    public List<String> getValues() {
        return values;
    }
}
