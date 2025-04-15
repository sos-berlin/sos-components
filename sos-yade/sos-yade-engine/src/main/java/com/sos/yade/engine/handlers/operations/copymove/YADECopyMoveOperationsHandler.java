package com.sos.yade.engine.handlers.operations.copymove;

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.commons.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.exceptions.YADEEngineOperationException;
import com.sos.yade.engine.exceptions.YADEEngineTransferFileException;
import com.sos.yade.engine.handlers.operations.copymove.file.YADEFileHandler;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADEFileActionsExecuter;
import com.sos.yade.engine.handlers.operations.copymove.file.helpers.YADETargetCumulativeFileHelper;

// Target File
// - TransferEntryState.NOT_OVERWRITTEN
// - TransferEntryState.TRANSFERRING
// - TransferEntryState.TRANSFERRED
// - TransferEntryState.ROLLED_BACK <- (Target file deleted) checkTargetFileSize, checkChecksum
// - targetFile.setSubState(TransferEntryState.RENAMED); after transfer
// Source File
// - TransferEntryState.ROLLED_BACK <- on rollback - setted on the sourceFile if the Target file was not initialized
// - sourceFile.setState(TransferEntryState.MOVED); after transfer - after source file deleted
public class YADECopyMoveOperationsHandler {

    public static void process(TransferOperation operation, ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, AtomicBoolean cancel) throws Exception {
        if (targetDelegator == null) {
            throw new YADEEngineOperationException("TargetDelegator is required but missing");
        }

        // 1) Source/Target: initialize transfer configuration(cumulative file,compress,atomic etc.)
        YADECopyMoveOperationsConfig config = new YADECopyMoveOperationsConfig(operation, args, sourceDelegator, targetDelegator, sourceFiles.size());

        // 2) Target: map the source to the target directories and try to create all target directories before individual file transfer
        // - all target directories are only evaluated if target replacement is not enabled,
        // -- otherwise the target directories are evaluated/created on every file
        sourceDelegator.getDirectoryMapper().tryCreateAllTargetDirectoriesBeforeOperation(logger, config, targetDelegator);

        // 3) Source/Target: Transfer files
        boolean isMoveOperation = config.isMoveOperation();
        boolean useCumulativeTargetFile = config.getTarget().getCumulate() != null;
        try {
            processFiles(logger, config, sourceDelegator, targetDelegator, sourceFiles, isMoveOperation, useCumulativeTargetFile, cancel);
            finalizeTransactionIfNeeded(logger, config, sourceDelegator, targetDelegator, sourceFiles, isMoveOperation, useCumulativeTargetFile,
                    cancel);
        } catch (Throwable e) {
            // rollbackIfTransactional - does not throws exception
            rollbackTransactionIfNeeded(logger, config, sourceDelegator, targetDelegator, sourceFiles, useCumulativeTargetFile);
            throw e;
        }
    }

    public static void handleReusableResourcesBeforeTransfer(YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator) {

        if (!config.processFilesSequentially()) {
            // preparing providers for multi-threading
            sourceDelegator.getProvider().disableReusableResource();
            targetDelegator.getProvider().disableReusableResource();
        }
    }

