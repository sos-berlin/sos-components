package com.sos.yade.engine.handlers.operations.copymove.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSHTTPUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.ftp.FTPProvider;
import com.sos.commons.vfs.http.HTTPProvider;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.commons.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.exceptions.YADEEngineException;
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

/** Single "transfer" file manager */
public class YADEFileHandler {

    // SSH (buffer_size=32KB): 4.5GB ~ 1.15 minutes, 1.5 GB ~ 25 seconds
    private static final long LOG_TRANSFER_START_IF_FILESIZE_GREATER_THAN = 3221225475L;// 3GB
    private static final long USE_BUFFERED_STREAMS_IF_FILESIZE_GREATER_THAN = 10485760L;// 10 MB

    private final ISOSLogger logger;
    private final YADECopyMoveOperationsConfig config;
    private final YADESourceProviderDelegator sourceDelegator;
    private final YADETargetProviderDelegator targetDelegator;
    private final YADEProviderFile sourceFile;
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

    public void run(boolean isMoveOperation, boolean useCumulativeTargetFile) throws YADEEngineTransferFileException {
        this.sourceFile.resetSteady();

        // 'index' or 'index][thread name'
        String fileTransferLogPrefix = config.getParallelism() == 1 ? String.valueOf(sourceFile.getIndex()) : sourceFile.getIndex() + "][" + Thread
                .currentThread().getName();
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
                    if (targetDelegator.getProvider().exists(targetFile.getFinalFullPath())) {
                        targetFile.setState(TransferEntryState.NOT_OVERWRITTEN);
                        logger.info("[%s][%s][%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile.getState()),
                                targetDelegator.getLabel(), targetFile.getFinalFullPath());

                        YADECommandExecutor.executeBeforeFile(logger, sourceDelegator, targetDelegator, sourceFile);
                        return;
                    }
                }
            }

            // 2) Source/Target: commands before file transfer
            YADECommandExecutor.executeBeforeFile(logger, sourceDelegator, targetDelegator, sourceFile);
            targetFile.setState(TransferEntryState.TRANSFERRING);
            // TODO config.getParallelMaxThreads() == 1 - make it sense if parallel because of random order?
            if (config.getParallelism() == 1 && sourceFile.getSize() >= LOG_TRANSFER_START_IF_FILESIZE_GREATER_THAN) {
                logger.info("[%s][%s][%s,bytes=%s][%s][%s]start...", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath(),
                        sourceFile.getSize(), targetDelegator.getLabel(), targetFile.getFullPath());
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug("[%s][%s][%s,bytes=%s][%s][%s]start...", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath(),
                            sourceFile.getSize(), targetDelegator.getLabel(), targetFile.getFullPath());
                }
            }

            // not compress if cumulative file
            boolean compressTarget = config.getTarget().getCompress() != null && !useCumulativeTargetFile;
            MessageDigest sourceMessageDigest = YADEChecksumFileHelper.initializeMessageDigest(config, config.getSource()
                    .isCheckIntegrityHashEnabled());
            MessageDigest targetMessageDigest = YADEChecksumFileHelper.initializeMessageDigest(config, config.getTarget()
                    .isCreateIntegrityHashFileEnabled());

            boolean useBufferedStreams = sourceFile.getSize() > USE_BUFFERED_STREAMS_IF_FILESIZE_GREATER_THAN ? true : false;

            int attempts = 0;
            boolean isCumulateTargetWritten = false;
            // int cumulativeFileSeperatorLength = 0;

            Instant startTime = Instant.now();
            if (targetDelegator.isHTTP()) {
                // TODO compressing, cumulative, messageDigest, skipSourceInputStreamToPosition ...
                l: while (attempts < config.getMaxRetries()) {
                    try (InputStream sourceStream = YADEFileStreamHelper.getSourceInputStream(config, sourceDelegator, sourceFile,
                            useBufferedStreams)) {
                        targetFile.setSize(((HTTPProvider) targetDelegator.getProvider()).upload(targetFile.getFullPath(), sourceStream, sourceFile
                                .getSize()));
                        break l;
                    } catch (Throwable e) {
                        attempts++;
                        handleException(fileTransferLogPrefix, targetFile, e, attempts);
                    }
                }
            } else {
                l: while (attempts < config.getMaxRetries()) {
                    // cumulativeFileSeperatorLength = 0;
                    Throwable exception = null;
                    try (InputStream sourceStream = YADEFileStreamHelper.getSourceInputStream(config, sourceDelegator, sourceFile,
                            useBufferedStreams); OutputStream targetOutputStream = YADEFileStreamHelper.getTargetOutputStream(config, targetDelegator,
                                    targetFile, useBufferedStreams); OutputStream targetStream = compressTarget ? new GZIPOutputStream(
                                            targetOutputStream) : targetOutputStream) {
                        if (targetStream == null) {
                            throw new YADEEngineTargetOutputStreamException(
                                    "Failed to obtain OutputStream from Target Provider: target stream is null.");
                        }

                        if (attempts > 0) {
                            YADEFileStreamHelper.skipSourceInputStreamToPosition(sourceStream, targetFile);
                            // if skip is not used - targetFile.getBytesProcessed() should be reset
                        }

                        if (useCumulativeTargetFile && !isCumulateTargetWritten) {
                            byte[] bytes = cumulativeTargetFileSeparator.getBytes();
                            // cumulativeFileSeperatorLength = bytes.length;
                            targetOutputStream.write(bytes);

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
                                targetFile.updateProgressSize(bytesRead);

                                YADEChecksumFileHelper.updateMessageDigest(sourceMessageDigest, buffer, bytesRead, false);
                                YADEChecksumFileHelper.updateMessageDigest(targetMessageDigest, buffer, bytesRead, compressTarget);
                            }
                            // YADEFileStreamHelper.finishTargetOutputStream(logger, targetFile, targetStream, compressTarget);
                        }
                        YADEFileStreamHelper.finishTargetOutputStream(logger, targetFile, targetStream, compressTarget);
                        break l;
                    } catch (Throwable e) {
                        exception = e;
                        attempts++;
                        handleException(fileTransferLogPrefix, targetFile, e, attempts);
                    } finally {
                        try {
                            YADEFileStreamHelper.onStreamsClosed(logger, sourceDelegator, sourceFile, targetDelegator, targetFile);
                        } finally {
                            tryCleanupIfFailed(fileTransferLogPrefix, targetFile, exception);
                        }
                    }
                }
                YADEFileActionsExecuter.finalizeTargetFileSize(targetDelegator, targetFile, compressTarget);
            }

            targetFile.setState(TransferEntryState.TRANSFERRED);
            // renamed based on ReplaceWhat...
            String renamed = targetFile.isNameReplaced() ? "[" + YADEClientBannerWriter.formatState(TransferEntryState.RENAMED) + "]" : "";
            logger.info("[%s][%s]%s[%s=%s][%s=%s][Bytes=%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile.getState()),
                    renamed, sourceDelegator.getLabel(), sourceFile.getFullPath(), targetDelegator.getLabel(), targetFile.getFullPath(), targetFile
                            .getSize(), SOSDate.getDuration(startTime, Instant.now()));

            YADEFileActionsExecuter.checkTargetFileSize(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile);
            YADEChecksumFileHelper.checkSourceIntegrityHash(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile,
                    sourceMessageDigest);
            YADEChecksumFileHelper.setTargetIntegrityHash(sourceFile, targetMessageDigest);

            if (!config.isTransactionalEnabled() && (isMoveOperation || config.getSource().needsFilePostProcessing() || config.getTarget()
                    .needsFilePostProcessing())) {
                // If NOT Transactional
                // - MOVE operations - remove source file
                // - Source - Replacement if enabled, Commands AfterFile/BeforeRename
                // - Target - Replacement/Rename(Atomic) if enabled, IntergityHash, KeepLastModifiedDate, Commands AfterFile/BeforeRename
                if (isMoveOperation) {
                    if (!sourceDelegator.isJumpHost()) {
                        if (sourceDelegator.getProvider().deleteFileIfExists(sourceFile.getFullPath())) {
                            logger.info("[%s][%s][%s]deleted", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath());
                        }
                        sourceFile.setState(TransferEntryState.MOVED);
                    }

                }
                YADEFileActionsExecuter.postProcessingOnSuccess(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile,
                        config.getTarget().getAtomic() != null);
            }
        } catch (YADEEngineTransferFileException e) {
            throw e;
        } catch (Throwable e) {
            throwException(fileTransferLogPrefix, targetFile, e, "");
        }
    }

    public void initializeTarget() throws ProviderException {
        YADEFileNameInfo fileNameInfo = getTargetFinalFilePathInfo();

        /** finalFileName: the final name of the file after transfer (compressed/replaced name...) */
        String finalFileName = fileNameInfo.getName();
        if (sourceDelegator.isHTTP()) {
            // e.g. for HTTP(s) transfers with the file names like SET-217?filter=13400
            if (!targetDelegator.isHTTP()) {
                finalFileName = SOSHTTPUtils.toValidFileSystemName(finalFileName, targetDelegator.isWindows());
            }
        }

        /** transferFileName: file name during transfer - same path as finalFileName but can contains the atomic prefix/suffix */
        String transferFileName = finalFileName;

        if (config.getTarget().getAtomic() != null) {
            transferFileName = config.getTarget().getAtomic().getPrefix() + finalFileName + config.getTarget().getAtomic().getSuffix();
        }
        String targetDirectory = sourceDelegator.getDirectoryMapper().getTargetDirectory(logger, config, targetDelegator, sourceFile, fileNameInfo);
        String transferFileFullPath = targetDelegator.appendPath(targetDirectory, transferFileName);

        String httpOriginalParentFullPath = null;
        if (targetDelegator.isHTTP()) {
            // without base URI because of possible double encoding etc
            httpOriginalParentFullPath = targetDelegator.getParentPath(transferFileFullPath);
            // adds baseURI (+ encoding)
            transferFileFullPath = targetDelegator.getProvider().normalizePath(transferFileFullPath);
        }
        YADETargetProviderFile target = new YADETargetProviderFile(targetDelegator, transferFileFullPath);
        /** the final path of the file after transfer */

        if (targetDelegator.isHTTP()) {
            String httpFinalPath = targetDelegator.appendPath(httpOriginalParentFullPath, finalFileName);
            target.setFinalFullPath(targetDelegator.getProvider().normalizePath(httpFinalPath));
        } else {
            target.setFinalFullPath(targetDelegator, finalFileName);
        }
        target.setIndex(sourceFile.getIndex());
        target.setNameReplaced(fileNameInfo.isReplaced());
        sourceFile.setTarget(target);
    }

    public static Optional<YADEFileNameInfo> getReplacementResultIfDifferent(AYADEProviderDelegator delegator, YADEProviderFile file) {
        return YADEFileReplacementHelper.getReplacementResultIfDifferent(delegator, file.getName(), delegator.getArgs().getReplacing().getValue(),
                delegator.getArgs().getReplacement().getValue());
    }

    public static String getFinalFullPath(AYADEProviderDelegator delegator, YADEProviderFile file, YADEFileNameInfo newNameInfo) {
        if (newNameInfo.isAbsolutePath()) {
            return newNameInfo.getPath();
        } else {
            String finalFullPath = file.getParentFullPath();
            if (newNameInfo.needsParent()) {
                finalFullPath = delegator.appendPath(finalFullPath, newNameInfo.getParent());
            }
            return delegator.appendPath(finalFullPath, newNameInfo.getName());
        }
    }

    /** Returns the final name of the file after transfer<br/>
     * May contains a path separator and have a different path than the original path if target replacement is enabled
     * 
     * @param sourceFile
     * @param config
     * @return the final name of the file after transfer */
    private YADEFileNameInfo getTargetFinalFilePathInfo() {
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

    private void handleException(String fileTransferLogPrefix, YADETargetProviderFile targetFile, Throwable e, int attempts)
            throws YADEEngineException {
        boolean throwException = false;
        String throwExceptionAdd = "";
        if (YADEProviderDelegatorHelper.isSourceOrTargetNotConnected(sourceDelegator, targetDelegator)) {
            if (attempts >= config.getMaxRetries()) {
                throwException = true;
                if (config.getMaxRetries() > 1) {
                    throwExceptionAdd = "[maximum retry attempts=" + config.getMaxRetries() + " reached]";
                }
            } else {
                YADEProviderDelegatorHelper.ensureConnected(logger, sourceDelegator);
                YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator);

                YADECopyMoveOperationsHandler.handleReusableResourcesBeforeTransfer(config, sourceDelegator, targetDelegator);
            }
        } else {
            throwException = true;
        }
        if (throwException) {
            throwException(fileTransferLogPrefix, targetFile, e, throwExceptionAdd);
        } else {
            String msg = String.format("[%s][%s][%s=%s][%s][%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile.getState()),
                    sourceDelegator.getLabel(), sourceFile.getFullPath(), targetDelegator.getLabel(), targetFile.getFullPath(), throwExceptionAdd
                            + e);
            logger.warn(msg);
        }
    }

    private void tryCleanupIfFailed(String fileTransferLogPrefix, YADETargetProviderFile targetFile, Throwable e) throws YADEEngineException {
        if (e == null) {
            return;
        }
        // if (e instanceof YADEEngineTargetOutputStreamException) {
        String msg = String.format("[tryCleanupIfFailed][%s][%s][%s][%s]", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile
                .getState()), targetDelegator.getLabel(), targetFile.getFullPath());
        try {
            if (!targetDelegator.getProvider().isConnected()) {
                if (targetDelegator.getProvider() instanceof FTPProvider) {
                    FTPProvider ftp = (FTPProvider) targetDelegator.getProvider();
                    if (!ftp.getArguments().isPassiveMode()) {
                        ftp.getArguments().getPassiveMode().setValue(true);
                        ftp.ensureConnected();
                        // ftp.getArguments().getPassiveMode().setValue(false);
                    }
                }
            }

            if (targetDelegator.getProvider().isConnected()) {
                if (targetDelegator.getProvider().deleteFileIfExists(targetFile.getFullPath())) {
                    targetFile.setState(TransferEntryState.ROLLED_BACK);
                    msg = String.format("[%s][%s][%s][%s]deleted", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile.getState()),
                            targetDelegator.getLabel(), targetFile.getFullPath());
                    logger.info(msg);
                } else {
                    logger.info(msg + "not found");
                }
            } else {
                logger.info(msg + "not connected");
            }
        } catch (Exception ex) {
            logger.warn(msg + ex, ex);
        }
        // }

    }

    private void throwException(String fileTransferLogPrefix, YADETargetProviderFile targetFile, Throwable e, String throwExceptionAdd)
            throws YADEEngineTransferFileException {
        String target = targetFile == null ? "null" : targetFile.getFullPath();
        String msg = String.format("[%s][%s=%s][%s][%s]%s", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath(),
                targetDelegator.getLabel(), target, throwExceptionAdd + e);
        logger.error(msg);
        if (logger.isTraceEnabled()) {
            logger.trace("  [StackTrace]" + SOSClassUtil.getStackTrace(e));
        }
        throw new YADEEngineTransferFileException(msg, e);
    }

}
