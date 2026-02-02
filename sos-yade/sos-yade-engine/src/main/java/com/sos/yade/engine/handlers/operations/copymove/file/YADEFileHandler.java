package com.sos.yade.engine.handlers.operations.copymove.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.http.HttpUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.azure.AzureBlobStorageProvider;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.commons.vfs.exceptions.ProviderConnectException;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.http.HTTPProvider;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADEArguments.RetryOnConnectionError;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.commons.helpers.YADEClientHelper;
import com.sos.yade.engine.commons.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.exceptions.YADEEngineException;
import com.sos.yade.engine.exceptions.YADEEngineInvalidExpressionException;
import com.sos.yade.engine.exceptions.YADEEngineTargetOutputStreamException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileException;
import com.sos.yade.engine.handlers.command.YADECommandExecutor;
import com.sos.yade.engine.handlers.command.YADEFileCommandVariablesResolver;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsConfig;
import com.sos.yade.engine.handlers.operations.copymove.YADECopyMoveOperationsHandler;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADEFileNameInfo;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADEChecksumFileHelper;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADEFileActionsExecuter;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADEFileReplacementHelper;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADEFileStreamHelper;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADERetryFileHelper;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADERetryFileHelper.Result;

/** Single "transfer" file manager */
public class YADEFileHandler {

    // TODO - Temporary file size limit for uploads to Azure Blob Storage (due to non-streaming implementation)
    private static final long AZURE_BLOB_STORAGE_MAX_UPLOAD_FILESIZE = 50L * 1024 * 1024;// 50MB
    // SSH (buffer_size=32KB): 4.5GB ~ 1.15 minutes, 1.5 GB ~ 25 seconds
    private static final long LOG_TRANSFER_START_IF_FILESIZE_GREATER_THAN = 1073741824L;// 1GB

    private final ISOSLogger logger;
    private final YADECopyMoveOperationsConfig config;
    private final YADESourceProviderDelegator sourceDelegator;
    private final YADETargetProviderDelegator targetDelegator;
    private final YADEProviderFile sourceFile;
    @SuppressWarnings("unused")
    private final AtomicBoolean cancel;

    public YADEFileHandler(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, YADEProviderFile sourceFile, AtomicBoolean cancel) {
        this.logger = logger;
        this.config = config;
        this.sourceDelegator = sourceDelegator;
        this.targetDelegator = targetDelegator;
        this.sourceFile = sourceFile;
        this.cancel = cancel;
    }

