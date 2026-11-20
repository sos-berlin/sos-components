
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
    // FileTransfer: Total range 80-99
    // FileTransfer: Non-protocol Fragments (80–84)
    FT_FRAGMENT_CREDENTIAL_STORE(80), 
    FT_FRAGMENT_DECRYPTION(81), 
    FT_FRAGMENT_MAIL(82), 
    FT_FRAGMENT_HTTP_PROXY(83), 
    FT_FRAGMENT_SOCKS_PROXY(84),
    // FileTransfer: Protocol fragments (85–91)
    FT_FRAGMENT_AZURE_BLOB_STORAGE(85), 
    FT_FRAGMENT_FTP(86), 
    FT_FRAGMENT_HTTP(87), 
    FT_FRAGMENT_JUMP(88), 
    FT_FRAGMENT_SFTP(89), 
    FT_FRAGMENT_SMB(90), 
    FT_FRAGMENT_WEBDAV(91),
    // Reserve 92–98 for future
    // FileTransfer: JobResource per profile
    FT_PROFILE_JOBRESOURCE(99);

    private final Integer intValue;
    private final static Map<String, ConfigurationType> CONSTANTS = new HashMap<String, ConfigurationType>();
    private final static Map<Integer, ConfigurationType> INTCONSTANTS = new HashMap<Integer, ConfigurationType>();

    static {
        for (ConfigurationType c : values()) {
            CONSTANTS.put(c.name(), c);
        }
    }

    static {
        for (ConfigurationType c : values()) {
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
