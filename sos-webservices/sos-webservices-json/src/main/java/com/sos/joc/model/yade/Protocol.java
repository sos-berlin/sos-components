
package com.sos.joc.model.yade;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Protocol {

    UNKNOWN("UNKNOWN"),
    AZURE_BLOB_STORAGE("AZURE_BLOB_STORAGE"),
    FTP("FTP"),
    FTPS("FTPS"),
    HTTP("HTTP"),
    HTTPS("HTTPS"),
    LOCAL("LOCAL"),
    SFTP("SFTP"),
    SMB("SMB"),
    WEBDAV("WEBDAV"),
    WEBDAVS("WEBDAVS");
    private final String value;
    private final static Map<String, Protocol> CONSTANTS = new HashMap<String, Protocol>();

    static {
        for (Protocol c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private Protocol(String value) {
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
    public static Protocol fromValue(String value) {
        Protocol constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
