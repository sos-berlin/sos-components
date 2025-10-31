package com.sos.js7.converter.commons;

import java.nio.file.Path;

import com.sos.commons.util.SOSPathUtils;

public class JS7ExportObjectPath {

    private final Path path;
    private final String name;
    private final String baseName;

    protected JS7ExportObjectPath(Path path) {
        this.path = JS7ConverterResult.normalize(path);
        this.name = JS7ExportObjects.getName(path);
        this.baseName = SOSPathUtils.getBaseName(name);
    }

    public Path getPath() {
        return path;
    }

    /** @return e.g. my_workflow.workflow, my_schedule.schedule */
    public String getName() {
        return name;
    }

    /** @return e.g. my_workflow, my_schedule */
    public String getBaseName() {
        return baseName;
    }
}
