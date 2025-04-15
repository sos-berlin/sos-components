package com.sos.yade.engine.handlers.operations.copymove.file;

import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.zip.GZIPOutputStream;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSHTTPUtils;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.exceptions.ProviderException;
import com.sos.commons.vfs.http.HTTPProvider;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.delegators.AYADEProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.commons.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.exceptions.YADEEngineException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileException;
import com.sos.yade.engine.handlers.command.YADECommandExecutor;
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

    public void run(boolean useCumulativeTargetFile) throws YADEEngineTransferFileException {
        this.sourceFile.resetSteady();

        // 'index' or 'index][thread name'
        String fileTransferLogPrefix = config.getParallelism() == 1 ? String.valueOf(sourceFile.getIndex()) : sourceFile.getIndex() + "][" + Thread
                .currentThread().getName();
        YADETargetProviderFile targetFile = null;
        try {
            // 1) Target - initialize/get Target file
            if (useCumulativeTargetFile) {
                targetFile = config.getTarget().getCumulate().getFile();
            } else {
                // 1) Target: may create target directories if target replacement enabled
                initializeTarget();
                targetFile = (YADETargetProviderFile) sourceFile.getTarget();

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
                    // int cumulativeFileSeperatorLength = 0;
                    try (InputStream sourceStream = YADEFileStreamHelper.getSourceInputStream(config, sourceDelegator, sourceFile,
                            useBufferedStreams); OutputStream targetOutputStream = YADEFileStreamHelper.getTargetOutputStream(config, targetDelegator,
                                    targetFile, useBufferedStreams); OutputStream targetStream = compressTarget ? new GZIPOutputStream(
                                            targetOutputStream) : targetOutputStream) {
                        if (attempts > 0) {
                            YADEFileStreamHelper.skipSourceInputStreamToPosition(sourceStream, targetFile);
                            // if skip is not used - targetFile.getBytesProcessed() should be reset
                        }

                        if (useCumulativeTargetFile && !isCumulateTargetWritten) {
                            // TODO replace variables .... XML Schema description for CumulativeFileSeparator is wrong
                            String fs = config.getTarget().getCumulate().getFileSeparator() + System.getProperty("line.separator");
                            byte[] bytes = fs.getBytes();
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
                        attempts++;
                        handleException(fileTransferLogPrefix, targetFile, e, attempts);
                    } finally {
                        YADEFileStreamHelper.onStreamsClosed(logger, sourceDelegator, sourceFile, targetDelegator, targetFile);
                    }
                }
                YADEFileActionsExecuter.finalizeTargetFileSize(targetDelegator, targetFile, compressTarget);
            }

            targetFile.setState(TransferEntryState.TRANSFERRED);
            logger.info("[%s][%s][%s=%s][%s=%s][bytes=%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(targetFile.getState()),
                    sourceDelegator.getLabel(), sourceFile.getFullPath(), targetDelegator.getLabel(), targetFile.getFullPath(), targetFile.getSize(),
                    SOSDate.getDuration(startTime, Instant.now()));

            YADEFileActionsExecuter.checkTargetFileSize(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile);
            YADEChecksumFileHelper.checkSourceIntegrityHash(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile,
                    sourceMessageDigest);
            YADEChecksumFileHelper.setTargetIntegrityHash(sourceFile, targetMessageDigest);

            if (!config.isTransactionalEnabled() && (config.isMoveOperation() || config.getSource().needsFilePostProcessing() || config.getTarget()
                    .needsFilePostProcessing())) {
                // If NOT Transactional
                // - MOVE operations - remove source file
                // - Source - Replacement if enabled, Commands AfterFile/BeforeRename
                // - Target - Replacement/Rename(Atomic) if enabled, IntergityHash, KeepLastModifiedDate, Commands AfterFile/BeforeRename
                if (config.isMoveOperation()) {
                    if (sourceDelegator.getProvider().deleteIfExists(sourceFile.getFullPath())) {
                        logger.info("[%s][%s][%s]deleted", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath());
                    }
                    sourceFile.setState(TransferEntryState.MOVED);
                }
                YADEFileActionsExecuter.postProcessingOnSuccess(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile);
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
        /** transferFileName: file name during transfer - same path as finalFileName but can contains the atomic prefix/suffix */
        String transferFileName = finalFileName;

        if (config.getTarget().getAtomic() != null) {
            transferFileName = config.getTarget().getAtomic().getPrefix() + finalFileName + config.getTarget().getAtomic().getSuffix();
        }
        String targetDirectory = sourceDelegator.getDirectoryMapper().getTargetDirectory(logger, config, targetDelegator, sourceFile, fileNameInfo);
        String transferFileFullPath = targetDelegator.appendPath(targetDirectory, transferFileName);
        YADETargetProviderFile target = new YADETargetProviderFile(targetDelegator, transferFileFullPath);
        /** the final path of the file after transfer */
        target.setFinalFullPath(targetDelegator, finalFileName);
        target.setIndex(sourceFile.getIndex());
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

        if (sourceDelegator.isHTTP()) {
            // e.g. for HTTP(s) transfers with the file names like SET-217?filter=13400
            fileName = SOSHTTPUtils.toValidFileSystemName(fileName);
        }

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
            info = new YADEFileNameInfo(targetDelegator, fileName);
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
            String msg = String.format("[%s][%s=%s][%s][%s]%s", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath(),
                    targetDelegator.getLabel(), targetFile.getFullPath(), throwExceptionAdd + e);
            logger.warn(msg);
        }
    }

    private void throwException(String fileTransferLogPrefix, YADETargetProviderFile targetFile, Throwable e, String throwExceptionAdd)
            throws YADEEngineTransferFileException {
        String msg = String.format("[%s][%s=%s][%s][%s]%s", fileTransferLogPrefix, sourceDelegator.getLabel(), sourceFile.getFullPath(),
                targetDelegator.getLabel(), targetFile.getFullPath(), throwExceptionAdd + e);
        logger.error(msg);
        throw new YADEEngineTransferFileException(msg, e);
    }

}
