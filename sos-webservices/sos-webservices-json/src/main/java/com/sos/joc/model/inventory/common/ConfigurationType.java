
package com.sos.joc.model.inventory.common;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ConfigurationType {

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
    DEPLOYMENTDESCRIPTOR(13),
    DESCRIPTORFOLDER(14),
    REPORT(15),
    FT_FRAGMENT_CREDENTIAL_STORE(80),
    FT_FRAGMENT_DECRYPTION(81),
    FT_FRAGMENT_PROXY_HTTP(82),
    FT_FRAGMENT_PROXY_SOCKS(83),
    FT_FRAGMENT_AZURE_BLOB_STORAGE(84),
    FT_FRAGMENT_FTP(85),
    FT_FRAGMENT_HTTP(86),
    FT_FRAGMENT_JUMP(87),
    FT_FRAGMENT_SFTP(88),
    FT_FRAGMENT_SMB(89),
    FT_FRAGMENT_WEBDAV(90),
    FT_PROFILE(91);
    private final Integer intValue;
    private final static Map<String, ConfigurationType> CONSTANTS = new HashMap<String, ConfigurationType>();
    private final static Map<Integer, ConfigurationType> INTCONSTANTS = new HashMap<Integer, ConfigurationType>();

    static {
        for (ConfigurationType c: values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (ConfigurationType c: values()) {
            INTCONSTANTS.put(c.intValue, c);
        }
    }

    private ConfigurationType(Integer intValue) {
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
    public static ConfigurationType fromValue(String value) {
        ConfigurationType constant = CONSTANTS.get(value);
        if (constant == null) {
            throw new IllegalArgumentException(value);
        } else {
            return constant;
        }
    }

    public static ConfigurationType fromValue(Integer intValue) {
        ConfigurationType constant = INTCONSTANTS.get(intValue);
        if (constant == null) {
            throw new IllegalArgumentException(intValue + "");
        } else {
            return constant;
        }
    }

}