    private static void processFiles(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, boolean isMoveOperation, boolean useCumulativeTargetFile,
            AtomicBoolean cancel) throws Exception {
        try {
            if (config.processFilesSequentially()) {
                processFilesSequentially(logger, config, sourceDelegator, targetDelegator, sourceFiles, isMoveOperation, useCumulativeTargetFile,
                        cancel);
            } else {
                processFilesInParallel(logger, config, sourceDelegator, targetDelegator, sourceFiles, isMoveOperation, cancel);
            }
        } catch (Throwable e) {
            throw e;
        } finally {
            // re-connect in any case - e.g. for rollback, after operation commands...
            YADEProviderDelegatorHelper.ensureConnected(logger, sourceDelegator);
            YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator);
        }
    }

    // cumulative file is not processed here - only sequential processing
    private static void processFilesInParallel(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, boolean isMoveOperation, AtomicBoolean cancel)
            throws Exception {
        int maxThreads = config.getParallelism();
        int size = sourceFiles.size();
        if (size < maxThreads) {
            maxThreads = size;
        }

        handleReusableResourcesBeforeTransfer(config, sourceDelegator, targetDelegator);

        // custom ForkJoinPool & parallelStream because this combination:
        // - allows control over the number of threads created
        // - blocks the main thread until all tasks are completed
        // - does not increase memory usage (compared to using a Future list callback in case of a large number of files)
        ForkJoinPool threadPool = new ForkJoinPool(maxThreads, new ForkJoinPool.ForkJoinWorkerThreadFactory() {

            private int count = 1;

            @Override
            public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
                ForkJoinWorkerThread thread = ForkJoinPool.defaultForkJoinWorkerThreadFactory.newThread(pool);
                thread.setName("yade-thread-" + (count++));
                return thread;
            }

        }, null, false);
        try {
            threadPool.submit(() -> {
                sourceFiles.parallelStream().forEach(pf -> {
                    if (cancel.get()) {
                        return;
                    }
                    YADEProviderFile f = (YADEProviderFile) pf;
                    try {
                        // useCumulativeTargetFile=false for transfers in parallel
                        new YADEFileHandler(logger, config, sourceDelegator, targetDelegator, f, cancel).run(isMoveOperation, false);
                    } catch (Exception e) {
                        if (config.isTransactionalEnabled()) {
                            cancel.set(true);
                            throw new RuntimeException(e);
                        }
                        setFailedIfNonTransactional(f);
                    }
                });

            }).join();
        } catch (Exception e) {
            throw new YADEEngineOperationException(getTransferFileException(e.getCause()));
        } finally {
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }
    }

    private static void processFilesSequentially(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, boolean isMoveOperation, boolean useCumulativeTargetFile,
            AtomicBoolean cancel) throws Exception {

        if (useCumulativeTargetFile) {
            YADETargetCumulativeFileHelper.tryDeleteFile(logger, config, targetDelegator);
        }

        for (ProviderFile sourceFile : sourceFiles) {
            if (cancel.get()) {
                return;
            }
            YADEProviderFile f = (YADEProviderFile) sourceFile;
            try {
                new YADEFileHandler(logger, config, sourceDelegator, targetDelegator, f, cancel).run(isMoveOperation, useCumulativeTargetFile);
            } catch (Throwable e) {
                if (config.isTransactionalEnabled()) {
                    cancel.set(true);
                    throw e;
                }
                setFailedIfNonTransactional(f);
            }
        }

        if (useCumulativeTargetFile) {
            YADETargetCumulativeFileHelper.onSuccess(logger, config, targetDelegator);
        }
    }

    private static void finalizeTransactionIfNeeded(ISOSLogger logger, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles,
            boolean isMoveOperation, boolean useCumulativeTargetFile, AtomicBoolean cancel) throws Exception {
        if (!config.isTransactionalEnabled()) {
            return;
        }
        if (!config.getSource().needsFilePostProcessing() && !config.getTarget().needsFilePostProcessing()) {
            return;
        }
        logger.info(YADEClientBannerWriter.SEPARATOR_LINE);
        handleReusableResourcesAfterTransfer(logger, config, sourceDelegator, targetDelegator);

        try {
            String moved = isMoveOperation ? YADEClientBannerWriter.formatState(TransferEntryState.MOVED) : "";
            for (ProviderFile pf : sourceFiles) {
                YADEProviderFile sourceFile = (YADEProviderFile) pf;
                String fileTransferLogPrefix = String.valueOf(sourceFile.getIndex());
                YADEFileActionsExecuter.postProcessingOnSuccess(logger, fileTransferLogPrefix, config, sourceDelegator, targetDelegator, sourceFile,
                        true);

                if (isMoveOperation) {
                    if (sourceDelegator.getProvider().deleteIfExists(sourceFile.getFinalFullPath())) {
                        sourceFile.setState(TransferEntryState.MOVED);
                        logger.info("[%s][%s][%s]%s", fileTransferLogPrefix, moved, sourceDelegator.getLabel(), sourceFile.getFinalFullPath());
                    }
                }
            }
        } catch (Exception e) {
            logger.error("[finalizeTransaction]" + e.getMessage());
            throw e;
        }
    }

    /** @apiNote the Source files by rollback are not affected */
    private static void rollbackTransactionIfNeeded(ISOSLogger logger, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles,
            boolean useCumulativeTargetFile) {
        if (!config.isTransactionalEnabled()) {
            return;
        }
        if (useCumulativeTargetFile) {
            YADETargetCumulativeFileHelper.rollback(logger, config, targetDelegator);
            return;
        }
        logger.info(YADEClientBannerWriter.SEPARATOR_LINE);

        boolean isJumpHostRollback = false;
        if (targetDelegator.isJumpHost()) {
            // Source(Any Provider) -> Jump(SSHProvider)
            logger.info("[" + targetDelegator.getLabel() + "]rollback");
            isJumpHostRollback = true;
        }

        if (!isJumpHostRollback) {
            handleReusableResourcesAfterTransfer(logger, config, sourceDelegator, targetDelegator);
        }

        l: for (ProviderFile pf : sourceFiles) {
            YADEProviderFile sourceFile = (YADEProviderFile) pf;
            YADEProviderFile targetFile = sourceFile.getTarget();
            String fileTransferLogPrefix = String.valueOf(sourceFile.getIndex());

            // 1) a targetFile was not initialized because the transfer was aborted in a previous file
            if (targetFile == null) {
                // set state on sourceFile for Summary
                sourceFile.setState(TransferEntryState.SELECTED);
                sourceFile.setSubState(TransferEntryState.ABORTED);
                continue l;
            }

            // 2) only set the status targetFile - the entire jump directory will be deleted anyway - no individual files need to be deleted
            if (isJumpHostRollback) {
                targetFile.setSubState(TransferEntryState.ROLLED_BACK);
                continue l;
            }

            // 3) the targetFile was not created because it may have been skipped due to non-overwrite criteria
            if (!targetFile.isTransferredOrTransferring()) {
                continue l;
            }

            // 4) a targetFile intergityHash file may have been created and needs to be deleted
            if (config.getTarget().isCreateIntegrityHashFileEnabled() && targetFile.getIntegrityHash() != null) {
                String path = targetFile.getFinalFullPath() + config.getIntegrityHashFileExtensionWithDot();
                try {
                    if (targetDelegator.getProvider().deleteIfExists(path)) {
                        logger.info("[%s][%s][rollback][%s]deleted", fileTransferLogPrefix, targetDelegator.getLabel(), path);
                    }
                } catch (Exception e) {
                    logger.error("[%s][%s][rollback][%s]%s", fileTransferLogPrefix, targetDelegator.getLabel(), path, e.toString());
                }
            }

            // 5) the targetFile may have already been renamed to the final name or may still be a file with the atomic suffix/prefix
            // - note for "if compress": all names already contain the compress extension
            String targetFilePath = TransferEntryState.RENAMED.equals(targetFile.getSubState()) ? targetFile.getFinalFullPath() : targetFile
                    .getFullPath();

            // 6) delete targetFile
            try {
                if (targetDelegator.getProvider().deleteIfExists(targetFilePath)) {
                    logger.info("[%s][%s][rollback][%s]deleted", fileTransferLogPrefix, targetDelegator.getLabel(), targetFilePath);
                }
                targetFile.setSubState(TransferEntryState.ROLLED_BACK);
            } catch (Exception e) {
                logger.error("[%s][%s][rollback][%s]%s", fileTransferLogPrefix, targetDelegator.getLabel(), targetFilePath, e.toString());
                targetFile.setSubState(TransferEntryState.ROLLBACK_FAILED);
            }
        }
    }

    private static void handleReusableResourcesAfterTransfer(ISOSLogger logger, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator) {

        // if (!config.processFilesSequentially()) {
        sourceDelegator.getProvider().enableReusableResource();
        targetDelegator.getProvider().enableReusableResource();
        // }
    }

    private static void setFailedIfNonTransactional(YADEProviderFile f) {
        YADEProviderFile y = (YADEProviderFile) f;
        if (y.getTarget() == null) {
            y.setSubState(TransferEntryState.FAILED);
        } else {
            y.getTarget().setSubState(TransferEntryState.FAILED);
        }
    }

    private static Throwable getTransferFileException(Throwable ex) {
        if (ex == null) {
            return ex;
        }
        Throwable e = ex;
        while (e != null) {
            if (e instanceof YADEEngineTransferFileException) {
                return e;
            }
            e = e.getCause();
        }
        return e;
    }

}
