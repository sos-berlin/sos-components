package com.sos.js7.converter.commons.beans;

public class JS7Lock extends AJS7Object {

    private final String name;
    private final Integer capacity;

    public JS7Lock(String name, Integer capacity) {
        this.name = name;
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public Integer getCapacity() {
        return capacity;
    }

}
