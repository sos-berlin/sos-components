package com.sos.cluster.configuration;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsCleanup;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;

public class ConfigurationGlobalsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationGlobalsTest.class);

    @Ignore
    @Test
    public void testCleanup2() throws Exception {

        ConfigurationGlobals c = new ConfigurationGlobals();
        c.setConfigurationValues(null);
        AConfigurationSection s = c.getConfigurationSection(DefaultSections.cleanup);
        ConfigurationGlobalsCleanup cleanup = (ConfigurationGlobalsCleanup) s;

        LOGGER.info(SOSString.toString(cleanup));
        LOGGER.info(SOSString.toString(c.getInitialSettings("Europe/Berlin")));
        LOGGER.info(SOSString.toString(c.getDefaults()));
    }
}
