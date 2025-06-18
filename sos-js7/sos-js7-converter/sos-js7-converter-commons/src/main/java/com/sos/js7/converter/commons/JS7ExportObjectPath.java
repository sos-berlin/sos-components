package com.sos.js7.converter.commons;

import java.nio.file.Path;

public class JS7ExportObjectPath {

    private final Path path;
    private final String name;

    protected JS7ExportObjectPath(Path path) {
        this.path = JS7ConverterResult.normalize(path);
        this.name = JS7ExportObjects.getName(path);
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }
}
