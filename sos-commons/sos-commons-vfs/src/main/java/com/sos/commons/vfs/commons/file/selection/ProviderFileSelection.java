package com.sos.commons.vfs.commons.file.selection;

import java.util.function.Function;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.AProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;

public class ProviderFileSelection {

    private ProviderFileSelectionConfig config;
    private ProviderFileSelectionResult result;

    /** Checks the allowed file type - regular, symbolic link. If not set - all file types allowed */
    private Function<Object, Boolean> fileTypeChecker;

    public static ProviderFileSelection createIfNull(ISOSLogger logger, ProviderFileSelection selection) {
        return selection == null ? new ProviderFileSelection(new ProviderFileSelectionConfig.Builder().build(logger)) : selection;
    }

    public ProviderFileSelection(ProviderFileSelectionConfig config) {
        this.config = config;
        this.result = new ProviderFileSelectionResult();
    }

    public boolean maxFilesExceeded(int currentSize) {
        return config.isFilterByMaxFilesEnabled() && currentSize >= config.getMaxFiles();
    }

    // TODO pattern and SOSPathUtil.toUnixStylePath ???
    public boolean checkDirectory(String directoryPath) {
        if (config.getExcludedDirectoriesPattern() != null) {
            // if (config.getExcludedDirectoriesPattern().matcher(SOSPathUtil.toUnixStyle(filePath)).find()) {
            if (config.getExcludedDirectoriesPattern().matcher(directoryPath).find()) {
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

    public boolean checkProviderFile(AProvider<?, ?> provider, ProviderFile file) {
        if (!checkProviderFileMinAge(provider, file)) {
            return false;
        }
        if (!checkProviderFileMaxAge(provider, file)) {
            return false;
        }
        if (!checkProviderFileMinSize(provider, file)) {
            return false;
        }
        if (!checkProviderFileMaxSize(provider, file)) {
            return false;
        }
        return true;
    }

    public boolean isValidFileType(Object fileRepresentator) {
        if (fileTypeChecker == null) {
            return true;
        }
        return fileTypeChecker.apply(fileRepresentator);
    }

    public void setFileTypeChecker(Function<Object, Boolean> val) {
        fileTypeChecker = val;
    }

    public ProviderFileSelectionConfig getConfig() {
        return config;
    }

    public ProviderFileSelectionResult getResult() {
        return result;
    }

    private boolean checkProviderFileMinAge(AProvider<?, ?> provider, ProviderFile file) {
        Long fileAge = config.getMinFileAge();
        if (fileAge != null && file.getLastModifiedMillis() > fileAge) {
            String msg = String.format("%s[Selection][skip][%s][UTC]lastModified=%s > MinFileAge=%s", provider.getLogPrefix(), file.getFullPath(),
                    file.getLastModifiedAsUTCString(), config.getFileAgeAsUTCString(fileAge));
            if (config.isPolling()) {
                config.getLogger().debug(msg);
            } else {
                config.getLogger().info(msg);
            }
            return false;
        }
        return true;
    }

    private boolean checkProviderFileMaxAge(AProvider<?, ?> provider, ProviderFile file) {
        Long fileAge = config.getMaxFileAge();
        if (fileAge != null && file.getLastModifiedMillis() < fileAge) {
            String msg = String.format("%s[Selection][skip][%s][UTC]lastModified=%s < MaxFileAge=%s", provider.getLogPrefix(), file.getFullPath(),
                    file.getLastModifiedAsUTCString(), config.getFileAgeAsUTCString(fileAge));
            if (config.isPolling()) {
                config.getLogger().debug(msg);
            } else {
                config.getLogger().info(msg);
            }
            return false;
        }
        return true;
    }

    private boolean checkProviderFileMinSize(AProvider<?, ?> provider, ProviderFile file) {
        Long fileSize = config.getMinFileSize();
        if (fileSize != null && file.getSize() < fileSize) {
            String msg = String.format("%s[Selection][skip][%s]fileSize=%s (%s Bytes) < MinFileSize=%s (%s Bytes)", provider.getLogPrefix(), file
                    .getFullPath(), SOSShell.formatBytes(file.getSize()), file.getSize(), config.getMinFileSizeConfigured(), fileSize);
            if (config.isPolling()) {
                config.getLogger().debug(msg);
            } else {
                config.getLogger().info(msg);
            }
            return false;
        }
        return true;
    }

    private boolean checkProviderFileMaxSize(AProvider<?, ?> provider, ProviderFile file) {
        Long fileSize = config.getMaxFileSize();
        if (fileSize != null && file.getSize() > fileSize) {
            String msg = String.format("%s[Selection][skip][%s]fileSize=%s (%s Bytes) > MaxFileSize=%s (%s Bytes)", provider.getLogPrefix(), file
                    .getFullPath(), SOSShell.formatBytes(file.getSize()), file.getSize(), config.getMaxFileSizeConfigured(), fileSize);
            if (config.isPolling()) {
                config.getLogger().debug(msg);
            } else {
                config.getLogger().info(msg);
            }
            return false;
        }
        return true;
    }

}
