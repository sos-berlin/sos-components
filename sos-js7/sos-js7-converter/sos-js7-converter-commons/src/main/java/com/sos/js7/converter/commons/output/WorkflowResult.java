package com.sos.js7.converter.commons.output;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class WorkflowResult {

    private Path path;
    private String name;

    private List<String> notConvertedJobs;
    private List<String> convertedJobs;

    public void set(Path path, String name) {
        this.path = path;
        this.name = name;
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name;
    }

    public List<String> getConvertedJobs() {
        return convertedJobs;
    }

    public void setConvertedJobs(List<String> val) {
        convertedJobs = new ArrayList<>(val);
    }

    public List<String> getNotConvertedJobs() {
        return notConvertedJobs;
    }

    public void setNotConvertedJobs(List<String> val) {
        notConvertedJobs = new ArrayList<>(val);
    }
}
