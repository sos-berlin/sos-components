package com.sos.js7.converter.autosys.config.items;

import java.util.Properties;

import com.sos.js7.converter.commons.config.items.AConfigItem;

public class AutosysInputConfig extends AConfigItem {

    private static final String CONFIG_KEY = "autosys.input";

    private boolean cleanup = true;
    private boolean exportFolders = false;

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
        case "exportFolders":
            withExportFolders(Boolean.parseBoolean(val));
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

    public AutosysInputConfig withExportFolders(boolean val) {
        this.exportFolders = val;
        return this;
    }

    public boolean getCleanup() {
        return cleanup;
    }

    public boolean getExportFolders() {
        return exportFolders;
    }

    public AutosysDiagramConfig getDiagramConfig() {
        return diagramConfig;
    }
}
