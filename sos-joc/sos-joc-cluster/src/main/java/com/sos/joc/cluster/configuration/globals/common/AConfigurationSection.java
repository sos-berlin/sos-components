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
                    se.setOrdering(ce.getOrdering());
                    s.setAdditionalProperty(ce.getName(), se);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", this.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
        return s;
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
                        field.set(this, ce);
                        continue;
                    }
                    GlobalSettingsSectionEntry entry = section.getAdditionalProperties().get(ce.getName());
                    String val = entry.getValue() == null ? null : entry.getValue().trim();
                    if (GlobalSettingsSectionValueType.PASSWORD.equals(ce.getType())) {
                        LOGGER.info(String.format("[%s.setValues]%s='%s'", this.getClass().getSimpleName(), ce.getName(), entry.getValue()));
                        ce.setValue(val == null ? ce.getDefault() : val);  //allow empty strings
                    } else {
                        ce.setValue(SOSString.isEmpty(val) ? ce.getDefault() : val);
                    }
                    field.set(this, ce);
                }
            } catch (Throwable e) {
                LOGGER.error(String.format("[can't get field][%s.%s]%s", this.getClass().getSimpleName(), field.getName(), e.toString()), e);
            }
        }
    }
}
