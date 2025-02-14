package com.sos.commons.vfs.common.file.files;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public abstract class AFileResult {

    private final int totalFiles;
    private int processedFiles = 0;
    private final Set<String> notFound = new LinkedHashSet<>();
    private final Map<String, Throwable> errors = new LinkedHashMap<>();

    public AFileResult(int totalFiles) {
        this.totalFiles = totalFiles;
    }

    public void addSuccess() {
        processedFiles++;
    }

    public void addNotFound(String path) {
        notFound.add(path);
    }

    public void addError(String path, Throwable error) {
        errors.put(path, error);
    }

    public boolean allProcessed() {
        return errors.isEmpty() && (processedFiles + notFound.size()) == totalFiles;
    }

    public Map<String, Throwable> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public int getTotalFiles() {
        return totalFiles;
    }

    public int getProcessedFiles() {
        return processedFiles;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("totalFiles=").append(totalFiles);
        sb.append(",processedFiles=").append(processedFiles);
        sb.append(",notFound=").append(notFound);
        if (hasErrors()) {
            sb.append(",errors=").append(errors);
        }
        return sb.toString();
    }

}
