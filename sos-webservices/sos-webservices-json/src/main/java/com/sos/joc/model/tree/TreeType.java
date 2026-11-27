
package com.sos.joc.model.tree;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TreeType {

    INVENTORY(99),
    FOLDER(0),
    WORKFLOW(1),
    JOBCLASS(2),
    LOCK(4),
    SCHEDULE(7),
    JOBTEMPLATE(8),
    FILEORDERSOURCE(9),
    JOBRESOURCE(10),
    NOTICEBOARD(11),
    INCLUDESCRIPT(12),
    DEPLOYMENTDESCRIPTOR(13),
    DESCRIPTORFOLDER(14),
    REPORT(15),
    DOCUMENTATION(20),
    WORKINGDAYSCALENDAR(60),
    NONWORKINGDAYSCALENDAR(61),
    FT_FRAGMENT_CREDENTIAL_STORE(80),
    FT_FRAGMENT_DECRYPTION(81),
    FT_FRAGMENT_MAIL(82),
    FT_FRAGMENT_HTTP_PROXY(83),
    FT_FRAGMENT_SOCKS_PROXY(84),
    FT_FRAGMENT_AZURE_BLOB_STORAGE(85),
    FT_FRAGMENT_FTP(86),
    FT_FRAGMENT_HTTP(87),
    FT_FRAGMENT_JUMP(88),
    FT_FRAGMENT_SFTP(89),
    FT_FRAGMENT_SMB(90),
    FT_FRAGMENT_WEBDAV(91),
    FT_PROFILE(95);

    private final Integer intValue;
    private final static Map<String, TreeType> CONSTANTS = new HashMap<String, TreeType>();
    private final static Map<Integer, TreeType> INTCONSTANTS = new HashMap<Integer, TreeType>();

    static {
        for (TreeType c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (TreeType c : values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private TreeType(Integer intValue) {
        this.intValue = intValue;
    }

    @Override
    public String toString() {
        return this.name();
    }

    @JsonValue
    public String value() {
        return this.name();
    }

    public Integer intValue() {
        return this.intValue;
    }

    @JsonCreator
    public static TreeType fromValue(String value) {
        TreeType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static TreeType fromValue(Integer intValue) {
        TreeType constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }

}
