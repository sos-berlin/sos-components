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
        return config.isFilterByMaxFilesEnabled() && currentSize >= config.getMaxFiles();
    }

    // TODO pattern and SOSPathUtil.toUnixStylePath ???
    public boolean checkDirectory(String filePath) {
        if (config.getExcludedDirectoriesPattern() != null) {
            if (config.getExcludedDirectoriesPattern().matcher(SOSPathUtil.toUnixPath(filePath)).find()) {
                return false;
            }
        }
        return true;
    }

    public boolean checkFileName(String fileName) {
        if (config.getExcludedFileExtension() != null && fileName.endsWith(config.getExcludedFileExtension())) {
            return false;
        }
        if (config.getFileNamePattern() != null && !config.getFileNamePattern().matcher(fileName).find()) {
            return false;
        }
        return true;
    }

    public boolean checkProviderFileMinMaxSize(ProviderFile file) {
        if (!checkProviderFileMaxSize(file)) {
            return false;
        }
        if (!checkProviderFileMinSize(file)) {
            return false;
        }
        return true;
    }

    public boolean checkProviderFileMaxSize(ProviderFile file) {
        if (config.isFilterByMaxFileSizeEnabled() && file.getSize() > config.getMaxFileSize()) {
            return false;
        }
        return true;
    }

    public boolean checkProviderFileMinSize(ProviderFile file) {
        if (config.isFilterByMinFileSizeEnabled() && file.getSize() < config.getMinFileSize()) {
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
