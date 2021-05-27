package com.sos.commons.exception;

import java.nio.file.Path;

public class SOSNoSuchFileException extends SOSException {

    private static final long serialVersionUID = 1L;

    private final String path;

    public SOSNoSuchFileException(Path path) {
        super(path == null ? "null" : path.toString());
        this.path = path == null ? null : path.toString();
    }

    public SOSNoSuchFileException(String path) {
        super(path == null ? "null" : path);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