    public void run(boolean isMoveOperation, boolean useCumulativeTargetFile, boolean useLastModified) throws YADEEngineTransferFileException {
        // 'index' or 'index][thread name'
        String fileTransferLogPrefix = config.getParallelism() == 1 ? String.valueOf(sourceFile.getIndex()) : sourceFile.getIndex() + "][" + Thread
                .currentThread().getName();

        prepareSource(fileTransferLogPrefix);

        YADETargetProviderFile targetFile = null;
        String cumulativeTargetFileSeparator = null;
        try {
            // 1) Target - initialize/get Target file
            if (useCumulativeTargetFile) {
                targetFile = config.getTarget().getCumulate().getFile();
                cumulativeTargetFileSeparator = YADEFileCommandVariablesResolver.resolve(sourceDelegator, targetDelegator, sourceFile, config
                        .getTarget().getCumulate().getFileSeparator()) + System.getProperty("line.separator");
                sourceFile.setTarget(targetFile);
            } else {
                // 1) Target: may create target directories if target replacement enabled
                initializeTarget();
                targetFile = sourceFile.getTarget();

                // 2) Target: check should be transferred...
                if (!config.getTarget().isOverwriteFilesEnabled()) {
                    ProviderFile tf = targetDelegator.getProvider().getFileIfExists(targetFile.getFinalFullPath());
                    // if (targetDelegator.getProvider().exists(targetFile.getFinalFullPath())) {
                    if (tf != null) {
                        targetFile.setFullPath(targetFile.getFinalFullPath()); // reset possible rename, atomic etc

                        logger.info("[%s][%s][%s=%s, Bytes=%s, ModificationDate(UTC)=%s][%s=%s, Bytes=%s, ModificationDate(UTC)=%s",
                                fileTransferLogPrefix, YADEClientBannerWriter.formatState(TransferEntryState.TRANSFERRING), sourceDelegator
                                        .getLabel(), sourceFile.getFullPath(), sourceFile.getSize(), sourceFile.getLastModifiedAsUTCString(),
                                targetDelegator.getLabel(), targetFile.getFinalFullPath(), tf.getSize(), tf.getLastModifiedAsUTCString());

                        targetFile.setState(TransferEntryState.NOT_OVERWRITTEN);
                        logger.info("[%s][%s][%s]%s", fileTransferLogPrefix, targetDelegator.getLabel(), YADEClientBannerWriter.formatState(targetFile
                                .getState()), targetFile.getFinalFullPath());

                        YADECommandExecutor.executeBeforeFile(logger, sourceDelegator, targetDelegator, sourceFile);
                        finalizeIfNonTransactional(isMoveOperation, false, fileTransferLogPrefix);
                        return;
                    }
                }
            }

            // TODO
            if (targetDelegator.isAzure()) {
                if (sourceFile.getSize() >= AZURE_BLOB_STORAGE_MAX_UPLOAD_FILESIZE) {
                    targetFile.setState(TransferEntryState.ABORTED);
                    String msg = String.format("[%s][%s][%s][upload cancelled]file size %s exceeds temporary %s YADE limit for Azure Blob Storage",
                            fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath(), SOSShell.formatBytes(sourceFile.getSize()),
                            SOSShell.formatBytes(AZURE_BLOB_STORAGE_MAX_UPLOAD_FILESIZE));
                    throw new YADEEngineTransferFileException(msg);
                }
            }

            // 2) Source/Target: commands before file transfer
            YADECommandExecutor.executeBeforeFile(logger, sourceDelegator, targetDelegator, sourceFile);
            targetFile.setState(TransferEntryState.TRANSFERRING);
            // TODO config.getParallelMaxThreads() == 1 - make it sense if parallel because of random order?
            if (config.getParallelism() == 1 && sourceFile.getSize() >= LOG_TRANSFER_START_IF_FILESIZE_GREATER_THAN) {
                logger.info("[%s][%s][%s][%s, Bytes=%s][%s][%s]start...", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile
                        .getState()), sourceDelegator.getLabel(), sourceFile.getFullPath(), sourceFile.getSize(), targetDelegator.getLabel(),
                        targetFile.getFullPath());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[%s][%s][%s][%s, Bytes=%s][%s][%s]start...", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile
                            .getState()), sourceDelegator.getLabel(), sourceFile.getFullPath(), sourceFile.getSize(), targetDelegator.getLabel(),
                            targetFile.getFullPath());
                }
            }

            // not compress if cumulative file
            boolean compressTarget = config.getTarget().getCompress() != null && !useCumulativeTargetFile;
            MessageDigest sourceMessageDigest = YADEChecksumFileHelper.initializeMessageDigest(config, config.getSource()
                    .isCheckIntegrityHashEnabled());
            MessageDigest targetMessageDigest = YADEChecksumFileHelper.initializeMessageDigest(config, config.getTarget()
                    .isCreateIntegrityHashFileEnabled());

            int attempts = 0;
            boolean isCumulateTargetWritten = false;
            // int cumulativeFileSeperatorLength = 0;

            Instant startTime = Instant.now();
            if (targetDelegator.isHTTP()) {
                // TODO compressing, cumulative, messageDigest, offset ...
                long sourceFileReadOffset = 0L;
                l: while (attempts <= config.getRetry().getMaxRetries()) {
                    try (InputStream sourceStream = YADEFileStreamHelper.getSourceInputStream(config, sourceDelegator, sourceFile,
                            sourceFileReadOffset)) {
                        if (targetDelegator.isAzure()) {
                            targetFile.setSize(((AzureBlobStorageProvider) targetDelegator.getProvider()).upload(targetFile.getFullPath(),
                                    sourceStream, sourceFile.getSize()));
                        } else {
                            targetFile.setSize(((HTTPProvider) targetDelegator.getProvider()).upload(targetFile.getFullPath(), sourceStream,
                                    sourceFile.getSize()));
                        }
                        break l;
                    } catch (Exception e) {
                        attempts++;
                        handleException(fileTransferLogPrefix, targetFile, e, attempts, true);
                    }
                }
            } else {
                // AppendFiles/CumulateFiles
                boolean targetIsAppendEnabled = config.getTarget().isAppendEnabled();
                YADERetryFileHelper retryHelper = new YADERetryFileHelper(config.getRetry().isEnabled());
                retryHelper.beforeTransfer(logger, targetDelegator, targetFile, targetIsAppendEnabled, config.getTarget().isResumeEnabled());

                l: while (attempts <= config.getRetry().getMaxRetries()) {
                    if (attempts > 0) { // retry on connection errors
                        // Observations on Windows with the LocalProvider as Target:
                        // - For large files (~1.5 GB) and RetryInterval=1s, retrying may fail with
                        // -- java.nio.file.FileSystemException ("The process cannot access the file because it is being used by another process").
                        // - Root cause: On Windows, Java opens files without shared write access.
                        // -- After closing the stream, external processes (e.g. antivirus ...) may temporarily hold a read lock on the file.
                        // -- As a result, immediate reopen attempts can fail even though all Java streams have been properly closed (try-with-resources).
                        // - Workarounds / solutions:
                        // 1) Increase retry interval to allow external file scans to complete.
                        // 2) Prefer transactional, atomic transfers (write to temporary file and atomically move/replace the target file after successful
                        // transfer).

                        // Resume example: SFTP->Local (~1.5GB) reduces transfer from ~27s to ~16s (including 1-2s RetryInterval + SFTP reconnect)
                        Result onRetry = retryHelper.onRetry(logger, fileTransferLogPrefix, sourceFile, targetDelegator, targetFile,
                                useCumulativeTargetFile);
                        switch (onRetry.getType()) {
                        case RESTART: // "normal, append/cumulative or resume
                            targetIsAppendEnabled = config.getTarget().isAppendEnabled();

                            // reset - due to restart the whole transfer of the source file
                            targetFile.resetBytesProcessed();// 0L

                            sourceMessageDigest = YADEChecksumFileHelper.initializeMessageDigest(config, config.getSource()
                                    .isCheckIntegrityHashEnabled());
                            targetMessageDigest = YADEChecksumFileHelper.initializeMessageDigest(config, config.getTarget()
                                    .isCreateIntegrityHashFileEnabled());
                            break;
                        case RESUME: // append/cumulative or resume
                            targetIsAppendEnabled = true;

                            targetFile.setBytesProcessed(onRetry.getOffset());
                            break;
                        case COMPLETED:
                        case SKIPPED:
                            break l;
                        }
                    }

                    // cumulativeFileSeperatorLength = 0;
                    Exception exception = null;
                    try (InputStream sourceStream = YADEFileStreamHelper.getSourceInputStream(config, sourceDelegator, sourceFile, targetFile
                            .getBytesProcessed()); OutputStream targetStream = YADEFileStreamHelper.getTargetOutputStream(config, targetDelegator,
                                    targetFile, targetIsAppendEnabled, compressTarget)) {
                        if (targetStream == null) {
                            throw new YADEEngineTargetOutputStreamException(
                                    "Failed to obtain OutputStream from Target Provider: target stream is null.");
                        }

                        if (useCumulativeTargetFile && !isCumulateTargetWritten) {
                            byte[] bytes = cumulativeTargetFileSeparator.getBytes();
                            // cumulativeFileSeperatorLength = bytes.length;
                            targetStream.write(bytes);

                            YADEChecksumFileHelper.updateMessageDigest(sourceMessageDigest, bytes, false);
                            YADEChecksumFileHelper.updateMessageDigest(targetMessageDigest, bytes, compressTarget);
                            isCumulateTargetWritten = true;
                        }

                        if (sourceFile.getSize() <= 0L) {
                            byte[] bytes = new byte[0];
                            targetStream.write(bytes);

                            YADEChecksumFileHelper.updateMessageDigest(sourceMessageDigest, bytes, false);
                            YADEChecksumFileHelper.updateMessageDigest(targetMessageDigest, bytes, compressTarget);
                        } else {
                            byte[] buffer = new byte[config.getBufferSize()];
                            int bytesRead;
                            while ((bytesRead = sourceStream.read(buffer)) != -1) {
                                targetStream.write(buffer, 0, bytesRead);
                                targetFile.updateBytesProcessed(bytesRead);

                                YADEChecksumFileHelper.updateMessageDigest(sourceMessageDigest, buffer, bytesRead, false);
                                YADEChecksumFileHelper.updateMessageDigest(targetMessageDigest, buffer, bytesRead, compressTarget);
                            }
                        }
                        YADEFileStreamHelper.finishTargetOutputStream(logger, targetFile, targetStream, compressTarget);
                        break l;
                    } catch (Exception e) {
                        attempts++;
                        exception = e;
                    }
                    // handle exception only after all streams have been closed by try-with-resources
                    if (exception != null) {
                        handleException(fileTransferLogPrefix, targetFile, exception, attempts, false);
                    }

                }

                if (attempts > 0) {
                    targetFile = retryHelper.afterTransfer(logger, targetDelegator, targetFile);
                }
                YADEFileActionsExecuter.finalizeTargetFileSize(targetDelegator, targetFile, compressTarget);
            }

            targetFile.setState(TransferEntryState.TRANSFERRED);
            // renamed based on ReplaceWhat...
            String renamed = "";
            if (targetFile.isNameReplaced()) {
                targetFile.setSubState(TransferEntryState.RENAMED);
                // - the Target file is always renamed during transfer
                // -- logged by default as [renamed]
                // - if the Source file should also be renamed:
                // -- the Source file renaming will take place later
                // -- therefore - which file has already been renamed should be clearly visible here
                // --- logged as [target renamed]
                String add = config.getSource().isReplacementEnabled() ? "target " : "";
                renamed = "[" + add + YADEClientBannerWriter.formatState(TransferEntryState.RENAMED) + "]";
            }
            logger.info("[%s][%s]%s[%s=%s][%s=%s][Bytes=%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile.getState()),
                    renamed, sourceDelegator.getLabel(), sourceFile.getFullPath(), targetDelegator.getLabel(), targetFile.getFullPath(), targetFile
                            .getSize(), SOSDate.getDuration(startTime, Instant.now()));

            YADEFileActionsExecuter.checkTargetFileSize(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile);
            YADEChecksumFileHelper.checkSourceIntegrityHash(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile,
                    sourceMessageDigest);
            YADEChecksumFileHelper.setTargetIntegrityHash(sourceFile, targetMessageDigest);

            finalizeIfNonTransactional(isMoveOperation, useLastModified, fileTransferLogPrefix);
        } catch (YADEEngineTransferFileException e) {
            throw e;
        } catch (Exception e) {
            throwException(fileTransferLogPrefix, targetFile, e, "");
        }
    }

    private void finalizeIfNonTransactional(boolean isMoveOperation, boolean useLastModified, String fileTransferLogPrefix) throws Exception {
        if (!config.isTransactionalEnabled() && (isMoveOperation || config.getSource().needsFilePostProcessing() || config.getTarget()
                .needsFilePostProcessing())) {
            // If NOT Transactional
            // - MOVE operations - remove source file
            // - Source: Replacement if enabled, Commands AfterFile/BeforeRename
            // - Target:
            // -- if not skipped - Replacement/Rename(Atomic) if enabled, IntergityHash, KeepLastModifiedDate, Commands AfterFile/BeforeRename
            // -- if skipped - Commands AfterFile
            if (isMoveOperation) {
                if (!sourceDelegator.isJumpHost()) {
                    sourceFile.setState(TransferEntryState.MOVED);
                    if (sourceDelegator.getProvider().deleteFileIfExists(sourceFile.getFullPath())) {
                        logger.info("[%s][%s][%s/deleted]%s", fileTransferLogPrefix, sourceDelegator.getLabel(), YADEClientBannerWriter.formatState(
                                sourceFile.getState()), sourceFile.getFullPath());
                    }
                }
            }
            YADEFileActionsExecuter.postProcessingOnSuccess(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile,
                    config.getTarget().getAtomic() != null, useLastModified);
        }
    }

    private void prepareSource(String fileTransferLogPrefix) throws YADEEngineTransferFileException {
        sourceFile.resetSteady();

        try {
            YADEFileNameInfo fileNameInfo = getSourceFinalFilePathInfo();
            sourceFile.setNameReplaced(fileNameInfo.isReplaced());
            if (sourceFile.isNameReplaced()) {
                sourceFile.setFinalFullPath(sourceDelegator, fileNameInfo.getName());
            }
        } catch (Exception e) {
            throwExceptionOnSource(fileTransferLogPrefix, e);
        }
    }

    private void initializeTarget() throws ProviderException, YADEEngineInvalidExpressionException {
        YADEFileNameInfo fileNameInfo = getTargetFinalFilePathInfo();

        /** finalFileName: the final name of the file after transfer (compressed/replaced name...) */
        String finalFileName = fileNameInfo.getName();
        if (sourceDelegator.isHTTP()) {
            // e.g. for HTTP(s) transfers with the file names like SET-217?filter=13400
            if (!targetDelegator.isHTTP()) {
                finalFileName = HttpUtils.toValidFileSystemName(finalFileName, targetDelegator.isWindows());
            }
        }

        /** transferFileName: file name during transfer - same path as finalFileName but can contains the atomic prefix/suffix */
        String transferFileName = finalFileName;

        if (config.getTarget().getAtomic() != null) {
            transferFileName = config.getTarget().getAtomic().getPrefix() + finalFileName + config.getTarget().getAtomic().getSuffix();
        }
        String targetDirectory = sourceDelegator.getDirectoryMapper().getTargetDirectory(logger, config, sourceDelegator, targetDelegator, sourceFile,
                fileNameInfo);
        String transferFileFullPath = targetDelegator.appendPath(targetDirectory, transferFileName);

        String httpOriginalParentFullPath = null;
        if (targetDelegator.isHTTP()) {
            // without base URI because of possible double encoding etc
            httpOriginalParentFullPath = targetDelegator.getParentPath(transferFileFullPath);
            // adds baseURI (+ encoding)
            // transferFileFullPath = targetDelegator.getProvider().normalizePath(transferFileFullPath);
        }
        YADETargetProviderFile target = new YADETargetProviderFile(targetDelegator, transferFileFullPath);
        /** the final path of the file after transfer */

        if (targetDelegator.isHTTP()) {
            String httpFinalPath = targetDelegator.appendPath(httpOriginalParentFullPath, finalFileName);
            // target.setFinalFullPath(targetDelegator.getProvider().normalizePath(httpFinalPath));
            target.setFinalFullPath(httpFinalPath);
        } else {
            target.setFinalFullPath(targetDelegator, finalFileName);
        }
        target.setIndex(sourceFile.getIndex());
        target.setNameReplaced(fileNameInfo.isReplaced());
        sourceFile.setTarget(target);
    }

    /** Returns the final name of the file after transfer<br/>
     * May contains a path separator and have a different path than the original path if target replacement is enabled
     * 
     * @param sourceFile
     * @param config
     * @return the final name of the file after transfer
     * @throws YADEEngineInvalidExpressionException */
    private YADEFileNameInfo getTargetFinalFilePathInfo() throws YADEEngineInvalidExpressionException {
        // 1) Source name
        String fileName = sourceFile.getName();
        // 2) Compressed name
        if (config.getTarget().getCompress() != null) {
            fileName = fileName + config.getTarget().getCompress().getFileExtension();
        }
        // 3) Replaced name
        YADEFileNameInfo info = null;
        // Note: possible replacement setting is disabled when cumulative file enabled
        if (config.getTarget().isReplacementEnabled()) {
            Optional<YADEFileNameInfo> newFileNameInfo = getReplacementResultIfDifferent(targetDelegator, sourceFile);
            if (newFileNameInfo.isPresent()) {
                info = newFileNameInfo.get();
            }
        }
        if (info == null) {
            info = new YADEFileNameInfo(targetDelegator, fileName, false);
        }
        return info;
    }

    private YADEFileNameInfo getSourceFinalFilePathInfo() throws YADEEngineInvalidExpressionException {
        // 1) Source name
        String fileName = sourceFile.getName();
        // 2) Replaced name
        YADEFileNameInfo info = null;
        if (config.getSource().isReplacementEnabled()) {
            Optional<YADEFileNameInfo> newFileNameInfo = getReplacementResultIfDifferent(sourceDelegator, sourceFile);
            if (newFileNameInfo.isPresent()) {
                info = newFileNameInfo.get();
            }
        }
        if (info == null) {
            info = new YADEFileNameInfo(sourceDelegator, fileName, false);
        }
        return info;
    }

    private static Optional<YADEFileNameInfo> getReplacementResultIfDifferent(AYADEProviderDelegator delegator, YADEProviderFile file)
            throws YADEEngineInvalidExpressionException {
        return YADEFileReplacementHelper.getReplacementResultIfDifferent(delegator, file.getName(), delegator.getArgs().getReplacing().getValue(),
                delegator.getArgs().getReplacement().getValue());
    }

    private void handleException(String fileTransferLogPrefix, YADETargetProviderFile targetFile, Throwable e, int attempts, boolean targetIsHTTP)
            throws YADEEngineException {

        if (!config.getRetry().isEnabled() || !YADEProviderDelegatorHelper.isSourceOrTargetNotConnected(sourceDelegator, targetDelegator)) {
            throwException(fileTransferLogPrefix, targetFile, e, "");
        }

        if (attempts > config.getRetry().getMaxRetries()) { // > because attempt increased before retry
            String add = "";
            if (config.getRetry().getMaxRetries() > 1) {
                add = "[Maximum Retry attempts=" + config.getRetry().getMaxRetries() + " reached]";
            }
            throwException(fileTransferLogPrefix, targetFile, e, add);
        } else {
            String targetBytesProcessed = targetFile.getBytesProcessed() + "";
            if (targetIsHTTP && targetFile.getBytesProcessed() == 0) {
                targetBytesProcessed = "unknown";
            }
            String msg = String.format("[%s][%s][Retry " + attempts + "/" + config.getRetry().getMaxRetries() + " starts in " + config.getRetry()
                    .getInterval() + "s][%s=%s][%s][%s][Bytes(processed)=%s/%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(
                            targetFile.getState()), sourceDelegator.getLabel(), sourceFile.getFullPath(), targetDelegator.getLabel(), targetFile
                                    .getFullPath(), targetBytesProcessed, sourceFile.getSize(), "due to " + e);
            logger.info(msg);

            YADEClientHelper.waitFor(config.getRetry().getInterval());

            boolean sourceConnected = false;
            boolean targetConnected = false;

            // execute ensureConnected only once
            RetryOnConnectionError ensureConnectedRetry = config.getRetry().createNotEnabledInstance();
            String action = YADEClientBannerWriter.formatState(targetFile.getState());
            try {
                YADEProviderDelegatorHelper.ensureConnected(logger, sourceDelegator, action, ensureConnectedRetry);
                sourceConnected = true;
            } catch (Exception ex) {
                logger.info("[%s][Retry %s failed]%s", sourceDelegator.getLabel(), attempts, e.toString());
            }
            try {
                YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator, action, ensureConnectedRetry);
                targetConnected = true;
            } catch (Exception ex) {
                logger.info("[%s][Retry %s failed]%s", targetDelegator.getLabel(), attempts, e.toString());
            }
            if (sourceConnected && targetConnected) {
                YADECopyMoveOperationsHandler.handleReusableResourcesBeforeTransfer(config, sourceDelegator, targetDelegator);
            }

        }
    }

    /** TODO new provider method
     * 
     * @param e
     * @return */
    @SuppressWarnings("unused")
    private static boolean isConnectionException(Throwable cause) {
        if (cause == null) {
            return false;
        }
        Throwable e = cause;
        while (e != null) {
            try {
                // e.g.: net.schmizz.sshj.connection.ConnectionException:
                if (e.getClass().getName().toLowerCase().contains("connection")) {
                    return true;
                    // } else if (e instanceof YADEEngineConnectionException) {
                    // return true;
                } else if (e instanceof ProviderConnectException) {
                    return true;
                }
            } catch (Exception ex) {
            }
            e = e.getCause();
        }
        return false;
    }

    private void throwException(String fileTransferLogPrefix, YADETargetProviderFile targetFile, Throwable e, String throwExceptionAdd)
            throws YADEEngineTransferFileException {
        String target = "null";
        if (targetFile != null) {
            target = targetFile.getCurrentFullPath();
            if (targetFile.isTransferring()) {
                target = target + "][Bytes(processed)=" + targetFile.getBytesProcessed() + "/" + sourceFile.getSize();
            }
        }
        String msg = String.format("[%s][%s][%s=%s][%s][%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile.getState()),
                sourceDelegator.getLabel(), sourceFile.getFullPath(), targetDelegator.getLabel(), target, throwExceptionAdd + e);
        logger.error(msg);
        if (logger.isTraceEnabled()) {
            logger.trace("  [StackTrace]" + SOSClassUtil.getStackTrace(e));
        }
        throw new YADEEngineTransferFileException(msg, e.getCause() == null ? e : e.getCause());
    }

    private void throwExceptionOnSource(String fileTransferLogPrefix, Throwable e) throws YADEEngineTransferFileException {
        String msg = String.format("[%s][%s=%s]%s", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath(), e);
        logger.error(msg);
        if (logger.isTraceEnabled()) {
            logger.trace("  [StackTrace]" + SOSClassUtil.getStackTrace(e));
        }
        throw new YADEEngineTransferFileException(msg, e);
    }
}
