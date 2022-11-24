package com.sos.js7.converter.js1.output.js7.helper;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class BoardHelper {

    private Map<String, Path> js7States = new HashMap<>();
    private Path workflowPath;
    private String workflowName;

    public Map<String, Path> getJS7States() {
        return js7States;
    }

    public void setJS7States(Map<String, Path> val) {
        js7States = val;
    }

    public Path getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(Path val) {
        workflowPath = val;
    }

    public String getWorkflowName() {
        return workflowName;
    }

    public void setWorkflowName(String val) {
        workflowName = val;
    }

}
