package com.sos.commons.vfs.exception;

public class SOSNoSuchFileException extends SOSProviderException {

    private static final long serialVersionUID = 1L;

    private final String path;

    public SOSNoSuchFileException(String path) {
        super(path);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
