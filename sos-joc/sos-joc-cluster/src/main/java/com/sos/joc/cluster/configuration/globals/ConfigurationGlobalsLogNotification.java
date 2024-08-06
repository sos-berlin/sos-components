package com.sos.joc.cluster.configuration.globals;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsLogNotification extends AConfigurationSection {

    private static final int DEFAULT_PORT = 4245;
    private static final int DEFAULT_MAX_MESSAGES_PER_SECOND = 1000;
    private static final boolean  DEFAULT_IS_ACTIVE = false;

    private ConfigurationEntry active = new ConfigurationEntry("log_server_active", DEFAULT_IS_ACTIVE + "", GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry updPort = new ConfigurationEntry("log_server_port", DEFAULT_PORT + "", GlobalSettingsSectionValueType.POSITIVEINTEGER);
    private ConfigurationEntry maxMessagesPerSecond = new ConfigurationEntry("log_server_max_messages_per_second", DEFAULT_MAX_MESSAGES_PER_SECOND
            + "", GlobalSettingsSectionValueType.POSITIVEINTEGER);

    public ConfigurationGlobalsLogNotification() {
        int index = -1;
        active.setOrdering(++index);
        updPort.setOrdering(++index);
        //maxMessagesPerSecond.setOrdering(++index);
    }

    public boolean isActive() {
        if(active.getValue() == null) {
            return DEFAULT_IS_ACTIVE;
        }
        return active.getValue().equalsIgnoreCase("true");
    }
    
    public Integer getPort() {
        try {
            return Integer.parseInt(updPort.getValue());
        } catch (Exception e) {
            return DEFAULT_PORT; //TODO or better set active=false?
        }
    }
    
    public Integer getMaxMessagesPerSecond() {
        try {
            return Integer.parseInt(maxMessagesPerSecond.getValue());
        } catch (Exception e) {
            return DEFAULT_MAX_MESSAGES_PER_SECOND;
        }
    }
    
    public static final int getDefaultPort() {
        return DEFAULT_PORT;
    }
    
    public static final boolean getDefaultIsActive() {
        return DEFAULT_IS_ACTIVE;
    }
    public static final int getDefaultMaxMessagesPerSecond() {
        return DEFAULT_MAX_MESSAGES_PER_SECOND;
    }

}