package com.sos.commons.vfs.common.file.selection;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.vfs.common.file.ProviderFile;

public class ProviderFileSelection {

    private ProviderFileSelectionConfig config;
    private ProviderFileSelectionResult result;

    public static ProviderFileSelection createIfNull(ProviderFileSelection selection) {
        return selection == null ? new ProviderFileSelection(new ProviderFileSelectionConfig.Builder().build()) : selection;
    }

    public ProviderFileSelection(ProviderFileSelectionConfig config) {
        this.config = config;
        this.result = new ProviderFileSelectionResult();
    }

    public boolean maxFilesExceeded(int currentSize) {
        return config.getMaxFiles() > 0 && currentSize >= config.getMaxFiles();
    }

    public boolean checkDirectory(String filePath) {
        if (config.getExcludedDirectoriesPattern() != null) {
            if (config.getExcludedDirectoriesPattern().matcher(SOSPathUtil.toUnixStylePath(filePath)).find()) {
                return false;
            }
        }
        return true;
    }

    public boolean checkFileName(String fileName) {
        if (config.getExcludedFileNameEnd() != null && fileName.endsWith(config.getExcludedFileNameEnd())) {
            return false;
        }
        if (config.getFileNamePattern() != null && !config.getFileNamePattern().matcher(fileName).find()) {
            return false;
        }
        return true;
    }

    public boolean checkProviderFile(ProviderFile file) {
        if (config.getMaxFileSize() > -1L && file.getSize() > config.getMaxFileSize()) {
            return false;
        }
        if (config.getMinFileSize() > -1 && file.getSize() < config.getMinFileSize()) {
            return false;
        }
        return true;
    }

    public ProviderFileSelectionConfig getConfig() {
        return config;
    }

    public ProviderFileSelectionResult getResult() {
        return result;
    }

}
