package com.sos.commons.vfs.common.file.files;

import java.util.LinkedHashMap;
import java.util.Map;

public class RenameFilesResult extends AFileResult {

    private final Map<String, String> renamedFiles = new LinkedHashMap<>();

    public RenameFilesResult(int totalFiles) {
        super(totalFiles);
    }

    public void addSuccess(String oldPath, String newPath) {
        renamedFiles.put(oldPath, newPath);
        addSuccess();
    }

    public Map<String, String> getRenamedFiles() {
        return renamedFiles;
    }
}
