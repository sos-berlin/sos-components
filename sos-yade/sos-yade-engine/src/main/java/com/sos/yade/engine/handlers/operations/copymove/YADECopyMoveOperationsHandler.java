package com.sos.yade.engine.handlers.operations.copymove;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.common.file.files.RenameFilesResult;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.common.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.YADEEngineOperationException;
import com.sos.yade.engine.handlers.operations.copymove.file.YADEFileHandler;
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
        boolean useCumulativeTargetFile = config.getTarget().getCumulate() != null;
        try {
            processFiles(logger, config, sourceDelegator, targetDelegator, sourceFiles, useCumulativeTargetFile, cancel);
        } catch (Throwable e) {
            // does not throws exception
            rollbackIfTransactional(logger, config, sourceDelegator, targetDelegator, sourceFiles, useCumulativeTargetFile);
            throw e;
        }
        completeSourceIfTransactional(logger, config, sourceDelegator, targetDelegator, sourceFiles);
    }

    private static void processFiles(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, boolean useCumulativeTargetFile, AtomicBoolean cancel)
            throws Exception {
        if (config.processFilesSequentially()) {
            processFilesSequentially(logger, config, sourceDelegator, targetDelegator, sourceFiles, useCumulativeTargetFile, cancel);
        } else {
            processFilesInParallel(logger, config, sourceDelegator, targetDelegator, sourceFiles, cancel);
        }
    }

    // cumulative file is not processed here - only sequential processing
    private static void processFilesInParallel(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, AtomicBoolean cancel) throws Exception {
        int maxThreads = config.getParallelism();
        int size = sourceFiles.size();
        if (size < maxThreads) {
            maxThreads = size;
        }
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
                        new YADEFileHandler(logger, config, sourceDelegator, targetDelegator, f, cancel).run(false);
                    } catch (Throwable e) {
                        if (config.isTransactionalEnabled()) {
                            cancel.set(true);
                            new RuntimeException(e);
                        }
                        setFailedIfNonTransactional(f);
                    }
                });
            }).join();
        } catch (Throwable e) {
            throw new YADEEngineOperationException(e.getCause());
        } finally {
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }
    }

    private static void processFilesSequentially(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, boolean useCumulativeTargetFile, AtomicBoolean cancel)
            throws Exception {

        if (useCumulativeTargetFile) {
            YADETargetCumulativeFileHelper.tryDeleteFile(logger, config, targetDelegator);
        }

        for (ProviderFile sourceFile : sourceFiles) {
            if (cancel.get()) {
                return;
            }
            YADEProviderFile f = (YADEProviderFile) sourceFile;
            try {
                new YADEFileHandler(logger, config, sourceDelegator, targetDelegator, f, cancel).run(useCumulativeTargetFile);
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

    private static void completeSourceIfTransactional(ISOSLogger logger, YADECopyMoveOperationsConfig config,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles)
            throws Exception {
        if (!config.isTransactionalEnabled()) {
            return;
        }

        if (config.isMoveOperation()) {
            List<String> paths = sourceFiles.stream().map(f -> f.getFullPath()).collect(Collectors.toList());
            // TODO - set DELETED state ?
            if (paths.size() > 0) {
                try {
                    DeleteFilesResult r = sourceDelegator.getProvider().deleteFilesIfExist(paths, false);
                    if (r.hasErrors()) {
                        logger.error("%s[deleteResult]%s", sourceDelegator.getLogPrefix(), r);
                        throw new YADEEngineOperationException(String.format("%s[deleteFiles]%s", sourceDelegator.getLogPrefix(), r));
                    }
                    logger.info("%s[deleteResult]%s", sourceDelegator.getLogPrefix(), r);
                    if (logger.isDebugEnabled()) {
                        logger.debug("%s[to delete]%s", sourceDelegator.getLogPrefix(), paths);
                    }
                } catch (SOSProviderException e) {
                    logger.error("%s[deleteFiles]%s", sourceDelegator.getLogPrefix(), e.toString());
                    throw new YADEEngineOperationException(String.format("%s[deleteFiles]%s", sourceDelegator.getLogPrefix(), e.toString()), e);
                }
            }
        } else {
            if (config.getSource().isReplacementEnabled()) {
                // independent of NOT_OVERWRITTEN
                Map<String, String> paths = sourceFiles.stream()// filter
                        .map(f -> (YADEProviderFile) f)// map
                        .filter(f -> f.needsRename())// collect
                        .collect(Collectors.toMap(f -> f.getFullPath(), // oldPath
                                f -> f.getFinalFullPath(),// newPath
                                (existing, replacement) -> existing,// remove duplicates
                                LinkedHashMap::new));

                if (paths.size() > 0) {
                    try {
                        RenameFilesResult r = sourceDelegator.getProvider().renameFilesIfExist(paths, false);
                        if (r.hasErrors()) {
                            logger.error("%s[renameResult]%s", sourceDelegator.getLogPrefix(), r);
                            throw new YADEEngineOperationException(String.format("%s[renameFiles]%s", sourceDelegator.getLogPrefix(), r));
                        }
                        logger.info("%s[renameFiles]%s", sourceDelegator.getLogPrefix(), r);
                        if (logger.isDebugEnabled()) {
                            logger.debug("%s[to rename]%s", sourceDelegator.getLogPrefix(), paths);
                        }
                    } catch (SOSProviderException e) {
                        logger.error("%s[renameFiles]%s", sourceDelegator.getLogPrefix(), e.toString());
                        throw new YADEEngineOperationException(String.format("%s[renameFiles]%s", sourceDelegator.getLogPrefix(), e.toString()), e);
                    }
                }
            }
        }
    }

    private static void rollbackIfTransactional(ISOSLogger logger, YADECopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, boolean useCumulativeTargetFile) {
        if (!config.isTransactionalEnabled()) {
            return;
        }

        if (useCumulativeTargetFile) {
            YADETargetCumulativeFileHelper.rollback(logger, config, targetDelegator);
            return;
        }

        // 2) Target: delete files with the TRANSFERRING/TRANSFERRED state
        // - Note: the Target files can be already renamed
        // - Note: the Source files by rollback are not affected
        Map<String, YADEProviderFile> paths = sourceFiles.stream()// filter
                .map(f -> {
                    YADEProviderFile y = (YADEProviderFile) f;
                    if (y.getTarget() == null) {
                        // set state on sourceFile for Summary if target was not initialized(e.g. error occurs in a previous file)
                        y.setState(TransferEntryState.SELECTED);
                        y.setSubState(TransferEntryState.ROLLED_BACK);
                        return null;
                    }
                    return y.getTarget().isTransferredOrTransferring() ? y : null;
                }).filter(Objects::nonNull).collect(Collectors.toMap(
                        // key - String
                        f -> {
                            if (TransferEntryState.RENAMED.equals(f.getTarget().getSubState())) {
                                return f.getTarget().getFinalFullPath();
                            } else {
                                return f.getTarget().getFullPath();// Atomic prefix/suffix
                            }
                        },
                        // value - ProviderFile object
                        f -> f, (
                                // remove duplicates
                                existing, replacement) -> existing, LinkedHashMap::new));

        if (paths.size() > 0) {
            try {
                if (logger.isDebugEnabled()) {
                    logger.debug("%s[rollback][deleteFiles]%s", targetDelegator.getLogPrefix(), String.join(" ,", paths.keySet()));
                }
                DeleteFilesResult r = targetDelegator.getProvider().deleteFilesIfExist(paths.keySet(), false);
                paths.entrySet().stream().forEach(m -> {
                    if (r.getErrors().containsKey(m.getKey())) {
                        // TODO rollback-failed
                        m.getValue().getTarget().setSubState(TransferEntryState.FAILED);
                    } else {
                        // already filtered by getTarget() != null
                        m.getValue().getTarget().setSubState(TransferEntryState.ROLLED_BACK);
                    }
                });
                logger.info("%s[rollback][deleteResult]%s", targetDelegator.getLogPrefix(), r);
            } catch (SOSProviderException e) {
                logger.info("%s[rollback][deleteFiles]%s", targetDelegator.getLogPrefix(), String.join(" ,", paths.keySet()));
                logger.error("%s[rollback][deleteFiles]%s", targetDelegator.getLogPrefix(), e.toString());
            }
        }
    }

    private static void setFailedIfNonTransactional(YADEProviderFile f) {
        YADEProviderFile y = (YADEProviderFile) f;
        if (y.getTarget() == null) {
            y.setSubState(TransferEntryState.FAILED);
        } else {
            y.getTarget().setSubState(TransferEntryState.FAILED);
        }
    }

}
