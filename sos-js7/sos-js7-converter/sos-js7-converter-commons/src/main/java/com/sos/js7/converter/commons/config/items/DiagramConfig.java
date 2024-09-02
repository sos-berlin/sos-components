package com.sos.js7.converter.commons.config.items;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DiagramConfig extends AConfigItem {

    private static final String CONFIG_KEY = "diagramConfig";

    private boolean generate = false;
    private Boolean excludeStandalone;
    private int size = 0;
    private String outputFormat = "svg";

    private Path graphvizExecutable = Paths.get("C://Program Files (x86)/Graphviz2.38/bin/dot.exe");
    private boolean graphvizCleanupDotFiles = false;

    public DiagramConfig() {
        this(CONFIG_KEY);
    }

    public DiagramConfig(String configKey) {
        super(configKey);
    }

    @Override
    protected void parse(String key, String val) {
        switch (key.toLowerCase()) {
        case "generate":
            withGenerate(Boolean.parseBoolean(val));
            break;
        case "excludestandalone":
            withExcludeStandalone(Boolean.parseBoolean(val));
            break;
        case "outputformat":
            withOutputFormat(val);
            break;
        case "size":
            withSize(Integer.parseInt(val));
            break;
        case "graphviz.executable":
            withGraphvizExecutable(val);
            break;
        case "graphviz.cleanupdotfiles":
            withGraphvizCleanupDotFiles(Boolean.parseBoolean(val));
            break;
        }
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    public DiagramConfig withGenerate(boolean val) {
        this.generate = val;
        return this;
    }

    public DiagramConfig withExcludeStandalone(boolean val) {
        this.excludeStandalone = val;
        return this;
    }

    public DiagramConfig withGraphvizCleanupDotFiles(boolean val) {
        this.graphvizCleanupDotFiles = val;
        return this;
    }

    public DiagramConfig withOutputFormat(String val) {
        this.outputFormat = val;
        return this;
    }

    public DiagramConfig withSize(int val) {
        this.size = val;
        return this;
    }

    public DiagramConfig withGraphvizExecutable(String val) {
        this.graphvizExecutable = Paths.get(val);
        return this;
    }

    public boolean getGenerate() {
        return generate;
    }

    public Boolean getExcludeStandalone() {
        return excludeStandalone;
    }

    public boolean excludeStandalone() {
        return excludeStandalone != null && excludeStandalone;
    }

    public boolean getGraphvizCleanupDotFiles() {
        return graphvizCleanupDotFiles;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public int getSize() {
        return size;
    }

    public Path getGraphvizExecutable() {
        return graphvizExecutable;
    }

}
