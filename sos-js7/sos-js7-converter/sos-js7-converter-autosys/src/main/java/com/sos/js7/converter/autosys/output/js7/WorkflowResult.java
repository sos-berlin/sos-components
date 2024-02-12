package com.sos.js7.converter.autosys.output.js7;

import java.nio.file.Path;

public class WorkflowResult {

    private Path path;
    private String name;

    public WorkflowResult() {
    }

    public void setPath(Path val) {
        path = val;
    }

    public void setName(String val) {
        name = val;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

}
