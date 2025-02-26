package com.sos.yade.engine.handlers.operations.copymove.fileoperations;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderFile;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileRuntimeException;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler;
import com.sos.yade.engine.handlers.operations.copymove.CopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.fileoperations.helpers.FileActionsHandler;
import com.sos.yade.engine.handlers.operations.copymove.fileoperations.helpers.FileIntegrityHashHelper;
import com.sos.yade.engine.handlers.operations.copymove.fileoperations.helpers.FileStreamHelper;
import com.sos.yade.engine.helpers.YADEDelegatorHelper;

/** Single "transfer" file manager */
public class FileHandler {

    // SSH (buffer_size=32KB): 4.5GB ~ 1.15 minutes, 1.5 GB ~ 25 seconds
    private static final long LOG_TRANSFER_START_IF_FILESIZE_GREATER_THAN = 3221225475L;// 3GB

    private static final long USE_BUFFERED_STREAMS_IF_FILESIZE_GREATER_THAN = 10485760L;// 10 MB

    private final ISOSLogger logger;
    private final CopyMoveOperationsConfig config;
    private final YADESourceProviderDelegator sourceDelegator;
    private final YADETargetProviderDelegator targetDelegator;
    private final YADEProviderFile sourceFile;
    private final AtomicBoolean cancel;

    public FileHandler(ISOSLogger logger, CopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, YADEProviderFile sourceFile, AtomicBoolean cancel) {
        this.logger = logger;
        this.config = config;
        this.sourceDelegator = sourceDelegator;
        this.targetDelegator = targetDelegator;
        this.sourceFile = sourceFile;
        this.cancel = cancel;
    }

    public void run() {
        try {
            boolean isCumulateTarget = config.getTarget().getCumulate() != null;
            String logPrefix = config.getParallelMaxThreads() == 1 ? String.valueOf(sourceFile.getIndex()) : sourceFile.getIndex() + "][" + Thread
                    .currentThread().getName();
            YADETargetProviderFile targetFile;
            // 1) Target - initialize/get Target file
            if (isCumulateTarget) {
                targetFile = config.getTarget().getCumulate().getFile();
            } else {
                // 1) Target: may create target directories if target replacement enabled
                sourceFile.initTarget(logger, config, sourceDelegator, targetDelegator);
                targetFile = sourceFile.getTarget();

                // 2) Target: check should be transferred...
                if (!config.getTarget().isOverwriteFilesEnabled()) {
                    if (targetDelegator.getProvider().exists(targetFile.getFinalFullPath())) {
                        targetFile.setState(TransferEntryState.NOT_OVERWRITTEN);

                        logger.info("[%s][skipped][DisableOverwriteFiles=true]%s=%s", logPrefix, targetDelegator.getIdentifier(), targetFile
                                .getFinalFullPath());

                        YADECommandsHandler.executeBeforeFile(logger, sourceDelegator, targetDelegator, targetFile);
                        return;
                    }
                }
            }

            // 2) Source/Target: commands before file transfer
            YADECommandsHandler.executeBeforeFile(logger, sourceDelegator, targetDelegator, sourceFile);
            targetFile.setState(TransferEntryState.TRANSFERRING);
            // TODO config.getParallelMaxThreads() == 1 - make it sense if parallel because of random order?
            if (config.getParallelMaxThreads() == 1 && sourceFile.getSize() >= LOG_TRANSFER_START_IF_FILESIZE_GREATER_THAN) {
                logger.info("[%s][%s][%s]start...", logPrefix, sourceDelegator.getIdentifier(), sourceFile.getFullPath());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[%s][%s][%s][% b]start...", logPrefix, sourceDelegator.getIdentifier(), sourceFile.getFullPath(), sourceFile
                            .getSize());
                }
            }

            boolean isCompressTarget = config.getTarget().getCompress() != null;
            MessageDigest sourceMessageDigest = FileIntegrityHashHelper.getMessageDigest(config, config.getSource().isCheckIntegrityHashEnabled());
            MessageDigest targetMessageDigest = FileIntegrityHashHelper.getMessageDigest(config, config.getTarget()
                    .isCreateIntegrityHashFileEnabled());

            boolean useBufferedStreams = sourceFile.getSize() > USE_BUFFERED_STREAMS_IF_FILESIZE_GREATER_THAN ? true : false;

            int attempts = 0;
            boolean isCumulateTargetWritten = false;

