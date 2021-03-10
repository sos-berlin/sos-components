package com.sos.joc.cluster.configuration;

import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.configuration.ConfigurationObjectType;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public class JocClusterGlobalSettings {

    public static final String CONTROLLER_ID = ".";
    public static final Long INSTANCE_ID = 0L;
    public static final String ACCOUNT = ".";
    public static final boolean SHARED = false;
    public static final String DEFAULT_CONFIGURATION_ITEM = "{}";
    public static final ConfigurationObjectType OBJECT_TYPE = null;

    private final static String INITIAL_CLEANUP_PERIOD = "1,2,3,4,5,6,7";
    private static GlobalSettings defaultSettings = null;

    private static GlobalSettingsSection createDefaultDailyPlanSettings() {
        GlobalSettingsSection s = new GlobalSettingsSection();
        s.setOrdering(0);
        addDefaultEntry(s, 0, "time_zone", "UTC", GlobalSettingsSectionValueType.TIMEZONE);
        addDefaultEntry(s, 1, "period_begin", "00:00", GlobalSettingsSectionValueType.TIME);
        addDefaultEntry(s, 2, "start_time", "", GlobalSettingsSectionValueType.TIME);
        addDefaultEntry(s, 3, "days_ahead_plan", "7", GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);
        addDefaultEntry(s, 4, "days_ahead_submit", "3", GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);
        return s;
    }

    private static GlobalSettingsSection createDefaultCleanupSettings() {
        GlobalSettingsSection s = new GlobalSettingsSection();
        s.setOrdering(1);
        addDefaultEntry(s, 0, "time_zone", "UTC", GlobalSettingsSectionValueType.TIMEZONE);
        addDefaultEntry(s, 1, "period", null, GlobalSettingsSectionValueType.WEEKDAYS);
        addDefaultEntry(s, 2, "period_begin", "01:00", GlobalSettingsSectionValueType.TIME);
        addDefaultEntry(s, 3, "period_end", "04:00", GlobalSettingsSectionValueType.TIME);
        addDefaultEntry(s, 4, "batch_size", "1000", GlobalSettingsSectionValueType.POSITIVEINTEGER);

        addDefaultEntry(s, 5, "order_history_age", "90d", GlobalSettingsSectionValueType.DURATION);
        addDefaultEntry(s, 6, "order_history_logs_age", "90d", GlobalSettingsSectionValueType.DURATION);
        addDefaultEntry(s, 7, "daily_plan_history_age", "30d", GlobalSettingsSectionValueType.DURATION);
        addDefaultEntry(s, 8, "audit_log_age", "90d", GlobalSettingsSectionValueType.DURATION);
        addDefaultEntry(s, 9, "deployment_history_versions", "10", GlobalSettingsSectionValueType.NONNEGATIVEINTEGER);
        return s;
    }

    public static GlobalSettings addDefaultInfos(GlobalSettings settings) {
        if (settings == null) {
            return null;
        }
        setDefaultSettings();
        settings.getAdditionalProperties().entrySet().stream().forEach(s -> {
            s.getValue().getAdditionalProperties().entrySet().stream().forEach(e -> {
                GlobalSettingsSectionEntry entry = e.getValue();

                GlobalSettingsSection defaultSection = defaultSettings.getAdditionalProperties().get(s.getKey());
                GlobalSettingsSectionEntry defaultEntry = null;
                if (defaultSection != null) {
                    defaultEntry = defaultSection.getAdditionalProperties().get(e.getKey());
                }
                if (defaultEntry == null) {
                    entry.setDefault(null);
                    entry.setType(GlobalSettingsSectionValueType.STRING);
                } else {
                    entry.setDefault(defaultEntry.getDefault());
                    entry.setType(defaultEntry.getType());
                }
            });
        });
        return settings;
    }

    public static GlobalSettings useAndRemoveDefaultInfos(GlobalSettings settings) {
        if (settings == null) {
            return null;
        }
        settings.getAdditionalProperties().entrySet().stream().forEach(s -> {
            s.getValue().getAdditionalProperties().entrySet().stream().forEach(e -> {
                GlobalSettingsSectionEntry entry = e.getValue();
                entry.setValue(entry.getDefault());
                entry.setDefault(null);
                entry.setType(null);

            });
        });
        return settings;
    }

    public static void setCleanupInitialPeriod(GlobalSettings settings) {
        try {
            settings.getAdditionalProperties().get(ClusterServices.cleanup.name()).getAdditionalProperties().get("period").setValue(
                    INITIAL_CLEANUP_PERIOD);
        } catch (Throwable e) {
        }
    }

    private static void addDefaultEntry(GlobalSettingsSection s, int ordering, String entryName, String defaultValue,
            GlobalSettingsSectionValueType valueType) {
        GlobalSettingsSectionEntry e = new GlobalSettingsSectionEntry();
        e.setOrdering(ordering);
        e.setDefault(defaultValue);
        e.setType(valueType);
        s.setAdditionalProperty(entryName, e);
    }

    private static void setDefaultSettings() {
        defaultSettings = new GlobalSettings();
        defaultSettings.setAdditionalProperty(ClusterServices.dailyplan.name(), createDefaultDailyPlanSettings());
        defaultSettings.setAdditionalProperty(ClusterServices.cleanup.name(), createDefaultCleanupSettings());
    }

    public static GlobalSettings getDefaultSettings() {
        setDefaultSettings();
        return defaultSettings;
    }

    public static GlobalSettingsSection getDefaultSettings(ClusterServices service) {
        setDefaultSettings();
        return defaultSettings == null ? null : defaultSettings.getAdditionalProperties().get(service.name());
    }

    public static GlobalSettingsSectionEntry getSectionEntry(GlobalSettingsSection section, String entryName) {
        try {
            return section.getAdditionalProperties().get(entryName);
        } catch (Throwable e) {
            return null;
        }
    }

    public static String getValue(GlobalSettingsSection section, String entryName) {
        try {
            return section.getAdditionalProperties().get(entryName).getValue();
        } catch (Throwable e) {
            return null;
        }
    }

}
