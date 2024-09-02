package com.sos.js7.converter.autosys.config.items;

import com.sos.js7.converter.commons.config.items.DiagramConfig;

public class AutosysDiagramConfig extends DiagramConfig {

    private static final String CONFIG_KEY = "autosys.input.diagram";

    private boolean optimizeDependencies = false;

    public AutosysDiagramConfig() {
        super(CONFIG_KEY);
    }

    @Override
    protected void parse(String key, String val) {
        super.parse(key, val);
        switch (key.toLowerCase()) {
        case "optimize.dependencies":
            withOptimizeDependencies(Boolean.parseBoolean(val));
            break;
        }
    }

    public AutosysDiagramConfig withOptimizeDependencies(boolean val) {
        this.optimizeDependencies = val;
        return this;
    }

    public boolean optimizeDependencies() {
        return optimizeDependencies;
    }
}
