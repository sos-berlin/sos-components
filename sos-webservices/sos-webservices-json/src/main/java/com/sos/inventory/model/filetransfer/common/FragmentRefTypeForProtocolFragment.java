
package com.sos.inventory.model.filetransfer.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FragmentRefTypeForProtocolFragment {

    AZURE_BLOB_STORAGE("AZURE_BLOB_STORAGE"),
    FTP("FTP"),
    JUMP("JUMP"),
    SFTP("SFTP"),
    SMB("SMB"),
    HTTP("HTTP"),
    WEBDAV("WEBDAV");
    private final String value;
    private final static Map<String, FragmentRefTypeForProtocolFragment> CONSTANTS = new HashMap<String, FragmentRefTypeForProtocolFragment>();

    static {
        for (FragmentRefTypeForProtocolFragment c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FragmentRefTypeForProtocolFragment(String value) {
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
    public static FragmentRefTypeForProtocolFragment fromValue(String value) {
        FragmentRefTypeForProtocolFragment constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
