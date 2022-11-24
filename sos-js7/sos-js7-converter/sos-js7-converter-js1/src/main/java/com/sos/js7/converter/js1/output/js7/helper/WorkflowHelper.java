package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;

public class WorkflowHelper {

    private final String name;
    private final Path path;

    public WorkflowHelper(String name, Path path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

}
