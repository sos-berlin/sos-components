package com.sos.yade.engine.handlers.operations.remove;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.util.concurrency.SOSParallelWorkerExecutor;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.commons.Yade.TransferEntryState;
import com.sos.yade.commons.Yade.TransferOperation;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.commons.helpers.YADEParallelExecutorFactory;
import com.sos.yade.engine.exceptions.YADEEngineOperationException;
import com.sos.yade.engine.exceptions.YADEEngineRemoveFileException;
import com.sos.yade.engine.handlers.command.YADECommandExecutor;

/** Remove files on Source */
public class YADERemoveOperationHandler {

    public static void process(TransferOperation operation, ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            List<ProviderFile> sourceFiles, AtomicBoolean cancel) throws YADEEngineOperationException {

        int sourceFilesSize = sourceFiles.size();
        int parallelism = YADEParallelExecutorFactory.getParallelism(args, sourceFilesSize);
        try {
            if (parallelism == 1) {
                processFilesSequentially(logger, sourceDelegator, sourceFiles, cancel);
            } else {
                processFilesInParallel(logger, sourceDelegator, sourceFiles, parallelism, sourceFilesSize, cancel);
            }
        } catch (Exception e) {
            throw new YADEEngineOperationException(e.getCause());
        }
    }

    private static void processFilesSequentially(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles,
            AtomicBoolean cancel) throws Exception {

        for (ProviderFile sourceFile : sourceFiles) {
            if (cancel.get()) {
                return;
            }
            run(logger, sourceDelegator, (YADEProviderFile) sourceFile, false);
        }
    }

    private static void processFilesInParallel(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, List<ProviderFile> sourceFiles,
            int parallelism, int sourceFilesSize, AtomicBoolean cancel) throws Exception {
        try (SOSParallelWorkerExecutor<YADEProviderFile> executor = YADEParallelExecutorFactory.createExecutor(sourceFiles, parallelism, true,
                cancel);) {
            executor.execute(sourceFile -> {
                run(logger, sourceDelegator, sourceFile, true);
            });
            executor.awaitAndShutdown();
        } catch (Exception e) {
            throw new YADEEngineOperationException(getRemoveFileException(e.getCause()));
        }
        finally {
            YADEParallelExecutorFactory.cleanup(sourceDelegator);
        }
    }

    private static void run(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADEProviderFile sourceFile, boolean isParallelismEnabled)
            throws YADEEngineRemoveFileException {
        sourceFile.resetSteady();

        String fileTransferLogPrefix = !isParallelismEnabled ? String.valueOf(sourceFile.getIndex()) : sourceFile.getIndex() + "][" + Thread
                .currentThread().getName();
        try {
            YADECommandExecutor.executeBeforeFile(logger, sourceDelegator, sourceFile);

            if (!sourceDelegator.getProvider().deleteFileIfExists(sourceFile.getFullPath())) {
                if (logger.isDebugEnabled()) {
                    logger.debug("[%s][%s][%s][%s]not exists", fileTransferLogPrefix, YADEClientBannerWriter.formatState(sourceFile.getState()),
                            sourceDelegator.getLabel(), sourceFile.getFullPath());
                }
            }
            sourceFile.setState(TransferEntryState.DELETED);
            logger.info("[%s][%s][%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(sourceFile.getState()), sourceDelegator
                    .getLabel(), sourceFile.getFullPath());

            // YADE JS7 (YADE1 does not execute AfterFile commands in case of a DELETE operation)
            YADECommandExecutor.executeAfterFile(logger, sourceDelegator, sourceFile);
        } catch (Exception e) {
            sourceFile.setState(TransferEntryState.FAILED);

            String msg = String.format("[%s][%s][%s][%s]%s", fileTransferLogPrefix, YADEClientBannerWriter.formatState(sourceFile.getState()),
                    sourceDelegator.getLabel(), sourceFile.getFullPath(), e);
            logger.error(msg);
            throw new YADEEngineRemoveFileException(msg, e);

        }
    }

    private static Throwable getRemoveFileException(Throwable ex) {
        if (ex == null) {
            return ex;
        }
        Throwable e = ex;
        while (e != null) {
            if (e instanceof YADEEngineRemoveFileException) {
                return e;
            }
            e = e.getCause();
        }
        return e;
    }
}
