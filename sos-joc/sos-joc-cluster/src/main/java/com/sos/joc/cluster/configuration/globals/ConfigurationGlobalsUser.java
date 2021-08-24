package com.sos.joc.cluster.configuration.globals;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.configuration.globals.common.ConfigurationEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class ConfigurationGlobalsUser extends AConfigurationSection {

    private ConfigurationEntry welcomeGotIt = new ConfigurationEntry("welcome_got_it", "false", GlobalSettingsSectionValueType.BOOLEAN);
    private ConfigurationEntry welcomeDoNotRemindMe = new ConfigurationEntry("welcome_do_not_remind_me", "false",
            GlobalSettingsSectionValueType.BOOLEAN);

    public ConfigurationGlobalsUser() {
        int index = -1;
        welcomeGotIt.setOrdering(++index);
        welcomeDoNotRemindMe.setOrdering(++index);
    }

    public ConfigurationEntry getWelcomeGotIt() {
        return welcomeGotIt;
    }

    public ConfigurationEntry getWelcomeDoNotRemindMe() {
        return welcomeDoNotRemindMe;
    }
}
