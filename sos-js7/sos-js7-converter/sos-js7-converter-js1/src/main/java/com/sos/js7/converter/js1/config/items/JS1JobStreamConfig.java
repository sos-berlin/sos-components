package com.sos.js7.converter.js1.config.items;

import com.sos.js7.converter.commons.config.items.AConfigItem;

public class JS1JobStreamConfig extends AConfigItem {

    private static final String CONFIG_KEY = "js1.jobStream";

    private boolean generateJobFileIfNotExists = false;

    public JS1JobStreamConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key) {
        case "generateJobFileIfNotExists":
            withGenerateJobFileIfNotExists(Boolean.parseBoolean(val));
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public JS1JobStreamConfig withGenerateJobFileIfNotExists(boolean val) {
        this.generateJobFileIfNotExists = val;
        return this;
    }

    public boolean getGenerateJobFileIfNotExists() {
        return generateJobFileIfNotExists;
    }

}
