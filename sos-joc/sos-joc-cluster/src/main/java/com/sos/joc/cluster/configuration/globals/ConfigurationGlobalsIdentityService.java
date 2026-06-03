package com.sos.joc.cluster.configuration.globals;

import java.util.Optional;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsIdentityService extends AConfigurationSection {

    private static final String INITIAL = "initial";
    private ConfigurationEntry idleSessionTimeout = new ConfigurationEntry("idle_session_timeout", "30m",
            GlobalSettingsSectionValueType.DURATION);
    private ConfigurationEntry initialPassword = new ConfigurationEntry("initial_password", INITIAL, GlobalSettingsSectionValueType.PASSWORD);
    private ConfigurationEntry minimumPasswordLength = new ConfigurationEntry("minimum_password_length", "1",
            GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);

    public ConfigurationGlobalsIdentityService() {
        int index = -1;
        idleSessionTimeout.setOrdering(++index);
        initialPassword.setOrdering(++index);
        minimumPasswordLength.setOrdering(++index);
    }

    public ConfigurationEntry getIdleSessionTimeout() {
        return idleSessionTimeout;
    }

    public ConfigurationEntry getInitialPassword() {
        return initialPassword;
    }

    public Integer getMininumPasswordLength() {
        String _default = Optional.ofNullable(minimumPasswordLength).map(ConfigurationEntry::getDefault).orElse("1");
        return Integer.valueOf(Optional.ofNullable(minimumPasswordLength).map(ConfigurationEntry::getValue).orElse(_default));
    }

}
