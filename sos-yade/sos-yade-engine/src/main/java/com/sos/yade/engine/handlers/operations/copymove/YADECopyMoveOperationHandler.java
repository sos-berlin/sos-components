package com.sos.yade.engine.handlers.operations.copymove;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.exception.SOSNoSuchFileException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.delegators.YADEFileNameInfo;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderFile;
import com.sos.yade.engine.exceptions.YADEEngineOperationException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileIntegrityHashViolationException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileSizeException;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler;
import com.sos.yade.engine.helpers.YADEDelegatorHelper;

// TransferEntryState.NOT_OVERWRITTEN
// TransferEntryState.TRANSFERRING
// TransferEntryState.TRANSFERRED
// TransferEntryState.ROLLED_BACK <- (Target file deleted) checkTargetFileSize, checkChecksum
// targetFile.setSubState(TransferEntryState.RENAMED); after transfer
public class YADECopyMoveOperationHandler {

    public static void execute(TransferOperation operation, ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles) throws YADEEngineOperationException {
        if (targetDelegator == null) {
            throw new YADEEngineOperationException(new SOSMissingDataException("TargetDelegator"));
        }

        // 1) Source/Target: initialize transfer configuration(cumulative file,compress,atomic etc.)
        YADECopyMoveOperationConfig config = new YADECopyMoveOperationConfig(operation, args, sourceDelegator, targetDelegator);

        try {
            // 2) Target: map the source to the target directories and try to create all target directories before individual file transfer
            // - all target directories are only evaluated if target replacement is not enabled,
            // -- otherwise the target directories are evaluated/created on every file
            sourceDelegator.getDirectoryMapper().tryCreateAllTargetDirectoriesBeforeOperation(config, targetDelegator);
        } catch (SOSProviderException e) {
            throw new YADEEngineOperationException(e);
        }

        // 3) Target: delete cumulative file before Transfer files
        // TODO cumulative file - an extra object? read file size before operation? and calculate the file size progress if compress?
        if (config.getTarget().isDeleteCumulativeFileEnabled()) {
            try {
                targetDelegator.getProvider().deleteIfExists(config.getTarget().getCumulate().getFile().getFinalFullPath());
            } catch (Throwable e) {
                throw new YADEEngineOperationException(e);
            }
        }

        // 4) Source/Target: Transfer files
        boolean parallel = true;
        boolean parallelConfigured = false;

        // TODO throw exception - only if transactional ?
        // Parallel transfer
        if (parallel) {
            // Parallel: number of threads is configurable and controlled with an ExecutorService
            if (parallelConfigured) {

            }
            // Parallel: number of threads is controlled by Java
            else {
                AtomicInteger index = new AtomicInteger(0);
                try {
                    sourceFiles.parallelStream().forEach(f -> {
                        try {
                            transferFile(logger, config, sourceDelegator, targetDelegator, (YADEProviderFile) f, index.getAndIncrement());
                        } catch (Throwable e) {
                            new RuntimeException(e);
                        }
                    });
                } catch (Throwable e) {
                    throw new YADEEngineOperationException(e);
                }
            }
        } else {
            // Not parallel
            int index = 0;
            for (ProviderFile sourceFile : sourceFiles) {
                try {
                    transferFile(logger, config, sourceDelegator, targetDelegator, (YADEProviderFile) sourceFile, index++);
                } catch (Throwable e) {
                    // TODO - details about source/target file - should be set in transferFile(..)
                    throw new YADEEngineOperationException(e);
                }
            }
        }

    }

