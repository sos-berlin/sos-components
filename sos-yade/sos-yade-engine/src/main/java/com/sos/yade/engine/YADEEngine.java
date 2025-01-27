package com.sos.yade.engine;

import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.commons.vfs.exception.SOSProviderException;
import com.sos.yade.engine.common.YADEDirectory;
import com.sos.yade.engine.common.YADEEngineHelper;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.handler.source.YADEEngineSourcePollingHandler;
import com.sos.yade.engine.common.handler.source.YADEEngineSourceSteadyFilesHandler;
import com.sos.yade.engine.common.handler.source.YADEEngineSourceZeroByteFilesHandler;
import com.sos.yade.engine.exception.SOSYADEEngineSourceZeroByteFilesException;

public class YADEEngine {

    public YADEEngine() {
        // TODO
    }

    public void execute(ISOSLogger logger, YADEArguments args) throws Exception {
        IProvider sourceProvider = null;
        IProvider targetProvider = null;

        try {
            /** 1) print transfer configuration */
            YADEEngineHelper.printBanner(logger, args);

            /** 2) check/initialize configuration */
            YADEEngineHelper.checkArguments(args);
            YADEEngineHelper.setConfiguredSystemProperties(logger, args);

            // initialize providers
            sourceProvider = YADEEngineHelper.getProvider(logger, args, true);
            targetProvider = YADEEngineHelper.getProvider(logger, args, false);

            // source handlers
            YADEEngineSourcePollingHandler sourcePolling = new YADEEngineSourcePollingHandler(args.getSource());
            YADEEngineSourceZeroByteFilesHandler sourceZeroBytes = new YADEEngineSourceZeroByteFilesHandler();

            // source/target normalized directories
            YADEDirectory sourceDir = YADEEngineHelper.getYADEDirectory(sourceProvider, args.getSource());
            YADEDirectory targetDir = YADEEngineHelper.getYADEDirectory(targetProvider, args.getTarget());

            // set YADE specific ProviderFile
            sourceProvider.setProviderFileCreator(builder -> new YADEProviderFile(builder.getFullPath(), builder.getSize(), builder
                    .getLastModifiedMillis(), args.getSource().checkSteadyState()));

            /** 3) connect source provider */
            connect(sourceProvider);

            if (sourcePolling.enabled()) {
                pl: while (true) {
                    /** 4) select files on source */
                    List<ProviderFile> sourceFiles = sourcePolling.selectFiles(logger, sourceProvider, sourceDir);

                    /** 5) check source files steady */
                    if (!YADEEngineSourceSteadyFilesHandler.checkFilesSteady(logger, sourceProvider, args.getSource(), sourceFiles)) {
                        break pl;
                    }

                    /** 6) handleZeroByteFiles on source - NOT throw exception */
                    boolean shouldExecuteOperation = true;
                    try {
                        sourceFiles = sourceZeroBytes.filter(logger, args.getSource(), sourceFiles);
                    } catch (SOSYADEEngineSourceZeroByteFilesException e) {
                        logger.error("[%s]%s", sourcePolling.getMethod(), e.toString());
                        shouldExecuteOperation = false;
                    }

                    if (shouldExecuteOperation) {
                        /** 7) connect target provider */
                        connect(targetProvider);
                
                        /** 8) transfer */
                        // -- handle operations: GetList etc
                    }

                    disconnect(targetProvider);
                    if (!sourcePolling.startNextPollingCycle(logger, !shouldExecuteOperation)) {
                        break pl;
                    }
                }
            } else {
                /** 4) select files on source */
                List<ProviderFile> sourceFiles = sourceProvider.selectFiles("");

                /** 5) check source files steady */
                if (YADEEngineSourceSteadyFilesHandler.checkFilesSteady(logger, sourceProvider, args.getSource(), sourceFiles)) {

                    /** 6) handleZeroByteFiles on source - throws exception */
                    sourceFiles = sourceZeroBytes.filter(logger, args.getSource(), sourceFiles);

                    /** 7) connect target provider */
                    connect(targetProvider);

                    /** 8) transfer */
                    // -- handle operations: GetList etc
                }
            }
            /** 9) disconnect */
            disconnect(sourceProvider, targetProvider);
            sourceProvider = null;
            targetProvider = null;
        } catch (Throwable e) {
            throw e;
        } finally {
            disconnect(sourceProvider, targetProvider);// if exception
            // 10 - print summary
            YADEEngineHelper.printSummary(logger, args);
        }
    }

    private void executePostTransferCommandsFinal(IProvider sourceTrovider, IProvider targetProvider, Throwable e) {
        try {
            // if(target != null && target.po)

            // executePostTransferCommandsFinal(exception);
        } catch (Throwable t) {
            // logger.error(t.toString());
        }
    }

    private void connect(IProvider... providers) throws SOSProviderException {
        for (IProvider p : providers) {
            if (p != null) {
                p.connect();
            }
        }
    }

    /** Provider Disconnect does not throw exceptions - but logs with the type (source/destination) when occurred/disconnect executed
     * 
     * @param source
     * @param target */
    private void disconnect(IProvider... providers) {
        for (IProvider p : providers) {
            if (p != null) {
                p.disconnect();
            }
        }
    }

}
