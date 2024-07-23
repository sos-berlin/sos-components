package com.sos.js7.converter.autosys.config.items;

import com.sos.js7.converter.commons.config.items.DiagramConfig;

public class AutosysDiagramConfig extends DiagramConfig {

    private static final String CONFIG_KEY = "autosys.input.diagram";

    private boolean optimizeBoxDependencies = false;

    public AutosysDiagramConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        super.parse(key, val);
        switch (key) {
        case "optimizeBoxDependencies":
            withOptimizeBoxDependencies(Boolean.parseBoolean(val));
            break;
        }
    }

    public AutosysDiagramConfig withOptimizeBoxDependencies(boolean val) {
        this.optimizeBoxDependencies = val;
        return this;
    }

    public boolean optimizeBoxDependencies() {
        return optimizeBoxDependencies;
    }
}
