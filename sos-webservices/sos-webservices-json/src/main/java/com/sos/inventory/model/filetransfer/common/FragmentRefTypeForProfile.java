
package com.sos.inventory.model.filetransfer.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FragmentRefTypeForProfile {

    AZURE_BLOB_STORAGE("AZURE_BLOB_STORAGE"),
    FTP("FTP"),
    SFTP("SFTP"),
    SMB("SMB"),
    HTTP("HTTP"),
    WEBDAV("WEBDAV"),
    LOCAL("LOCAL");
    private final String value;
    private final static Map<String, FragmentRefTypeForProfile> CONSTANTS = new HashMap<String, FragmentRefTypeForProfile>();

    static {
        for (FragmentRefTypeForProfile c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FragmentRefTypeForProfile(String value) {
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
    public static FragmentRefTypeForProfile fromValue(String value) {
        FragmentRefTypeForProfile constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