    private static void transferFile(ISOSLogger logger, YADECopyMoveOperationConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, YADEProviderFile sourceFile, int index) throws Exception {

        boolean isCumulateTarget = config.getTarget().getCumulate() != null;
        YADETargetProviderFile targetFile;
        // 1) Target - initialize/get Target file
        if (isCumulateTarget) {
            targetFile = config.getTarget().getCumulate().getFile();
        } else {
            // 1) Target: may create target directories if target replacement enabled
            sourceFile.initTarget(config, sourceDelegator, targetDelegator, index++);
            targetFile = sourceFile.getTarget();

            // 2) Target: check should be transferred...
            if (!config.getTarget().isOverwriteFilesEnabled()) {
                if (targetDelegator.getProvider().exists(targetFile.getFinalFullPath())) {
                    targetFile.setState(TransferEntryState.NOT_OVERWRITTEN);
                    YADECommandsHandler.executeBeforeFile(logger, sourceDelegator, targetDelegator, targetFile);
                    return;
                }
            }
        }

        // 2) Source/Target: commands before file transfer
        YADECommandsHandler.executeBeforeFile(logger, sourceDelegator, targetDelegator, sourceFile);
        targetFile.setState(TransferEntryState.TRANSFERRING);

        Instant startTime = Instant.now();
        boolean isCompressTarget = config.getTarget().getCompress() != null;
        MessageDigest sourceMessageDigest = getMessageDigest(config, config.getSource().isCheckIntegrityHashEnabled());
        MessageDigest targetMessageDigest = getMessageDigest(config, config.getTarget().isCreateIntegrityHashFileEnabled());

        boolean useBufferedStreams = true;

        int attempts = 0;

        boolean isCumulateTargetWritten = false;
        l: while (attempts < config.getMaxRetries()) {
            // int cumulativeFileSeperatorLength = 0;
            try (InputStream sourceStream = getInputStream(config, sourceDelegator, sourceFile, useBufferedStreams); OutputStream targetOutputStream =
                    getOutputStream(config, targetDelegator, targetFile, useBufferedStreams); OutputStream targetStream = isCompressTarget
                            ? new GZIPOutputStream(targetOutputStream) : targetOutputStream) {
                if (attempts > 0) {
                    skipToPosition(sourceStream, targetFile);
                }

                if (isCumulateTarget && !isCumulateTargetWritten) {
                    // TODO replace variables .... XML Schema description for CumulativeFileSeparator is wrong
                    String fs = config.getTarget().getCumulate().getFileSeparator() + System.getProperty("line.separator");
                    byte[] bytes = fs.getBytes();
                    // cumulativeFileSeperatorLength = bytes.length;
                    targetOutputStream.write(bytes);

                    updateSourceMessageDigest(sourceMessageDigest, bytes);
                    updateTargetMessageDigest(targetMessageDigest, bytes, isCompressTarget);
                    isCumulateTargetWritten = true;
                }

                if (sourceFile.getSize() <= 0L) {
                    byte[] bytes = new byte[0];
                    targetStream.write(bytes);

                    updateSourceMessageDigest(sourceMessageDigest, bytes);
                    updateTargetMessageDigest(targetMessageDigest, bytes, isCompressTarget);
                } else {
                    byte[] buffer = new byte[config.getBufferSize()];
                    int bytesRead;
                    while ((bytesRead = sourceStream.read(buffer)) != -1) {
                        targetStream.write(buffer, 0, bytesRead);
                        targetFile.updateProgressSize(bytesRead);

                        updateSourceMessageDigest(targetMessageDigest, buffer, bytesRead);
                        updateTargetMessageDigest(targetMessageDigest, buffer, bytesRead, isCompressTarget);
                    }
                    finishTargetStream(logger, targetFile, targetStream, isCompressTarget);
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
        finalizeTargetFileSize(targetDelegator, targetFile, isCompressTarget, isCumulateTarget);

        targetFile.setState(TransferEntryState.TRANSFERRED);
        logger.info("[%s][transferred][%s=%s][%s=%s][Bytes=%s]%s", sourceFile.getIndex(), sourceDelegator.getIdentifier(), sourceFile.getFullPath(),
                targetDelegator.getIdentifier(), targetFile.getFullPath(), targetFile.getSize(), SOSDate.getDuration(startTime, Instant.now()));

        checkTargetFileSize(logger, config, sourceDelegator, sourceFile, targetDelegator, targetFile);

        setChecksum(logger, targetDelegator, targetFile, sourceFile.getIndex(), targetMessageDigest);
        setChecksum(logger, sourceDelegator, sourceFile, sourceFile.getIndex(), sourceMessageDigest);

        checkChecksum(logger, config, sourceDelegator, sourceFile, targetDelegator, targetFile);

        YADECommandsHandler.executeAfterFile(logger, sourceDelegator, targetDelegator, sourceFile);

        // YADE1 renames after executeAfterFile command
        renameTargetFile(logger, config, sourceFile, targetDelegator, targetFile);
        keepModificationDate(config, sourceFile, targetDelegator, targetFile);

        // Source
        if (config.getSource().isReplacementEnabled()) {
            // 3) after successful file transfer - rename source file
            // TODO rename per file? only if not transactional?
            renameSourceFile(logger, sourceDelegator, sourceFile);
        }
    }

    private static void renameTargetFile(ISOSLogger logger, YADECopyMoveOperationConfig config, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) throws SOSProviderException {
        if (!targetFile.needsRename()) {
            return;
        }
        String targetFileOldPath = targetFile.getFullPath();
        String targetFileNewPath = targetFile.getFinalFullPath();
        targetDelegator.getProvider().rename(targetFileOldPath, targetFileNewPath);
        targetFile.setSubState(TransferEntryState.RENAMED);
        logger.info("[%s]%s[%s][renamed][%s]", sourceFile.getIndex(), targetDelegator.getLogPrefix(), targetFileOldPath, targetFileNewPath);
    }

    private static void keepModificationDate(YADECopyMoveOperationConfig config, YADEProviderFile sourceFile,
            YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) {
        if (!config.getTarget().isKeepModificationDateEnabled()) {
            return;
        }
        targetDelegator.getProvider().setFileLastModifiedFromMillis(targetFile.getFinalFullPath(), sourceFile.getLastModifiedMillis());
    }

    /** Checks a checksum file on the Source system and deletes the transferred Target file if the checksum does not match */
    private static void checkChecksum(ISOSLogger logger, YADECopyMoveOperationConfig config, YADESourceProviderDelegator sourceDelegator,
            YADEProviderFile sourceFile, YADETargetProviderDelegator targetDelegator, YADETargetProviderFile targetFile) throws Exception {
        if (!config.getSource().isCheckIntegrityHashEnabled() || sourceFile.getChecksum() == null) {
            return;
        }
        String sourceChecksumFile = sourceFile.getFullPath() + "." + config.getIntegrityHashAlgorithm();
        String sourceChecksum = sourceDelegator.getProvider().getFileContentIfExists(sourceChecksumFile);

        String msg = String.format("%s]%s[%s", sourceFile.getIndex(), sourceDelegator.getLogPrefix(), sourceChecksumFile);
        if (sourceChecksum == null) {
            logger.info("[%s]checksum file not found", msg);
            return;
        }
        if (sourceChecksum.equals(sourceFile.getChecksum())) {
            logger.info("[%s]checksum matches", msg);
        } else {
            targetDelegator.getProvider().deleteIfExists(targetFile.getFullPath());
            targetFile.setState(TransferEntryState.ROLLED_BACK);
            logger.info("[%s][%s][calculated=%s][checksum does not match]target file deleted", msg, sourceChecksum, sourceFile.getChecksum());

            throw new YADEEngineTransferFileIntegrityHashViolationException(String.format("[%s][%s][calculated]%s", msg, sourceChecksum, sourceFile
                    .getChecksum()));
        }
    }

    private static void finalizeTargetFileSize(YADETargetProviderDelegator delegator, YADETargetProviderFile file, boolean isCompress,
            boolean isCumulate) throws Exception {
        if (isCompress) {// the file size check is suppressed by compress but we read the file size for logging and serialization
            if (!isCumulate) {
                String filePath = file.getFullPath();
                file = (YADETargetProviderFile) delegator.getProvider().rereadFileIfExists(file);
                if (file == null) {
                    throw new YADEEngineTransferFileException(new SOSNoSuchFileException(filePath, null));
                }
            }
        } else {
            // if isCumulate -
            file.finalizeFileSize();
        }
    }

    private static void checkTargetFileSize(ISOSLogger logger, YADECopyMoveOperationConfig config, YADESourceProviderDelegator sourceDelegator,
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

    private static void setChecksum(ISOSLogger logger, AYADEProviderDelegator delegator, YADEProviderFile file, int fileIndex,
            MessageDigest messageDigest) {
        if (messageDigest == null) {
            return;
        }
        file.setChecksum(messageDigest);
        // YADE1 sets if target: sourceFile.setChecksum = target checksum ????
        if (logger.isDebugEnabled()) {
            logger.debug("[%s][%s=%s][checksum]%s", fileIndex, delegator.getIdentifier(), file.getFullPath(), file.getChecksum());
        }
    }

    private static void updateSourceMessageDigest(MessageDigest digest, byte[] data) throws Exception {
        if (digest == null) {
            return;
        }
        digest.update(data);
    }

    private static void updateSourceMessageDigest(MessageDigest digest, byte[] data, int len) throws Exception {
        if (digest == null) {
            return;
        }
        digest.update(data, 0, len);
    }

    private static void updateTargetMessageDigest(MessageDigest digest, byte[] data, boolean isCompress) throws Exception {
        if (digest == null) {
            return;
        }
        if (isCompress) {
            byte[] r = SOSGzip.compressBytes(data, data.length);
            digest.update(r, 0, r.length);
        } else {
            digest.update(data);
        }
    }

    private static void updateTargetMessageDigest(MessageDigest digest, byte[] data, int len, boolean isCompress) throws Exception {
        if (digest == null) {
            return;
        }
        if (isCompress) {
            byte[] r = SOSGzip.compressBytes(data, len);
            digest.update(r, 0, r.length);
        } else {
            digest.update(data, 0, len);
        }
    }

    private static void skipToPosition(InputStream sourceStream, YADETargetProviderFile targetFile) throws IOException {
        long startPosition = targetFile.getBytesProcessed();
        long skipped = sourceStream.skip(startPosition);
        s: while (skipped < startPosition) {
            long newlySkipped = sourceStream.skip(startPosition - skipped);
            if (newlySkipped == 0) {
                break s;
            }
            skipped += newlySkipped;
        }
    }

    private static void finishTargetStream(ISOSLogger logger, YADEProviderFile targetFile, OutputStream targetStream, boolean isCompress) {
        try {
            if (isCompress) {
                ((GZIPOutputStream) targetStream).finish();
            }
            targetStream.flush();
        } catch (Throwable e) {
            logger.warn("[comressFinish][%s]%s", targetFile.getFullPath(), e.toString());
        }
    }

    private static InputStream getInputStream(YADECopyMoveOperationConfig config, YADESourceProviderDelegator sourceDelegator,
            YADEProviderFile sourceFile, boolean useBufferedStreams) throws SOSProviderException {
        InputStream is = sourceDelegator.getProvider().getInputStream(sourceFile.getFullPath());
        if (useBufferedStreams) {
            return new BufferedInputStream(is, config.getBufferSize());
        }
        return is;
    }

    private static OutputStream getOutputStream(YADECopyMoveOperationConfig config, YADETargetProviderDelegator targetDelegator,
            YADEProviderFile targetFile, boolean useBufferedStreams) throws SOSProviderException {
        OutputStream os = targetDelegator.getProvider().getOutputStream(targetFile.getFullPath(), config.getTarget().isAppendEnabled());
        if (useBufferedStreams) {
            return new BufferedOutputStream(os, config.getBufferSize());
        }
        return os;
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
            sourceDelegator.getProvider().rename(sourceFile.getFullPath(), sourceFile.getFinalFullPath());
            // after successful rename
            sourceFile.setState(TransferEntryState.RENAMED);

        }
    }

    // NoSuchAlgorithmException is already checked on YADEEngine start
    private static MessageDigest getMessageDigest(YADECopyMoveOperationConfig config, boolean create) {
        if (!create) {
            return null;
        }
        try {
            return MessageDigest.getInstance(config.getIntegrityHashAlgorithm());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

}
