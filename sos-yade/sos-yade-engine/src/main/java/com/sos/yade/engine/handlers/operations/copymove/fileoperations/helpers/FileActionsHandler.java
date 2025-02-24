package com.sos.yade.engine.handlers.operations.copymove.fileoperations.helpers;

import java.util.Optional;

import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.delegators.YADEFileNameInfo;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderFile;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileSizeException;
import com.sos.yade.engine.handlers.operations.copymove.CopyMoveOperationsConfig;

/** Single "transfer" file operations */
public class FileActionsHandler {

    /** Target: ProviderFile Operations */
    public static void renameTargetFile(ISOSLogger logger, CopyMoveOperationsConfig config, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) throws SOSProviderException {
        if (!targetFile.needsRename()) {
            return;
        }
        String targetFileOldPath = targetFile.getFullPath();
        String targetFileNewPath = targetFile.getFinalFullPath();
        targetDelegator.getProvider().renameFileIfExists(targetFileOldPath, targetFileNewPath);
        targetFile.setSubState(TransferEntryState.RENAMED);
        logger.info("[%s]%s[%s][renamed][%s]", sourceFile.getIndex(), targetDelegator.getLogPrefix(), targetFileOldPath, targetFileNewPath);
    }

    public static void setTargetFileModificationDate(ISOSLogger logger, CopyMoveOperationsConfig config, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) throws SOSProviderException {
        if (!config.getTarget().isKeepModificationDateEnabled()) {
            return;
        }
        targetDelegator.getProvider().setFileLastModifiedFromMillis(targetFile.getFinalFullPath(), sourceFile.getLastModifiedMillis());
        if (logger.isDebugEnabled()) {
            logger.debug("[%s]%s[%s][setTargetFileModificationDate]%s", sourceFile.getIndex(), targetDelegator.getLogPrefix(), targetFile
                    .getFinalFullPath(), targetFile.getLastModifiedAsString());
        }
    }

    public static void finalizeTargetFileSize(YADETargetProviderDelegator delegator, YADEProviderFile sourceFile, YADETargetProviderFile targetFile,
            boolean isCompress, boolean isCumulate) throws Exception {
        if (isCompress) {// the file size check is suppressed by compress but we read the file size for logging and serialization
            if (!isCumulate) {
                String filePath = targetFile.getFullPath();
                targetFile = (YADETargetProviderFile) delegator.getProvider().rereadFileIfExists(targetFile);
                if (targetFile == null) {
                    // ???? sourceFile.resetTarget();
                    throw new YADEEngineTransferFileException(new SOSNoSuchFileException(filePath, null));
                }
            }
        } else {
            targetFile.finalizeFileSize();
        }
    }

    public static void checkTargetFileSize(ISOSLogger logger, CopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADEProviderFile sourceFile, YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) throws Exception {
        if (!config.isCheckFileSizeEnabled()) {
            return;
        }
        if (sourceFile.getSize() != targetFile.getSize()) {
            String msg = String.format("[%s][%s=%s, Bytes=%s][%s=%s, Bytes=%s]", sourceFile.getIndex(), sourceDelegator.getIdentifier(), sourceFile
                    .getFullPath(), sourceFile.getSize(), targetDelegator.getIdentifier(), targetFile.getFullPath(), targetFile.getSize());

            targetDelegator.getProvider().deleteIfExists(targetFile.getFullPath());
            targetFile.setState(TransferEntryState.ROLLED_BACK);
            logger.info("%s[file size does not match]target file deleted", msg);

            throw new YADEEngineTransferFileSizeException(msg + "file size does not match");
        }
    }

    /** Source: ProviderFile Operations */
    public static void processSourceFileAfterNonTransactionalTransfer(ISOSLogger logger, CopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile) throws SOSProviderException {
        if (!config.getTarget().isAtomicTransactionalEnabled()) {
            return;
        }

        if (config.isMoveOperation()) {
            try {
                sourceDelegator.getProvider().deleteIfExists(sourceFile.getFullPath());
                sourceFile.setState(TransferEntryState.MOVED);
            } catch (Throwable e) {
                logger.error("[%s][delete]%s", sourceFile.getIndex(), e.toString());
            }
        } else if (config.getSource().isReplacementEnabled()) {
            renameSourceFile(logger, sourceDelegator, sourceFile);
        }
    }

    // TODO rename per file? - rollback... or after transfer of all files?
    private static void renameSourceFile(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile)
            throws SOSProviderException {
        Optional<YADEFileNameInfo> newNameInfo = sourceFile.getReplacementResultIfDifferent(sourceDelegator);
        if (newNameInfo.isPresent()) {
            YADEFileNameInfo info = newNameInfo.get();
            sourceFile.setFinalName(info);
            sourceDelegator.getDirectoryMapper().tryCreateSourceDirectory(sourceDelegator, sourceFile, info);

            // rename
            sourceDelegator.getProvider().renameFileIfExists(sourceFile.getFullPath(), sourceFile.getFinalFullPath());
            // after successful rename
            sourceFile.setState(TransferEntryState.RENAMED);
        }
    }
}
