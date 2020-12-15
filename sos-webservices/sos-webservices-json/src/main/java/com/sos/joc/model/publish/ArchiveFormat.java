
package com.sos.joc.model.publish;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ArchiveFormat {

    ZIP("ZIP"),
    TAR_GZ("TAR_GZ");
    private final String value;
    private final static Map<String, ArchiveFormat> CONSTANTS = new HashMap<String, ArchiveFormat>();

    static {
        for (ArchiveFormat c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private ArchiveFormat(String value) {
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
    public static ArchiveFormat fromValue(String value) {
        ArchiveFormat constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
