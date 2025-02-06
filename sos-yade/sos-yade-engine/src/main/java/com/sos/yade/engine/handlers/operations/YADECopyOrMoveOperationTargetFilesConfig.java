package com.sos.yade.engine.handlers.operations;

import com.sos.yade.engine.delegators.YADETargetProviderDelegator;

public class YADECopyOrMoveOperationTargetFilesConfig {

    private boolean cumulate;
    private boolean deleteCumulativeFile;
    private String cumulativeFileFullPath;

    private boolean compress;
    private String compressFileExtension;

    public YADECopyOrMoveOperationTargetFilesConfig(final YADETargetProviderDelegator targetDelegator) {
        if (!targetDelegator.getArgs().getCumulativeFileName().isEmpty()) {
            cumulate = true;
            deleteCumulativeFile = targetDelegator.getArgs().getCumulativeFileDelete().isTrue();
            cumulativeFileFullPath = targetDelegator.normalizePath(targetDelegator.getArgs().getCumulativeFileName().getValue());
            if (!targetDelegator.getProvider().isAbsolutePath(cumulativeFileFullPath)) {
                if (targetDelegator.getDirectory() != null) {
                    cumulativeFileFullPath = targetDelegator.getDirectory().getPathWithTrailingSeparator() + cumulativeFileFullPath;
                }
            }
        }
        if (!targetDelegator.getArgs().getCompressedFileExtension().isEmpty()) {
            compress = true;
            compressFileExtension = getFileExtension(targetDelegator.getArgs().getCompressedFileExtension().getValue());
        }

        // ?? YADE1 not use compressFileExtension if cumulate
        // if (cumulate && compress) {
        // cumulativeFileFullPath = cumulativeFileFullPath + compressFileExtension;
        // }
    }

    public boolean cumulate() {
        return cumulate;
    }

    public boolean compress() {
        return compress;
    }

    public boolean deleteCumulativeFile() {
        return deleteCumulativeFile;
    }

    public String getCumulativeFileFullPath() {
        return cumulativeFileFullPath;
    }

    public String getCompressFileExtension() {
        return compressFileExtension;
    }

    private String getFileExtension(String extension) {
        return extension.startsWith(".") ? extension : "." + extension;
    }
}
