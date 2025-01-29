package com.sos.yade.engine;

import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.common.YADEProviderFile;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.handler.YADECommandsHandler;
import com.sos.yade.engine.common.handler.YADEOperationHandler;
import com.sos.yade.engine.common.handler.source.YADESourceFilesSelector;
import com.sos.yade.engine.common.handler.source.YADESourcePollingHandler;
import com.sos.yade.engine.common.handler.source.YADESourceSteadyFilesHandler;
import com.sos.yade.engine.common.handler.source.YADESourceZeroByteFilesHandler;
import com.sos.yade.engine.common.helper.YADEHelper;
import com.sos.yade.engine.common.helper.YADEProviderHelper;
import com.sos.yade.engine.exception.SOSYADEEngineSourceZeroByteFilesException;

/** TODO<br/>
 * - resultSetFile: check YADE 1 behavior<br/>
 * -- deleteIfExists?<br/>
 * -- Full Path?<br/>
 * --- of a Source/Target file?<br />
 * ---- to check: YADE 1 seems to create resultSetFile before transfer (so for Source files...)<br/>
 * - YADE 1 extra LOGGER - JADE_REPORT_LOGGER (as HTML)<br/>
 * - forceFiles:<br/>
 * -- YADE 1 checks after transfer... Why?<br/>
 * -- YADE 1 does not check when GETLIST and REMOVE - Why?<br/>
 * - Polling:<br/>
 * -- YADE 1 execute commands:<br/>
 * --- before operation(only once and not before each cycle) <br/>
 * --- after operation on success(after each cycle) <br/>
 * --- after operation on error (only if YADE(not a polling cycle) finished with an error) <br/>
 * --- after operation final (only if YADE(not a polling cycle) finished) <br/>
 */
public class YADEEngine {

    public YADEEngine() {
        // TODO
    }

    // static ???
    public void execute(ISOSLogger logger, YADEArguments args) throws Exception {
        IProvider sourceProvider = null;
        IProvider targetProvider = null;

        ProviderDirectoryPath sourceDir = null;
        ProviderDirectoryPath targetDir = null;

        Throwable exception = null;
        try {
            /** 1) print transfer configuration */
            YADEHelper.printBanner(logger, args);

            /** 2) check/initialize configuration */
            YADEHelper.checkArguments(args);
            YADEHelper.setConfiguredSystemProperties(logger, args.getClient());

            // source/target initialize providers
            sourceProvider = YADEProviderHelper.getProvider(logger, args, true);
            targetProvider = YADEProviderHelper.getProvider(logger, args, false);

            // source/target normalized directories
            sourceDir = sourceProvider.getDirectoryPath(args.getSource().getDirectory().getValue());
            targetDir = targetProvider == null ? null : targetProvider.getDirectoryPath(args.getTarget().getDirectory().getValue());

            // source handlers
            YADESourcePollingHandler sourcePolling = new YADESourcePollingHandler(args.getSource());
            YADESourceZeroByteFilesHandler sourceZeroBytes = new YADESourceZeroByteFilesHandler();

            // set YADE specific ProviderFile
            sourceProvider.setProviderFileCreator(builder -> new YADEProviderFile(builder.getFullPath(), builder.getSize(), builder
                    .getLastModifiedMillis(), args.getSource().isCheckSteadyStateEnabled()));

            /** 3) connect source provider */
            YADEProviderHelper.connect(logger, sourceProvider, args.getSource());

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

                    boolean shouldExecuteOperation = true;
                    try {
                        /** 7) handleZeroByteFiles on source - throws exception */
                        sourceFiles = sourceZeroBytes.filter(logger, sourceProvider, args.getSource(), sourceFiles);

                        /** 8) check forceFiles, resultSet conditions - throws exception */
                        YADEHelper.checkSourceFiles(sourceProvider, args, sourceFiles);
                    } catch (SOSYADEEngineSourceZeroByteFilesException e) {
                        logger.error("%s[%s]%s", sourceProvider.getContext().getLogPrefix(), sourcePolling.getMethod(), e.toString());
                        shouldExecuteOperation = false;
                    }

                    if (shouldExecuteOperation) {
                        /** 9) connect target provider */
                        YADEProviderHelper.connect(logger, targetProvider, args.getTarget());

                        if (sourcePolling.isFirstCycle()) {
                            /** 10) target provider create directories before commands */
                            YADEProviderHelper.createDirectoriesOnTarget(logger, targetProvider, args.getTarget(), targetDir);

                            // YADE 1 handling - execute before operation commands only once - is it OK????
                            /** 11) target provider execute commands before operation */
                            YADECommandsHandler.executeBeforeOperation(logger, targetProvider, args.getTarget());
                        }

                        try {
                            /** 12) execute operation, e.g. COPY,REMOVE,GETLIST etc */
                            YADEOperationHandler.execute(logger, args, sourceProvider, sourceDir, sourceFiles, targetProvider, targetDir);

                            /** 13) execute commands after operation on success */
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
                        } catch (Throwable e) {
                            logger.error(e);
                        } finally {
                            YADEProviderHelper.disconnect(targetProvider);
                        }
                    }

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

                    /** 8) check forceFiles, resultSet conditions - throws exception */
                    YADEHelper.checkSourceFiles(sourceProvider, args, sourceFiles);

                    /** 9) connect target provider - throws exception */
                    YADEProviderHelper.connect(logger, targetProvider, args.getTarget());

                    /** 10) target provider create directories before commands */
                    YADEProviderHelper.createDirectoriesOnTarget(logger, targetProvider, args.getTarget(), targetDir);

                    /** 11) target provider execute commands before operation */
                    YADECommandsHandler.executeBeforeOperation(logger, targetProvider, args.getTarget());

                    /** 12) execute operation, e.g. COPY,REMOVE,GETLIST etc - throws exception */
                    YADEOperationHandler.execute(logger, args, sourceProvider, sourceDir, sourceFiles, targetProvider, targetDir);

                    /** 13) execute commands after operation on success - throws exception */
                    // TODO handle command exceptions and throw later?
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

            YADEProviderHelper.disconnect(sourceProvider, targetProvider);

            // sendNotifications
            YADEHelper.printSummary(logger, args);
        }
    }

}
