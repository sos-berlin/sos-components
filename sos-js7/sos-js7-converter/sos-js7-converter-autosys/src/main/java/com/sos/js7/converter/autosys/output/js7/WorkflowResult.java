package com.sos.js7.converter.autosys.output.js7;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WorkflowResult {

    private Path path;
    private String name;
    private String timezone;
    private boolean isAutosysTimezone;
    private Set<String> postNotices = new HashSet<>();

    public WorkflowResult() {
    }

    public void setPath(Path val) {
        path = val;
    }

    public void setName(String val) {
        name = val;
    }

    public void setTimezone(String val, boolean isAutosysTimezone) {
        timezone = val;
        this.isAutosysTimezone = isAutosysTimezone;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public String getTimezone() {
        return timezone;
    }

    public boolean isAutosysTimezone() {
        return isAutosysTimezone;
    }

    public void addPostNotice(String val) {
        if (!postNotices.contains(val)) {
            postNotices.add(val);
        }
    }

    public void addPostNotices(List<String> l) {
        if (l == null || l.size() == 0) {
            return;
        }
        for (String val : l) {
            addPostNotice(val);
        }
    }

    public boolean hasPostNotice(String val) {
        return postNotices.contains(val);
    }

}
