package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;

public class ProcessClassFirstUsageHelper {

    private final Path path;
    private final String js7AgentName;
    private final String message;

    public ProcessClassFirstUsageHelper(Path path, String js7AgentName, String message) {
        this.path = path;
        this.js7AgentName = js7AgentName;
        this.message = message;
    }

    public Path getPath() {
        return path;
    }

    public String getJs7AgentName() {
        return js7AgentName;
    }

    public String getMessage() {
        return message;
    }

}
