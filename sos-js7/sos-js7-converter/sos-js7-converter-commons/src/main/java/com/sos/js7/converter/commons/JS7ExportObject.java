package com.sos.js7.converter.commons;

import java.nio.file.Path;

public class JS7ExportObject<T> {

    private final T object;
    private final boolean reference;
    private final JS7ExportObjectPath originalPath;
    private final JS7ExportObjectPath uniquePath;

    protected JS7ExportObject(T object, boolean reference, Path originalPath, Path uniquePath) {
        this.object = object;
        this.reference = reference;
        this.originalPath = new JS7ExportObjectPath(originalPath);
        this.uniquePath = new JS7ExportObjectPath(uniquePath);
    }

    public T getObject() {
        return object;
    }

    public boolean isReference() {
        return reference;
    }

    public JS7ExportObjectPath getOriginalPath() {
        return originalPath;
    }

    public JS7ExportObjectPath getUniquePath() {
        return uniquePath;
    }
}
