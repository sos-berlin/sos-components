package com.sos.yade.engine.delegators;

import java.util.Optional;

import com.sos.commons.util.SOSPathUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.vfs.common.AProviderContext;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.yade.engine.arguments.YADETargetArguments;
import com.sos.yade.engine.handlers.operations.YADECopyOrMoveOperationTargetFilesConfig;

public class YADETargetProviderDelegator extends AYADEProviderDelegator {

    private final static String LOG_PREFIX = "[Target]";

    public YADETargetProviderDelegator(IProvider provider, YADETargetArguments args) {
        super(provider, args);

        // set YADE specific ProviderContext
        provider.setContext(new AProviderContext() {

            @Override
            public String getLogPrefix() {
                return LOG_PREFIX;
            }
        });
    }

    public YADEProviderFile newYADETargetProviderFile(YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile,
            YADECopyOrMoveOperationTargetFilesConfig config) {
        if (config.getCumulate() != null) {
            return null;
        }
        /** finalFileName: the final name of the file after transfer */
        String finalFileName = getFinalFileName(sourceFile, config);

        /** file during transfer with the atomic or final file name */
        String transferFileFullPath;
        boolean setNewName = false;
        if (config.getAtomic() == null) {
            transferFileFullPath = getTargetFileFullPath(config, sourceFile, finalFileName);
        } else {
            transferFileFullPath = getTargetFileFullPath(config, sourceFile, config.getAtomic().getPrefix() + finalFileName + config.getAtomic()
                    .getSuffix());
            setNewName = true;
        }
        YADEProviderFile file = new YADEProviderFile(transferFileFullPath, 0, 0, false);
        if (setNewName) {
            /** the final name of the file after transfer */
            file.setFinalName(finalFileName);
        }
        return file;
    }

    /** Returns the final name of the file after transfer
     * 
     * @param sourceFile
     * @param config
     * @return the final name of the file after transfer */
    private String getFinalFileName(YADEProviderFile sourceFile, YADECopyOrMoveOperationTargetFilesConfig config) {
        // 1) Source name
        String fileName = sourceFile.getName();
        // 2) Compressed name
        if (config.getCompress() != null) {
            fileName = fileName + config.getCompress().getFileExtension();
        }
        // 3) Replaced name
        if (config.isReplacingEnabled()) {
            Optional<String> newFileName = sourceFile.getNewFileNameIfDifferent(getArgs());
            if (newFileName.isPresent()) {
                fileName = newFileName.get();
            }
        }
        return fileName;
    }

    private String getTargetFileFullPath(YADECopyOrMoveOperationTargetFilesConfig config, YADEProviderFile sourceFile, String targetFileName) {
        // YADE-600 + YADE-619(makeDirs)
        if (config.getSource().isRecursiveSelection() && config.createDirectories()) {
            ProviderDirectoryPath sourceDirectory = config.getSource().getDirectory();

            String sourceFileParent = SOSPathUtil.getParentPath(sourceFile.getFullPath());

            String sourceRelativePath;
            if (sourceDirectory == null || !sourceFile.getFullPath().startsWith(sourceDirectory.getPathWithTrailingSeparator())) {
                if (SOSPathUtil.isAbsoluteWindowsPath(sourceFile.getFullPath())) {
                    int win = sourceFile.getFullPath().indexOf(":");
                    if (win > -1) {
                        sourceRelativePath = sourceFile.getFullPath().substring(win + 1);
                        sourceRelativePath = SOSString.trimStart(sourceFileParent, config.getSource().getPathSeparatot());
                    } else {
                        if (sourceFile.getFullPath().startsWith("\\")) {
                            // ...
                        }
                    }
                }
            }
        }

        return targetFileName;
    }

    @Override
    public YADETargetArguments getArgs() {
        return (YADETargetArguments) super.getArgs();
    }

    @Override
    public String getLogPrefix() {
        return LOG_PREFIX;
    }

}
