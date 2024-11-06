package com.sos.js7.converter.commons.config.items;

import com.sos.commons.util.SOSString;

public class BoardConfig extends AConfigItem {

    private static final String CONFIG_KEY = "boardConfig";

    private String forcedLifetime;
    private String defaultLifetime = "24 * 60"; // minutes

    public BoardConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        // FORCED
        case "forced.lifetime":
            withForcedLifetime(val);
            break;
        case "default.lifetime":
            withDefaultLifetime(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return forcedLifetime == null && defaultLifetime == null;
    }

    public BoardConfig withForcedLifetime(String val) {
        this.forcedLifetime = parseLifetime(val);
        return this;
    }

    public BoardConfig withDefaultLifetime(String val) {
        this.defaultLifetime = parseLifetime(val);
        return this;
    }

    public String getForcedLifetime() {
        return forcedLifetime;
    }

    public String getDefaultLifetime() {
        return defaultLifetime;
    }

    private String parseLifetime(String val) {
        if (SOSString.isEmpty(val)) {
            return val;
        }
        String v = val.toLowerCase();
        if (v.endsWith("d")) {
            v = SOSString.trim(v, "d").trim();
            v = v + " * 24 * 60";
        }
        return v;
    }

}
