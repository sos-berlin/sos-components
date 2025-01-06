package com.sos.js7.converter.commons.config.items;

import com.sos.commons.util.SOSDate;

public class WorkflowConfig extends AConfigItem {

    private static final String CONFIG_KEY = "workflowConfig";

    private String defaultTimeZone = SOSDate.TIMEZONE_UTC;

    public WorkflowConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        case "default.timezone":
            withDefaultTimeZone(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public WorkflowConfig withDefaultTimeZone(String val) {
        this.defaultTimeZone = val;
        return this;
    }

    public String getDefaultTimeZone() {
        return defaultTimeZone;
    }
}
