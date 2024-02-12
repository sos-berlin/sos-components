package com.sos.js7.converter.autosys.config.items;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;
import com.sos.js7.converter.commons.config.items.AConfigItem;

// TODO
public class AutosysGenerateConfig extends AConfigItem {

    private static final String CONFIG_KEY = "autosys.generate";

    private Set<String> applications;

    public AutosysGenerateConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key) {
        case "applications":
            withApplications(val);
            break;

        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public AutosysGenerateConfig withApplications(String val) {
        if (!SOSString.isEmpty(val)) {
            applications = Arrays.asList(val.split(",")).stream().map(e -> e.trim()).filter(e -> e.length() > 0).collect(Collectors.toSet());
        }
        return this;
    }

}
