package com.sos.js7.converter.autosys.config.items;

import java.util.Properties;

import com.sos.js7.converter.commons.config.items.AConfigItem;

public class AutosysInputConfig extends AConfigItem {

    private static final String CONFIG_KEY = "autosys.input";

    private boolean cleanup = true;
    private boolean splitConfiguration = false;

    private final AutosysDiagramConfig diagramConfig;

    public AutosysInputConfig() {
        super(CONFIG_KEY);
        diagramConfig = new AutosysDiagramConfig();
    }

    @Override
    public AConfigItem parse(Properties properties) {
        diagramConfig.parse(properties);
        return super.parse(properties);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key) {
        case "cleanup":
            withCleanup(Boolean.parseBoolean(val));
            break;
        case "splitConfiguration":
            withSplitConfiguration(Boolean.parseBoolean(val));
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public AutosysInputConfig withCleanup(boolean val) {
        this.cleanup = val;
        return this;
    }

    public AutosysInputConfig withSplitConfiguration(boolean val) {
        this.splitConfiguration = val;
        return this;
    }

    public boolean getCleanup() {
        return cleanup;
    }

    public boolean getSplitConfiguration() {
        return splitConfiguration;
    }

    public AutosysDiagramConfig getDiagramConfig() {
        return diagramConfig;
    }
}