            Instant startTime = Instant.now();
            l: while (attempts < config.getMaxRetries()) {
                // int cumulativeFileSeperatorLength = 0;
                try (InputStream sourceStream = FileStreamHelper.getSourceInputStream(config, sourceDelegator, sourceFile, useBufferedStreams);
                        OutputStream targetOutputStream = FileStreamHelper.getTargetOutputStream(config, targetDelegator, targetFile,
                                useBufferedStreams); OutputStream targetStream = isCompressTarget ? new GZIPOutputStream(targetOutputStream)
                                        : targetOutputStream) {
                    if (attempts > 0) {
                        FileStreamHelper.skipSourceInputStreamToPosition(sourceStream, targetFile);
                    }

                    if (isCumulateTarget && !isCumulateTargetWritten) {
                        // TODO replace variables .... XML Schema description for CumulativeFileSeparator is wrong
                        String fs = config.getTarget().getCumulate().getFileSeparator() + System.getProperty("line.separator");
                        byte[] bytes = fs.getBytes();
                        // cumulativeFileSeperatorLength = bytes.length;
                        targetOutputStream.write(bytes);

                        FileIntegrityHashHelper.updateSourceMessageDigest(sourceMessageDigest, bytes);
                        FileIntegrityHashHelper.updateTargetMessageDigest(targetMessageDigest, bytes, isCompressTarget);
                        isCumulateTargetWritten = true;
                    }

                    if (sourceFile.getSize() <= 0L) {
                        byte[] bytes = new byte[0];
                        targetStream.write(bytes);

                        FileIntegrityHashHelper.updateSourceMessageDigest(sourceMessageDigest, bytes);
                        FileIntegrityHashHelper.updateTargetMessageDigest(targetMessageDigest, bytes, isCompressTarget);
                    } else {
                        byte[] buffer = new byte[config.getBufferSize()];
                        int bytesRead;
                        while ((bytesRead = sourceStream.read(buffer)) != -1) {
                            targetStream.write(buffer, 0, bytesRead);
                            targetFile.updateProgressSize(bytesRead);

                            FileIntegrityHashHelper.updateSourceMessageDigest(targetMessageDigest, buffer, bytesRead);
                            FileIntegrityHashHelper.updateTargetMessageDigest(targetMessageDigest, buffer, bytesRead, isCompressTarget);
                        }
                        FileStreamHelper.finishTargetOutputStream(logger, targetFile, targetStream, isCompressTarget);
                    }
                    break l;
                } catch (Throwable e) {
                    attempts++;

                    // ?
                    if (targetFile.getSize() < sourceFile.getSize()) {

                    }

                    YADEDelegatorHelper.ensureConnected(logger, sourceDelegator);
                    YADEDelegatorHelper.ensureConnected(logger, targetDelegator);

                    if (attempts >= config.getMaxRetries()) {
                        throw new YADEEngineTransferFileException("[" + targetFile.getFullPath() + "]Maximum retry attempts=" + config.getMaxRetries()
                                + " reached", e);
                    }
                }
            }
            FileActionsHandler.finalizeTargetFileSize(targetDelegator, sourceFile, targetFile, isCompressTarget, isCumulateTarget);

            targetFile.setState(TransferEntryState.TRANSFERRED);
            logger.info("[%s][transferred][%s=%s][%s=%s][bytes=%s]%s", logPrefix, sourceDelegator.getIdentifier(), sourceFile.getFullPath(),
                    targetDelegator.getIdentifier(), targetFile.getFullPath(), targetFile.getSize(), SOSDate.getDuration(startTime, Instant.now()));

            FileActionsHandler.checkTargetFileSize(logger, config, sourceDelegator, sourceFile, targetDelegator, targetFile);

            FileIntegrityHashHelper.setChecksum(logger, targetDelegator, targetFile, sourceFile.getIndex(), targetMessageDigest);
            FileIntegrityHashHelper.setChecksum(logger, sourceDelegator, sourceFile, sourceFile.getIndex(), sourceMessageDigest);
            FileIntegrityHashHelper.checkSourceChecksum(logger, config, sourceDelegator, sourceFile, targetDelegator, targetFile);

            YADECommandsHandler.executeAfterFile(logger, sourceDelegator, targetDelegator, sourceFile);

            // YADE1 renames after executeAfterFile command
            // Rename Target always - transactional transfer or not
            FileActionsHandler.renameTargetFile(logger, config, sourceFile, targetDelegator, targetFile);
            FileActionsHandler.setTargetFileModificationDate(logger, config, sourceFile, targetDelegator, targetFile);

            // Source
            FileActionsHandler.processSourceFileAfterNonTransactionalTransfer(logger, config, sourceDelegator, sourceFile);
        } catch (Throwable e) {
            throw new YADEEngineTransferFileRuntimeException(e);
        }
    }

}
