package com.sos.cluster.configuration;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsCleanup;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntryChildren;

public class ConfigurationGlobalsTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationGlobalsTest.class);
    private static final ObjectMapper MAPPER = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
            DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

    @Ignore
    @Test
    public void testCleanupToDefaults() throws Exception {

        ConfigurationGlobals c = new ConfigurationGlobals();
        c.setConfigurationValues(null);
        AConfigurationSection s = c.getConfigurationSection(DefaultSections.cleanup);
        ConfigurationGlobalsCleanup cleanup = (ConfigurationGlobalsCleanup) s;

        GlobalSettingsSection gss = cleanup.toDefaults(3);
        logSection(gss);
    }

    @Ignore
    @Test
    public void testConfigurationGlobals() throws Exception {

        ConfigurationGlobals c = new ConfigurationGlobals();
        c.setConfigurationValues(null);
        GlobalSettings defaults = c.getDefaults();
        GlobalSettings clonedDefaults = c.getClonedDefaults();

        LOGGER.info("defaults=" + SOSString.toString(defaults.getAdditionalProperties().get("cleanup")));
        LOGGER.info("clonedDefaults=" + SOSString.toString(clonedDefaults.getAdditionalProperties().get("cleanup")));
    }

    @Ignore
    @Test
    public void testReadAndSetConfiguration() {
        Path file = Paths.get("src/test/resources/configuration/joc_configurations_globals.json");

        ConfigurationGlobals configurations = new ConfigurationGlobals();
        try {
            GlobalSettings settings = MAPPER.readValue(SOSPath.readFile(file), GlobalSettings.class);
            configurations.setConfigurationValues(settings);
            AConfigurationSection s = configurations.getConfigurationSection(DefaultSections.cleanup);
            ConfigurationGlobalsCleanup cleanup = (ConfigurationGlobalsCleanup) s;

            LOGGER.info("cleanup=" + SOSString.toString(cleanup));
            LOGGER.info("getForceCleanup=" + SOSString.toString(cleanup.getForceCleanup()));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        }
    }

    private void logSection(GlobalSettingsSection section) {
        if (section == null || section.getAdditionalProperties() == null || section.getAdditionalProperties().size() == 0) {
            LOGGER.info("[skip]because empty");
            return;
        }
        section.getAdditionalProperties().entrySet().forEach(e -> {
            logEntry(e.getKey(), e.getValue(), "");
        });
    }

    private void logEntry(String name, GlobalSettingsSectionEntry en, String indent) {
        LOGGER.info(indent + name);

        indent = indent + "   ";
        LOGGER.info(indent + "value=" + en.getValue() + ", default=" + en.getDefault());
        GlobalSettingsSectionEntryChildren children = en.getChildren();
        if (children != null) {
            LOGGER.info(indent + "children:");
            indent = indent + "   ";
            for (Map.Entry<String, GlobalSettingsSectionEntry> c : children.getAdditionalProperties().entrySet()) {
                logEntry(c.getKey(), c.getValue(), indent);
            }
        }
    }
}
