package com.sos.jitl.jobs.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class KeyAndValue {
    @JsonProperty("key")
    private String key;

    @JsonProperty("value")
    private String value;

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
