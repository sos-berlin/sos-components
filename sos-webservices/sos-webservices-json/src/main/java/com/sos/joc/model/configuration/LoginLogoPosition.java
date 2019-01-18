
package com.sos.joc.model.configuration;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

@Generated("org.jsonschema2pojo")
public enum LoginLogoPosition {

    TOP("TOP"),
    BOTTOM("BOTTOM");
    private final String value;
    private final static Map<String, LoginLogoPosition> CONSTANTS = new HashMap<String, LoginLogoPosition>();

    static {
        for (LoginLogoPosition c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private LoginLogoPosition(String value) {
        this.value = value;
    }

    @JsonValue
    @Override
    public String toString() {
        return this.value;
    }

    @JsonCreator
    public static LoginLogoPosition fromValue(String value) {
        LoginLogoPosition constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
