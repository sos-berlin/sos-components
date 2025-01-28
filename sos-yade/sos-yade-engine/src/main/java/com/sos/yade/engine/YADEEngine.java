package com.sos.yade.engine;

import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.common.YADEDirectory;
import com.sos.yade.engine.common.YADEHelper;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.arguments.YADESourceTargetArguments;
import com.sos.yade.engine.common.handler.YADECommandsHandler;
import com.sos.yade.engine.common.handler.source.YADESourceFilesSelector;
import com.sos.yade.engine.common.handler.source.YADESourcePollingHandler;
import com.sos.yade.engine.common.handler.source.YADESourceSteadyFilesHandler;
import com.sos.yade.engine.common.handler.source.YADESourceZeroByteFilesHandler;
import com.sos.yade.engine.exception.SOSYADEEngineConnectionException;
import com.sos.yade.engine.exception.SOSYADEEngineSourceZeroByteFilesException;

public class YADEEngine {

    public YADEEngine() {
        // TODO
    }

    // static ???
    public void execute(ISOSLogger logger, YADEArguments args) throws Exception {
        IProvider sourceProvider = null;
        IProvider targetProvider = null;

        YADEDirectory sourceDir = null;
        YADEDirectory targetDir = null;

        Throwable exception = null;
        try {
            /** 1) print transfer configuration */
            YADEHelper.printBanner(logger, args);

            /** 2) check/initialize configuration */
            YADEHelper.checkArguments(args);
            YADEHelper.setConfiguredSystemProperties(logger, args);

            // source/target initialize providers
            sourceProvider = YADEHelper.getProvider(logger, args, true);
            targetProvider = YADEHelper.getProvider(logger, args, false);

            // source/target normalized directories
            sourceDir = YADEHelper.getYADEDirectory(sourceProvider, args.getSource());
            targetDir = YADEHelper.getYADEDirectory(targetProvider, args.getTarget());

            // source handlers
            YADESourcePollingHandler sourcePolling = new YADESourcePollingHandler(args.getSource());
            YADESourceZeroByteFilesHandler sourceZeroBytes = new YADESourceZeroByteFilesHandler();

            // set YADE specific ProviderFile
            sourceProvider.setProviderFileCreator(builder -> new YADEProviderFile(builder.getFullPath(), builder.getSize(), builder
                    .getLastModifiedMillis(), args.getSource().checkSteadyState()));

            /** 3) connect source provider */
            connect(logger, sourceProvider, args.getSource());

            /** 4) source provider execute commands before operation */
            YADECommandsHandler.executeBeforeOperation(logger, sourceProvider, args.getSource());

            if (sourcePolling.enabled()) {
                pl: while (true) {
                    /** 5) select files on source */
                    List<ProviderFile> sourceFiles = sourcePolling.selectFiles(logger, sourceProvider, sourceDir);

                    /** 6) check source files steady */
                    if (!YADESourceSteadyFilesHandler.checkFilesSteady(logger, sourceProvider, args.getSource(), sourceFiles)) {
                        break pl;
                    }

                    /** 7) handleZeroByteFiles on source - NOT throw exception */
                    boolean shouldExecuteOperation = true;
                    try {
                        sourceFiles = sourceZeroBytes.filter(logger, sourceProvider, args.getSource(), sourceFiles);
                    } catch (SOSYADEEngineSourceZeroByteFilesException e) {
                        logger.error("%s[%s]%s", sourceProvider.getContext().getLogPrefix(), sourcePolling.getMethod(), e.toString());
                        shouldExecuteOperation = false;
                    }

                    if (shouldExecuteOperation) {
                        /** 8) connect target provider */
                        connect(logger, targetProvider, args.getTarget());

                        // YADE 1 handling - execute before operation commands only once - is it OK????
                        if (sourcePolling.isFirstCycle()) {
                            /** 9) target provider execute commands before operation */
                            YADECommandsHandler.executeBeforeOperation(logger, targetProvider, args.getTarget());
                        }

                        /** 10) transfer */
                        // -- handle operations: GetList etc

                        /** 11) execute commands after operation on success */
                        try {
                            YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceProvider, args.getSource(), sourceDir);
                        } catch (Throwable e) {
                            logger.error("%s[%s]%s", sourceProvider.getContext().getLogPrefix(), sourcePolling.getMethod(), e.toString());
                        }
                        try {
                            YADECommandsHandler.executeAfterOperationOnSuccess(logger, targetProvider, args.getTarget(), targetDir);
                        } catch (Throwable e) {
                            logger.error("%s[%s]%s", targetProvider.getContext().getLogPrefix(), sourcePolling.getMethod(), e.toString());
                        }
                    }

                    disconnect(targetProvider);
                    if (!sourcePolling.startNextPollingCycle(logger)) {
                        break pl;
                    }

                    // sendNotifications
                    YADEHelper.printSummary(logger, args);
                }
            } else {
                /** 5) select files on source */
                List<ProviderFile> sourceFiles = YADESourceFilesSelector.selectFiles(logger, sourceProvider, args.getSource(), sourceDir, false);

                /** 6) check source files steady */
                if (YADESourceSteadyFilesHandler.checkFilesSteady(logger, sourceProvider, args.getSource(), sourceFiles)) {

                    /** 7) handleZeroByteFiles on source - throws exception */
                    sourceFiles = sourceZeroBytes.filter(logger, sourceProvider, args.getSource(), sourceFiles);

                    /** 8) connect target provider */
                    connect(logger, targetProvider, args.getTarget());

                    /** 9) target provider execute commands before operation */
                    YADECommandsHandler.executeBeforeOperation(logger, targetProvider, args.getTarget());

                    /** 10) transfer */
                    // -- handle operations: GetList etc

                    /** 11) execute commands after operation on success */
                    YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceProvider, args.getSource(), sourceDir);
                    YADECommandsHandler.executeAfterOperationOnSuccess(logger, targetProvider, args.getTarget(), targetDir);
                }
            }

        } catch (Throwable e) {
            /** execute commands after operation on error */
            YADECommandsHandler.executeAfterOperationOnError(logger, sourceProvider, args.getSource(), sourceDir, e);
            YADECommandsHandler.executeAfterOperationOnError(logger, targetProvider, args.getTarget(), targetDir, e);

            exception = e;
            throw e;
        } finally {
            /** execute commands after operation final */
            YADECommandsHandler.executeAfterOperationFinal(logger, sourceProvider, args.getSource(), sourceDir, exception);
            YADECommandsHandler.executeAfterOperationFinal(logger, targetProvider, args.getTarget(), targetDir, exception);

            disconnect(sourceProvider, targetProvider);

            // sendNotifications
            YADEHelper.printSummary(logger, args);
        }
    }

    // TODO alternate connections ... + see YADEEngineSourcePollingHandler.ensureConnected
    private static void connect(ISOSLogger logger, IProvider provider, YADESourceTargetArguments args) throws SOSYADEEngineConnectionException {
        if (provider == null) {
            return;
        }

        // without retry
        if (!args.retryOnConnectionError()) {
            try {
                provider.connect();
            } catch (Throwable e) {
                YADEHelper.throwConnectionException(provider, e);
            }
            return;
        }

        // with retry
        int maxRetries = args.getConnectionErrorRetryCountMax().getValue().intValue();
        long retryInterval = YADEHelper.getIntervalInSeconds(args.getConnectionErrorRetryInterval(), 0);
        for (int retryCounter = 0; retryCounter <= maxRetries; retryCounter++) {
            try {
                provider.connect();
                return;
            } catch (Throwable e) {
                if (retryCounter == maxRetries) {
                    YADEHelper.throwConnectionException(provider, e);
                }
                logger.info("%s[retry=%s in %ss]%s", provider.getContext().getLogPrefix(), retryCounter + 1, retryInterval, e.toString(), e);
                YADEHelper.waitFor(retryInterval);
            }
        }
    }

    /** Provider Disconnect does not throw exceptions - but logs with the type (source/destination) when occurred/disconnect executed
     * 
     * @param source
     * @param target */
    private static void disconnect(IProvider... providers) {
        for (IProvider p : providers) {
            if (p != null) {
                p.disconnect();
            }
        }
    }

}
