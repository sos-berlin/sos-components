
package com.sos.joc.model.docu;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Generated("org.jsonschema2pojo")
public enum MimeType {

    HTML("HTML"),
    XML("XML"),
    MD("MD");
    private final String value;
    private final static Map<String, MimeType> CONSTANTS = new HashMap<String, MimeType>();

    static {
        for (MimeType c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private MimeType(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator
    public static MimeType fromValue(String value) {
        MimeType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
