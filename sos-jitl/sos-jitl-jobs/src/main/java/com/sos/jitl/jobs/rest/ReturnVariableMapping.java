package com.sos.jitl.jobs.rest;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReturnVariableMapping {
    @JsonProperty("name")
    private String name;
    @JsonProperty("path")
    private String path;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}