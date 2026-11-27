
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
    // FileTransfer: Total range 70-89
    // FileTransfer: Non-protocol Fragments (70–74)
    FT_FRAGMENT_CREDENTIAL_STORE(70),
    FT_FRAGMENT_DECRYPTION(71),
    FT_FRAGMENT_MAIL(72),
    FT_FRAGMENT_HTTP_PROXY(73),
    FT_FRAGMENT_SOCKS_PROXY(74),
    // FileTransfer: Protocol fragments (75–81)
    FT_FRAGMENT_AZURE_BLOB_STORAGE(75),
    FT_FRAGMENT_FTP(76),
    FT_FRAGMENT_HTTP(77),
    FT_FRAGMENT_JUMP(78),
    FT_FRAGMENT_SFTP(79),
    FT_FRAGMENT_SMB(80),
    FT_FRAGMENT_WEBDAV(81),
    // Reserve 82–88 for future
    // FileTransfer: JobResource per profile
    FT_PROFILE(89);

    // 99 - see com.sos.joc.model.audit.ObjectType.ORDER
    
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
