
package com.sos.joc.model.pgp;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum JocKeyAlgorithm {

    PGP(0),
    RSA(1),
    ECDSA(2);
    private final Integer value;
    private final static Map<Integer, JocKeyAlgorithm> CONSTANTS = new HashMap<Integer, JocKeyAlgorithm>();

    static {
        for (JocKeyAlgorithm c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private JocKeyAlgorithm(Integer value) {
        this.value = value;
    }

    @JsonValue
    public Integer value() {
        return this.value;
    }

    @JsonCreator
    public static JocKeyAlgorithm fromValue(Integer value) {
        JocKeyAlgorithm constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException((value +""));
        } else {
            return constant;
        }
    }

}
