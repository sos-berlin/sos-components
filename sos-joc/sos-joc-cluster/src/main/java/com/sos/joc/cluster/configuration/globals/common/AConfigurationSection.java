package com.sos.joc.cluster.configuration.globals.common;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntry;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionEntryChildren;
import com.sos.joc.model.configuration.globals.GlobalSettingsSectionValueType;

public abstract class AConfigurationSection {

    private static final Logger LOGGER = LoggerFactory.getLogger(AConfigurationSection.class);

    public GlobalSettingsSection toDefaults(int ordering) {
        GlobalSettingsSection s = new GlobalSettingsSection();
        s.setOrdering(ordering);

        List<Field> fields = Arrays.stream(this.getClass().getDeclaredFields()).filter(f -> f.getType().equals(ConfigurationEntry.class)).collect(
                Collectors.toList());

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                ConfigurationEntry ce = (ConfigurationEntry) field.get(this);
                if (ce != null) {
                    GlobalSettingsSectionEntry se = new GlobalSettingsSectionEntry();
                    se.setType(ce.getType());
                    se.setDefault(ce.getDefault());
                    se.setValues(ce.getValues());
                    se.setOrdering(ce.getOrdering());
                    se = toDefaultsSetChildren(ce, se);
                    s.setAdditionalProperty(ce.getName(), se);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", this.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
        return s;
    }

    private GlobalSettingsSectionEntry toDefaultsSetChildren(ConfigurationEntry ce, GlobalSettingsSectionEntry se) {
        if (ce.hasChildren()) {
            GlobalSettingsSectionEntryChildren seChildren = new GlobalSettingsSectionEntryChildren();
            for (ConfigurationEntry ceChild : ce.getChildren()) {
                GlobalSettingsSectionEntry seChild = new GlobalSettingsSectionEntry();
                seChild.setType(ceChild.getType());
                seChild.setDefault(ceChild.getDefault());
                seChild.setValues(ceChild.getValues());
                seChild.setOrdering(ceChild.getOrdering());

                if (ceChild.hasChildren()) {
                    seChild = toDefaultsSetChildren(ceChild, seChild);
                }
                seChildren.setAdditionalProperty(ceChild.getName(), seChild);
            }
            se.setChildren(seChildren);
        } else {
            se.setChildren(null);
        }
        return se;
    }

    public void setValues(GlobalSettingsSection section) {
        List<Field> fields = Arrays.stream(this.getClass().getDeclaredFields()).filter(f -> f.getType().equals(ConfigurationEntry.class)).collect(
                Collectors.toList());

        for (Field field : fields) {
            try {
                field.setAccessible(true);
                ConfigurationEntry ce = (ConfigurationEntry) field.get(this);
                if (ce != null) {
                    if (section == null || section.getAdditionalProperties() == null || !section.getAdditionalProperties().containsKey(ce
                            .getName())) {
                        ce.setValue(ce.getDefault());
                        if (ce.hasChildren()) {
                            ce = setValuesSetChildren(ce, null);
                        }
                        field.set(this, ce);
                        continue;
                    }
                    GlobalSettingsSectionEntry entry = section.getAdditionalProperties().get(ce.getName());
                    ce.setValue(setValuesGetValue(ce, entry));

                    if (ce.hasChildren()) {
                        ce = setValuesSetChildren(ce, entry);
                    }
                    field.set(this, ce);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", this.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
    }

    private ConfigurationEntry setValuesSetChildren(ConfigurationEntry ce, GlobalSettingsSectionEntry entry) {
        if (ce.hasChildren()) {
            for (ConfigurationEntry cecc : ce.getChildren()) {
                GlobalSettingsSectionEntry entryChild = setValuesGetSettingsSectionEntryChildEntry(entry, cecc.getName());
                if (entryChild == null) {
                    cecc.setValue(cecc.getDefault());
                } else {
                    cecc.setValue(setValuesGetValue(cecc, entryChild));
                }
                if (cecc.hasChildren()) {
                    cecc = setValuesSetChildren(cecc, entryChild);
                }
            }
        }
        return ce;
    }

    private GlobalSettingsSectionEntry setValuesGetSettingsSectionEntryChildEntry(GlobalSettingsSectionEntry entry, String childName) {
        if (entry == null || entry.getChildren() == null || entry.getChildren().getAdditionalProperties() == null) {
            return null;
        }
        return entry.getChildren().getAdditionalProperties().get(childName);
    }

    private String setValuesGetValue(ConfigurationEntry ce, GlobalSettingsSectionEntry entry) {
        String val = entry.getValue() == null ? null : entry.getValue().trim();
        if (GlobalSettingsSectionValueType.PASSWORD.equals(ce.getType())) {
            return val == null ? ce.getDefault() : val;  // allow empty strings
        } else {
            return SOSString.isEmpty(val) ? ce.getDefault() : val;
        }
    }

}
