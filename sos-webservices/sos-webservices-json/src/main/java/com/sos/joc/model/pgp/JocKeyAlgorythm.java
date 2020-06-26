
package com.sos.joc.model.pgp;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocKeyAlgorythm {

    PGP("PGP"),
    RSA("RSA");
    private final String value;
    private final static Map<String, JocKeyAlgorythm> CONSTANTS = new HashMap<String, JocKeyAlgorythm>();

    static {
        for (JocKeyAlgorythm c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocKeyAlgorythm(String value) {
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
    public static JocKeyAlgorythm fromValue(String value) {
        JocKeyAlgorythm constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
