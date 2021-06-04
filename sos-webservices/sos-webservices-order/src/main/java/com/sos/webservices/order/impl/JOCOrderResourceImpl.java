package com.sos.webservices.order.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;

public class JOCOrderResourceImpl extends JOCResourceImpl{
    protected OrderInitiatorSettings settings;
    private static final Logger LOGGER = LoggerFactory.getLogger(CyclicOrdersImpl.class);

    protected void setSettings() throws Exception {
        if (Globals.configurationGlobals == null) {
            settings = new OrderInitiatorSettings();
            settings.setTimeZone("Europe/Berlin");
            settings.setPeriodBegin("00:00");
            LOGGER.warn("Could not read settings. Using defaults");
        } else {
            GlobalSettingsReader reader = new GlobalSettingsReader();
            AConfigurationSection section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
            this.settings = reader.getSettings(section);
        }
    }

}
