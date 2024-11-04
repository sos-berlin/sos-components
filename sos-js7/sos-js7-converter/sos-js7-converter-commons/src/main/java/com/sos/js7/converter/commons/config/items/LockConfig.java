package com.sos.js7.converter.commons.config.items;

import com.sos.js7.converter.commons.JS7ConverterHelper;

public class LockConfig extends AConfigItem {

    private static final String CONFIG_KEY = "lockConfig";

    private static final int DEFAULT_CAPACITY = 1;

    private Integer defaultCapacity;
    private Integer forcedCapacity;

    public LockConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        case "default.capacity":
            withDefaultCapacity(val);
            break;
        case "forced.capacity":
            withForcedCapacity(val);
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return defaultCapacity == null && forcedCapacity == null;
    }

    public LockConfig withDefaultCapacity(String val) {
        this.defaultCapacity = JS7ConverterHelper.integerValue(val);
        return this;
    }

    public LockConfig withForcedCapacity(String val) {
        this.forcedCapacity = JS7ConverterHelper.integerValue(val);
        return this;
    }

    public Integer getDefaultCapacity() {
        if (defaultCapacity == null) {
            defaultCapacity = DEFAULT_CAPACITY;
        }
        return defaultCapacity;
    }

    public Integer getForcedCapacity() {
        return forcedCapacity;
    }

}
