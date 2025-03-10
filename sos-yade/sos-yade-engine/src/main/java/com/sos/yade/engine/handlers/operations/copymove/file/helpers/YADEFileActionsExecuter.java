package com.sos.yade.engine.handlers.operations.copymove.file.helpers;

import java.util.Optional;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.exceptions.SOSProviderException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileSizeException;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.file.YADEFileHandler;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADEFileNameInfo;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;

/** Single "transfer" file operations */
public class YADEFileActionsExecuter {

    /** Target: ProviderFile Operations */
    public static void renameTargetFile(ISOSLogger logger, String logPrefix, YADETargetProviderDelegator targetDelegator, YADEProviderFile targetFile)
            throws SOSProviderException {
        if (targetFile == null || !targetFile.needsRename()) {
            return;
        }

        String targetFileOldPath = targetFile.getFullPath();
        String targetFileNewPath = targetFile.getFinalFullPath();
        targetDelegator.getProvider().renameFileIfSourceExists(targetFileOldPath, targetFileNewPath);
        targetFile.setSubState(TransferEntryState.RENAMED);
        logger.info("[%s]%s[%s][renamed][%s]", logPrefix, targetDelegator.getLogPrefix(), targetFileOldPath, targetFileNewPath);
    }

    public static void setTargetFileModificationDate(ISOSLogger logger, String logPrefix, YADECopyMoveOperationsConfig config,
            YADEProviderFile sourceFile, YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) throws Exception {
        if (!config.getTarget().isKeepModificationDateEnabled()) {
            return;
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[%s]%s[%s][setTargetFileModificationDate][UTC]%s", logPrefix, targetDelegator.getLogPrefix(), targetFile.getFinalFullPath(),
                    sourceFile.getLastModifiedAsUTCString());
        }
        targetDelegator.getProvider().setFileLastModifiedFromMillis(targetFile.getFinalFullPath(), sourceFile.getLastModifiedMillis());
    }

    public static void finalizeTargetFileSize(YADETargetProviderDelegator delegator, YADEProviderFile sourceFile, YADETargetProviderFile targetFile,
            boolean isCompress) throws Exception {
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

    public static void checkTargetFileSize(ISOSLogger logger, String logPrefix, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile, YADETargetProviderDelegator targetDelegator,
            YADETargetProviderFile targetFile) throws Exception {
        if (!config.isCheckFileSizeEnabled()) {
            return;
        }
        if (sourceFile.getSize() != targetFile.getSize()) {
            String msg = String.format("[%s][%s=%s, Bytes=%s][%s=%s, Bytes=%s]", logPrefix, sourceDelegator.getIdentifier(), sourceFile.getFullPath(),
                    sourceFile.getSize(), targetDelegator.getIdentifier(), targetFile.getFullPath(), targetFile.getSize());

            targetDelegator.getProvider().deleteIfExists(targetFile.getFullPath());
            targetFile.setState(TransferEntryState.ROLLED_BACK);
            logger.info("%s[file size does not match]target file deleted", msg);

            throw new YADEEngineTransferFileSizeException(msg + "file size does not match");
        }
    }

    /** Source: ProviderFile Operations */
    // TODO set State? subState? how to display the source file info in the summary?
    public static void processSourceFileAfterNonTransactionalTransfer(ISOSLogger logger, String logPrefix, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile) throws SOSProviderException {
        if (config.isTransactionalEnabled()) {
            return;
        }

        if (config.isMoveOperation()) {
            sourceDelegator.getProvider().deleteIfExists(sourceFile.getFullPath());
            sourceFile.setState(TransferEntryState.MOVED);
        } else if (config.getSource().isReplacementEnabled()) {
            renameSourceFile(logger, logPrefix, sourceDelegator, sourceFile);
        }
    }

    private static void renameSourceFile(ISOSLogger logger, String logPrefix, YADESourceProviderDelegator sourceDelegator,
            YADEProviderFile sourceFile) throws SOSProviderException {
        Optional<YADEFileNameInfo> newNameInfo = YADEFileHandler.getReplacementResultIfDifferent(sourceDelegator, sourceFile);
        if (newNameInfo.isPresent()) {
            YADEFileNameInfo info = newNameInfo.get();
            sourceFile.setFinalFullPath(sourceDelegator, YADEFileHandler.getFinalFullPath(sourceDelegator, sourceFile, info));
            sourceDelegator.getDirectoryMapper().tryCreateSourceDirectory(logger, sourceDelegator, sourceFile, info);

            // rename
            sourceDelegator.getProvider().renameFileIfSourceExists(sourceFile.getFullPath(), sourceFile.getFinalFullPath());
            // after successful rename
            sourceFile.setState(TransferEntryState.RENAMED);
        }
    }

}
