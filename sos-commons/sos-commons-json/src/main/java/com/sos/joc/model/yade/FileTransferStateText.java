
package com.sos.joc.model.yade;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum FileTransferStateText {

    UNDEFINED("UNDEFINED"),
    WAITING("WAITING"),
    TRANSFERRING("TRANSFERRING"),
    IN_PROGRESS("IN_PROGRESS"),
    TRANSFERRED("TRANSFERRED"),
    SUCCESS("SUCCESS"),
    SKIPPED("SKIPPED"),
    FAILED("FAILED"),
    ABORTED("ABORTED"),
    COMPRESSED("COMPRESSED"),
    NOT_OVERWRITTEN("NOT_OVERWRITTEN"),
    DELETED("DELETED"),
    RENAMED("RENAMED"),
    IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT("IGNORED_DUE_TO_ZEROBYTE_CONSTRAINT"),
    ROLLED_BACK("ROLLED_BACK"),
    POLLING("POLLING");
    private final String value;
    private final static Map<String, FileTransferStateText> CONSTANTS = new HashMap<String, FileTransferStateText>();

    static {
        for (FileTransferStateText c: values()) {
            CONSTANTS.put(c.value, c);
        }
    }

    private FileTransferStateText(String value) {
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
    public static FileTransferStateText fromValue(String value) {
        FileTransferStateText constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

}
