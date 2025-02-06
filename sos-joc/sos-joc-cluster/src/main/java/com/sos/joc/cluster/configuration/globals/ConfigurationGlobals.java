package com.sos.joc.cluster.configuration.globals;

import java.util.HashMap;
import java.util.Map;

import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.model.configuration.ConfigurationObjectType;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntryChildren;

public class ConfigurationGlobals {

    public static final String CONTROLLER_ID = ".";
    public static final Long INSTANCE_ID = 0L;
    public static final String ACCOUNT = ".";
    public static final boolean SHARED = false;
    public static final String DEFAULT_CONFIGURATION_ITEM = "{}";
    public static final ConfigurationObjectType OBJECT_TYPE = null;

    public enum DefaultSections {
       dailyplan, cleanup, joc, user, git, lognotification, identityService, kiosk
    }

    private GlobalSettings defaults = null;
    private Map<DefaultSections, AConfigurationSection> sections = null;

    public ConfigurationGlobals() {
        defaults = new GlobalSettings();
        addDefaultSection(DefaultSections.dailyplan, 0);
        addDefaultSection(DefaultSections.cleanup, 1);
        addDefaultSection(DefaultSections.identityService, 2);
        addDefaultSection(DefaultSections.lognotification, 3);
        addDefaultSection(DefaultSections.joc, 4);
        addDefaultSection(DefaultSections.git, 5);
        addDefaultSection(DefaultSections.kiosk, 6);
        addDefaultSection(DefaultSections.user, 7);
    }

    public void setConfigurationValues(GlobalSettings values) {
        sections = new HashMap<>();
        defaults.getAdditionalProperties().entrySet().stream().forEach(s -> {
            DefaultSections ds = DefaultSections.valueOf(s.getKey());
            AConfigurationSection inst = getClassInstance(ds);
            if (values != null && values.getAdditionalProperties().containsKey(s.getKey())) {
                inst.setValues(values.getAdditionalProperties().get(s.getKey()));
            } else {
                inst.setValues(null);
            }
            sections.put(ds, inst);
        });
    }

    public AConfigurationSection getConfigurationSection(DefaultSections section) {
        if (sections == null) {
            setConfigurationValues(null);
        }
        return sections.get(section);
    }

    public GlobalSettings getInitialSettings(String timeZone) {
        GlobalSettings settings = clone(defaults);

        settings.getAdditionalProperties().entrySet().stream().forEach(s -> {
            s.getValue().getAdditionalProperties().entrySet().stream().forEach(e -> {
                GlobalSettingsSectionEntry entry = e.getValue();
                if (s.getKey().equals(DefaultSections.cleanup.name()) && e.getKey().equals(ConfigurationGlobalsCleanup.ENTRY_NAME_PERIOD)) {
                    entry.setValue(ConfigurationGlobalsCleanup.INITIAL_PERIOD);
                } else if (e.getKey().equals("time_zone")) {
                    entry.setValue(timeZone);
                } else {
                    entry.setValue(entry.getDefault());
                }
                entry.setDefault(null);
                entry.setType(null);
                entry.setValues(null);
            });
        });

        return settings;
    }

    public GlobalSettings getDefaults() {
        return defaults;
    }

    public GlobalSettings getClonedDefaults() {
        return clone(defaults);
    }

    private void addDefaultSection(DefaultSections section, int ordering) {
        defaults.setAdditionalProperty(section.name(), getClassInstance(section).toDefaults(ordering));
    }

    private AConfigurationSection getClassInstance(DefaultSections section) {
        if (section == null) {
            return null;
        }
        switch (section) {
        case dailyplan:
            return new ConfigurationGlobalsDailyPlan();
        case cleanup:
            return new ConfigurationGlobalsCleanup();
        case joc:
            return new ConfigurationGlobalsJoc();
        case user:
            return new ConfigurationGlobalsUser();
        case git:
            return new ConfigurationGlobalsGit();
        case lognotification:
            return new ConfigurationGlobalsLogNotification();
        case identityService:
            return new ConfigurationGlobalsIdentityService();
        case kiosk:
            return new ConfigurationGlobalsKiosk();
        default:
            return null;
        }
    }

    private GlobalSettings clone(GlobalSettings settings) {
        if (settings == null) {
            return null;
        }
        GlobalSettings s = new GlobalSettings();
        settings.getAdditionalProperties().entrySet().stream().forEach(os -> {
            GlobalSettingsSection ns = new GlobalSettingsSection();
            ns.setOrdering(os.getValue().getOrdering());

            os.getValue().getAdditionalProperties().entrySet().stream().forEach(e -> {
                GlobalSettingsSectionEntry oe = e.getValue();
                GlobalSettingsSectionEntry ne = new GlobalSettingsSectionEntry();

                ne.setDefault(oe.getDefault());
                ne.setOrdering(oe.getOrdering());
                ne.setType(oe.getType());
                ne.setValue(oe.getValue());
                ne.setValues(oe.getValues());
                ne = cloneChildren(oe, ne);

                ns.getAdditionalProperties().put(e.getKey(), ne);
            });

            s.getAdditionalProperties().put(os.getKey(), ns);
        });

        return s;
    }

    private GlobalSettingsSectionEntry cloneChildren(GlobalSettingsSectionEntry source, GlobalSettingsSectionEntry target) {
        if (source.getChildren() != null && source.getChildren().getAdditionalProperties() != null && source.getChildren().getAdditionalProperties()
                .size() > 0) {
            GlobalSettingsSectionEntryChildren children = new GlobalSettingsSectionEntryChildren();
            source.getChildren().getAdditionalProperties().entrySet().stream().forEach(e -> {
                GlobalSettingsSectionEntry oe = e.getValue();
                GlobalSettingsSectionEntry ne = new GlobalSettingsSectionEntry();

                ne.setDefault(oe.getDefault());
                ne.setOrdering(oe.getOrdering());
                ne.setType(oe.getType());
                ne.setValue(oe.getValue());
                ne.setValues(oe.getValues());
                ne = cloneChildren(oe, ne);

                children.getAdditionalProperties().put(e.getKey(), ne);
            });
            target.setChildren(children);
        } else {
            target.setChildren(null);
        }
        return target;
    }

}
