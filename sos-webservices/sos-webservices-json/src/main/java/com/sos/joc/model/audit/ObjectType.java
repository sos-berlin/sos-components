
package com.sos.joc.model.audit;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ObjectType {

    FOLDER(0),
    WORKFLOW(1),
    JOBCLASS(2),
    LOCK(4),
    WORKINGDAYSCALENDAR(60),
    NONWORKINGDAYSCALENDAR(61),
    SCHEDULE(7),
    JOBTEMPLATE(8),
    FILEORDERSOURCE(9),
    JOBRESOURCE(10),
    NOTICEBOARD(11),
    INCLUDESCRIPT(12),
    REPORT(15),
    DOCUMENTATION(20),
    FT_FRAGMENT_CREDENTIAL_STORE(70),
    FT_FRAGMENT_DECRYPTION(71),
    FT_FRAGMENT_MAIL(72),
    FT_FRAGMENT_HTTP_PROXY(73),
    FT_FRAGMENT_SOCKS_PROXY(74),
    FT_FRAGMENT_AZURE_BLOB_STORAGE(75),
    FT_FRAGMENT_FTP(76),
    FT_FRAGMENT_HTTP(77),
    FT_FRAGMENT_JUMP(78),
    FT_FRAGMENT_SFTP(79),
    FT_FRAGMENT_SMB(80),
    FT_FRAGMENT_WEBDAV(81),
    FT_PROFILE(89),
    ORDER(99);

    private final Integer intValue;
    private final static Map<String, ObjectType> CONSTANTS = new HashMap<String, ObjectType>();
    private final static Map<Integer, ObjectType> INTCONSTANTS = new HashMap<Integer, ObjectType>();

    static {
        for (ObjectType c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (ObjectType c : values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private ObjectType(Integer intValue) {
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
    public static ObjectType fromValue(String value) {
        ObjectType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static ObjectType fromValue(Integer intValue) {
        ObjectType constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }

}
