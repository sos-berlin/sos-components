package com.sos.cluster.configuration;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.configuration.JocClusterGlobalSettings;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;

public class JocClusterGlobalSettingsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterGlobalSettingsTest.class);

    @Ignore
    @Test
    public void testCleanup() throws Exception {
        GlobalSettings settings = JocClusterGlobalSettings.getDefaultSettings();
        JocClusterGlobalSettings.setCleanupInitialPeriod(settings);
        JocClusterGlobalSettings.useAndRemoveDefaultInfos(settings);

        LOGGER.info(SOSString.toString(settings));

        GlobalSettingsSection defaultSettings = JocClusterGlobalSettings.getDefaultSettings(ClusterServices.cleanup);
        GlobalSettingsSectionEntry timezone = JocClusterGlobalSettings.getSectionEntry(defaultSettings, "time_zone");

        LOGGER.info(timezone.getDefault());
    }

}
