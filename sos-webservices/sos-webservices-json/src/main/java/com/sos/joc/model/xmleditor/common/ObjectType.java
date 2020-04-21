
package com.sos.joc.model.xmleditor.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ObjectType {

    YADE("YADE"),
    NOTIFICATION("NOTIFICATION"),
    OTHER("OTHER");
    private final String value;
    private final static Map<String, ObjectType> CONSTANTS = new HashMap<String, ObjectType>();

    static {
        for (ObjectType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ObjectType(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static ObjectType fromValue(String value) {
        ObjectType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
