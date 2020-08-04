
package com.sos.joc.model.pgp;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocKeyAlgorythm {

    PGP(0),
    RSA(1);
    private final Integer value;
    private final static Map<Integer, JocKeyAlgorythm> CONSTANTS = new HashMap<Integer, JocKeyAlgorythm>();

    static {
        for (JocKeyAlgorythm c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocKeyAlgorythm(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static JocKeyAlgorythm fromValue(Integer value) {
        JocKeyAlgorythm constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
