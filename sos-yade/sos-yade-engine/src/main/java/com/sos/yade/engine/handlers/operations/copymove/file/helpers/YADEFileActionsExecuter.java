package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.IYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileSizeException;
import com.sos.yade.engine.handlers.command.YADECommandExecutor;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

/** Single "transfer" file operations */
public class YADEFileActionsExecuter {

    public static void postProcessingOnSuccess(ISOSLogger logger, String fileTransferLogPrefix, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, YADEProviderFile sourceFile,
            boolean isAtomicallyEnabled, boolean useLastModified) throws Exception {

        boolean executeAfterFile = false;
        // 1) Target - individual operations
        if (config.getTarget().needsFilePostProcessing()) {
            YADETargetProviderFile targetFile = sourceFile.getTarget();
            if (targetFile != null) {
                executeAfterFile = true;

                // 1) Target - Rename
                if (targetFile.needsRename()) {
                    // TODO merge content if append and atomic
                    // if not atomic - content already appended??? if not atomic but rename?
                    // boolean rename = true;
                    // if (config.getTarget().isAppendEnabled() && config.getTarget().getAtomic() != null) {
                    // if (targetDelegator.getProvider().exists(targetFileNewPath)) {
                    // merge in existing file
                    // delete targetFileOldPath
                    // rename=false;
                    // }
                    // }
                    rename(logger, fileTransferLogPrefix, targetDelegator, sourceDelegator, targetDelegator, sourceFile, isAtomicallyEnabled, false);
                }
                // 2) Target - KeepModificationDate
                // useLastModified - extra due to cumilativeFile case - setting only one time from the last source file
                if (useLastModified && config.getTarget().isKeepModificationDateEnabled()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("[%s][%s][%s][setTargetFileModificationDate][UTC]%s", fileTransferLogPrefix, targetDelegator.getLabel(),
                                targetFile.getFinalFullPath(), sourceFile.getLastModifiedAsUTCString());
                    }
                    targetDelegator.getProvider().setFileLastModifiedFromMillis(targetFile.getFinalFullPath(), sourceFile.getLastModifiedMillis());
                    logger.info("[%s][%s][%s][%s][UTC]%s", fileTransferLogPrefix, targetDelegator.getLabel(), targetDelegator.getArgs()
                            .getKeepModificationDate().getName(), targetFile.getFinalFullPath(), sourceFile.getLastModifiedAsUTCString());
                }
                // 3) Target - CreateIntegrityHashFile
                if (config.getTarget().isCreateIntegrityHashFileEnabled() && targetFile.getIntegrityHash() != null) {
                    String path = targetFile.getFinalFullPath() + config.getIntegrityHashFileExtensionWithDot();
                    targetDelegator.getProvider().writeFile(path, targetFile.getIntegrityHash());
                    logger.info("[%s][%s][%s][%s]created", fileTransferLogPrefix, targetDelegator.getLabel(), targetDelegator.getArgs()
                            .getCreateIntegrityHashFile().getName(), path);
                }
            }
        }
        // 2) Source - individual operations
        if (config.getSource().needsFilePostProcessing()) {
            executeAfterFile = true;

            // 1) Source - Rename
            if (sourceFile.needsRename()) {
                rename(logger, fileTransferLogPrefix, sourceDelegator, sourceDelegator, targetDelegator, sourceFile, isAtomicallyEnabled, true);
            }
        }

        // 3) Source/Target - CommandAfterFile
        if (executeAfterFile) {
            YADECommandExecutor.executeAfterFile(logger, sourceDelegator, targetDelegator, sourceFile);
        }
    }

    public static void finalizeTargetFileSize(YADETargetProviderDelegator delegator, YADETargetProviderFile targetFile, boolean isCompress)
            throws Exception {
        if (isCompress) {// the file size check is suppressed by compress but we read the file size for logging and serialization
            String filePath = targetFile.getFullPath();
            targetFile = (YADETargetProviderFile) delegator.getProvider().rereadFileIfExists(targetFile);
            if (targetFile == null) {
                // ???? sourceFile.resetTarget();
                throw new YADEEngineTransferFileException(new SOSNoSuchFileException(filePath, null));
            }
        } else {
            targetFile.finalizeFileSize();
        }
    }

    public static void checkTargetFileSize(ISOSLogger logger, String fileTransferLogPrefix, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, YADEProviderFile sourceFile) throws Exception {
        if (!config.isCheckFileSizeEnabled()) {
            return;
        }
        if (sourceFile.getSize() != sourceFile.getTarget().getSize()) {
            String msg = String.format("[%s][%s=%s, Bytes=%s][%s=%s, Bytes=%s]", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile
                    .getFullPath(), sourceFile.getSize(), targetDelegator.getLabel(), sourceFile.getTarget().getFullPath(), sourceFile.getTarget()
                            .getSize());

            targetDelegator.getProvider().deleteFileIfExists(sourceFile.getTarget().getFullPath());
            sourceFile.getTarget().setState(TransferEntryState.ROLLED_BACK);
            logger.info("%s[file size does not match]target file deleted", msg);

            throw new YADEEngineTransferFileSizeException(msg + "file size does not match");
        }
    }

    private static void rename(ISOSLogger logger, String fileTransferLogPrefix, IYADEProviderDelegator delegator,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, YADEProviderFile sourceFile,
            boolean isAtomicallyEnabled, boolean isSource) throws Exception {
        YADEProviderFile sourceOrTargetFile = isSource ? sourceFile : sourceFile.getTarget();
        String oldPath = sourceOrTargetFile.getFullPath();
        String newPath = sourceOrTargetFile.getFinalFullPath();

        YADECommandExecutor.executeBeforeRename(logger, delegator, sourceDelegator, targetDelegator, sourceFile, isSource);

        if (delegator.getProvider().renameFileIfSourceExists(oldPath, newPath)) {
            // for error tests
            // sourceDelegator.getProvider().renameFileIfSourceExists(oldPath, newPath);

            sourceOrTargetFile.setSubState(TransferEntryState.RENAMED);
            String renameCause = isAtomicallyEnabled ? "AtomicRename" : "Rename";
            logger.info("[%s][%s][%s][%s]->[%s]renamed", fileTransferLogPrefix, delegator.getLabel(), renameCause, oldPath, newPath);
        }
    }

}
