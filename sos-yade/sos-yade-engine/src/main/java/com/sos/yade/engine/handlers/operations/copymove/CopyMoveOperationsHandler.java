package com.sos.yade.engine.handlers.operations.copymove;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import com.sos.commons.exception.SOSMissingDataException;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.common.file.files.DeleteFilesResult;
import com.sos.commons.vfs.common.file.files.RenameFilesResult;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderFile;
import com.sos.yade.engine.exceptions.YADEEngineOperationException;
import com.sos.yade.engine.handlers.operations.copymove.fileoperations.FileHandler;

// TransferEntryState.NOT_OVERWRITTEN
// TransferEntryState.TRANSFERRING
// TransferEntryState.TRANSFERRED
// TransferEntryState.ROLLED_BACK <- (Target file deleted) checkTargetFileSize, checkChecksum
// targetFile.setSubState(TransferEntryState.RENAMED); after transfer
// sourceFile.setState(TransferEntryState.MOVED); after transfer - after source file deleted
public class CopyMoveOperationsHandler {

    public static void process(TransferOperation operation, ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, AtomicBoolean cancel) throws YADEEngineOperationException {
        if (targetDelegator == null) {
            throw new YADEEngineOperationException(new SOSMissingDataException("TargetDelegator"));
        }

        // 1) Source/Target: initialize transfer configuration(cumulative file,compress,atomic etc.)
        CopyMoveOperationsConfig config = new CopyMoveOperationsConfig(operation, args, sourceDelegator, targetDelegator);

        try {
            // 2) Target: map the source to the target directories and try to create all target directories before individual file transfer
            // - all target directories are only evaluated if target replacement is not enabled,
            // -- otherwise the target directories are evaluated/created on every file
            sourceDelegator.getDirectoryMapper().tryCreateAllTargetDirectoriesBeforeOperation(logger, config, targetDelegator);
        } catch (SOSProviderException e) {
            throw new YADEEngineOperationException(e);
        }

        // 3) Target: delete cumulative file before Transfer files
        // TODO cumulative file - an extra object? read file size before operation? and calculate the file size progress if compress?
        deleteTargetCumulativeFile(config, targetDelegator);

        // 4) Source/Target: Transfer files
        try {
            processFiles(logger, config, sourceDelegator, targetDelegator, sourceFiles, cancel);
        } catch (Throwable e) {
            // does not throws exception
            rollbackIfTransactional(logger, config, sourceDelegator, targetDelegator, sourceFiles);
            throw e;
        }
        completeSourceIfTransactional(logger, config, sourceDelegator, targetDelegator, sourceFiles);
    }

    private static void processFiles(ISOSLogger logger, CopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, AtomicBoolean cancel) throws YADEEngineOperationException {
        if (config.getParallelMaxThreads() == 1 || sourceFiles.size() == 1 || config.getTarget().getCumulate() != null) {
            processFilesSequentially(logger, config, sourceDelegator, targetDelegator, sourceFiles, cancel);
        } else {
            processFilesInParallel(logger, config, sourceDelegator, targetDelegator, sourceFiles, cancel);
        }
    }

    private static void processFilesInParallel(ISOSLogger logger, CopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, AtomicBoolean cancel) throws YADEEngineOperationException {
        int maxThreads = config.getParallelMaxThreads();
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
                sourceFiles.parallelStream().forEach(f -> {
                    if (cancel.get()) {
                        return;
                    }
                    try {
                        new FileHandler(logger, config, sourceDelegator, targetDelegator, (YADEProviderFile) f, cancel).run();
                    } catch (Throwable e) {
                        if (config.isTransactionalEnabled()) {
                            cancel.set(true);
                            new RuntimeException(e);
                        }
                    }
                });
            }).join();
        } catch (Throwable e) {
            throw new YADEEngineOperationException(e);
        } finally {
            threadPool.shutdown();
            try {
                threadPool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
            }
        }
    }

    private static void processFilesSequentially(ISOSLogger logger, CopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles, AtomicBoolean cancel) throws YADEEngineOperationException {
        for (ProviderFile sourceFile : sourceFiles) {
            if (cancel.get()) {
                return;
            }
            try {
                new FileHandler(logger, config, sourceDelegator, targetDelegator, (YADEProviderFile) sourceFile, cancel).run();
            } catch (Throwable e) {
                if (config.isTransactionalEnabled()) {
                    cancel.set(true);
                    throw new YADEEngineOperationException(e);
                }
            }
        }
    }

    private static void deleteTargetCumulativeFile(CopyMoveOperationsConfig config, YADETargetProviderDelegator targetDelegator)
            throws YADEEngineOperationException {
        if (!config.getTarget().isDeleteCumulativeFileEnabled()) {
            return;
        }
        try {
            targetDelegator.getProvider().deleteIfExists(config.getTarget().getCumulate().getFile().getFinalFullPath());
        } catch (Throwable e) {
            throw new YADEEngineOperationException(e);
        }
    }

    private static void completeSourceIfTransactional(ISOSLogger logger, CopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles) throws YADEEngineOperationException {
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

    private static void rollbackIfTransactional(ISOSLogger logger, CopyMoveOperationsConfig config, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> sourceFiles) {
        if (!config.isTransactionalEnabled()) {
            return;
        }
        // 1) Target: delete cumulative file
        try {
            deleteTargetCumulativeFile(config, targetDelegator);
        } catch (YADEEngineOperationException e) {
            // TODO
            logger.error(e.toString());
        }

        // 2) Target: delete files with the TRANSFERRING/TRANSFERRED state
        // - Note: the Target files can be already renamed
        // - Note: the Source files by rollback are not affected
        List<String> paths = sourceFiles.stream().map(f -> ((YADETargetProviderFile) f).getTarget()).filter(f -> f != null && f
                .isTransferredOrTransferring()).map(f -> {
                    if (TransferEntryState.RENAMED.equals(f.getSubState())) {
                        return f.getFinalFullPath();
                    } else {
                        return f.getFullPath();// Atomic prefix/suffix
                    }
                }).collect(Collectors.toList());

        // TODO - set ROLLED_BACK state
        if (paths.size() > 0) {
            try {
                DeleteFilesResult r = targetDelegator.getProvider().deleteFilesIfExist(paths, false);
                logger.info("%s[rollback][deleteResult]%s", targetDelegator.getLogPrefix(), r);
                if (logger.isDebugEnabled()) {
                    logger.debug("%s[rollback][to delete]%s", targetDelegator.getLogPrefix(), paths);
                }
            } catch (SOSProviderException e) {
                logger.info("%s[rollback][deleteFiles]%s", targetDelegator.getLogPrefix(), paths);
                logger.error("%s[rollback][deleteFiles]%s", targetDelegator.getLogPrefix(), e.toString());
            }
        }
    }

}
