package com.sos.yade.engine;

import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.delegators.YADEProviderFile;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineSourceZeroByteFilesException;
import com.sos.yade.engine.handlers.YADECommandsHandler;
import com.sos.yade.engine.handlers.operations.YADECommonOperationHandler;
import com.sos.yade.engine.handlers.source.YADESourceFilesSelector;
import com.sos.yade.engine.handlers.source.YADESourcePollingHandler;
import com.sos.yade.engine.handlers.source.YADESourceSteadyFilesHandler;
import com.sos.yade.engine.handlers.source.YADESourceZeroByteFilesHandler;
import com.sos.yade.engine.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.helpers.YADEDelegatorHelper;
import com.sos.yade.engine.helpers.YADEHelper;

/** TODO<br/>
 * - Arguments<br>
 * -- Resolve Environment and SOS Variables<br/>
 * - ResultSetFile: check YADE 1 behavior<br/>
 * -- deleteIfExists?<br/>
 * -- Full Path?<br/>
 * --- of a Source/Target file?<br />
 * ---- to check: YADE 1 seems to create resultSetFile before transfer (so for Source files...)<br/>
 * -- If 0 files were selected(not a GETLIST but e.g. COPY operation) ???<br/>
 * - JADE_REPORT_LOGGER YADE 1 extra LOGGER - JADE_REPORT_LOGGER (as HTML)<br/>
 * - ForceFiles:<br/>
 * -- YADE 1 checks after transfer... Why?<br/>
 * -- YADE 1 does not check when GETLIST and REMOVE - Why?<br/>
 * - Polling:<br/>
 * -- YADE 1 execute commands:<br/>
 * --- before operation(only once and not before each cycle) <br/>
 * --- after operation on success(after each cycle) <br/>
 * --- after operation on error (only if YADE(not a polling cycle) finished with an error) <br/>
 * --- after operation final (only if YADE(not a polling cycle) finished) <br/>
 * - DMZ<br/>
 */
public class YADEEngine {

    public YADEEngine() {
        // TODO
    }

    // static ???
    public void execute(ISOSLogger logger, YADEArguments args) throws Exception {
        YADESourceProviderDelegator sourceDelegator = null;
        YADETargetProviderDelegator targetDelegator = null;

        Throwable exception = null;
        try {
            /** 1) print transfer configuration */
            YADEHelper.printBanner(logger, args);

            /** 2) check/initialize configuration */
            YADEArgumentsHelper.checkOnStart(args);
            YADEHelper.setConfiguredSystemProperties(logger, args.getClient());

            // source/target initialize delegator/provider
            sourceDelegator = YADEDelegatorHelper.getSourceDelegator(logger, args);
            targetDelegator = YADEDelegatorHelper.getTargetDelegator(logger, args);

            // source handlers
            YADESourcePollingHandler sourcePolling = new YADESourcePollingHandler(sourceDelegator);
            YADESourceZeroByteFilesHandler sourceZeroBytes = new YADESourceZeroByteFilesHandler();

            /** 3) connect source provider */
            YADEDelegatorHelper.connect(logger, sourceDelegator);

            /** 4) source provider execute commands before operation */
            YADECommandsHandler.executeBeforeOperation(logger, sourceDelegator);

            if (sourcePolling.enabled()) {
                pl: while (true) {
                    /** 5) select files on source */
                    List<ProviderFile> sourceFiles = sourcePolling.selectFiles(logger, sourceDelegator);

                    /** 6) check source files steady */
                    if (!YADESourceSteadyFilesHandler.checkFilesSteady(logger, sourceDelegator, sourceFiles)) {
                        break pl;
                    }

                    boolean shouldExecuteOperation = true;
                    try {
                        /** 7) handleZeroByteFiles on source - throws exception */
                        sourceFiles = sourceZeroBytes.filter(logger, sourceDelegator, sourceFiles);

                        /** 8) check forceFiles, resultSet conditions - throws exception */
                        YADEHelper.checkSourceFiles(sourceDelegator, args, sourceFiles);
                    } catch (SOSYADEEngineSourceZeroByteFilesException e) {
                        logger.error("%s[%s]%s", sourceDelegator.getLogPrefix(), sourcePolling.getMethod(), e.toString());
                        shouldExecuteOperation = false;
                    }

                    if (shouldExecuteOperation) {
                        /** 9) connect target provider */
                        YADEDelegatorHelper.connect(logger, targetDelegator);

                        if (sourcePolling.isFirstCycle()) {
                            /** 10) target provider create directories before commands */
                            YADEDelegatorHelper.createDirectoriesOnTarget(logger, targetDelegator);

                            // YADE 1 handling - execute before operation commands only once - is it OK????
                            /** 11) target provider execute commands before operation */
                            YADECommandsHandler.executeBeforeOperation(logger, targetDelegator);
                        }

                        try {
                            /** 12) execute operation: COPY,MOVE,REMOVE or GETLIST - throws exception */
                            YADECommonOperationHandler.execute(logger, args, sourceDelegator, sourceFiles, targetDelegator);

                            /** 13) execute commands after operation on success */
                            try {
                                YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceDelegator);
                            } catch (Throwable e) {
                                logger.error("%s[%s]%s", sourceDelegator.getLogPrefix(), sourcePolling.getMethod(), e.toString());
                            }
                            try {
                                YADECommandsHandler.executeAfterOperationOnSuccess(logger, targetDelegator);
                            } catch (Throwable e) {
                                logger.error("%s[%s]%s", targetDelegator.getLogPrefix(), sourcePolling.getMethod(), e.toString());
                            }
                        } catch (Throwable e) {
                            logger.error(e);
                        } finally {
                            YADEDelegatorHelper.disconnect(targetDelegator);
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
                List<ProviderFile> sourceFiles = YADESourceFilesSelector.selectFiles(logger, (YADESourceProviderDelegator) sourceDelegator, false);

                /** 6) check source files steady */
                if (YADESourceSteadyFilesHandler.checkFilesSteady(logger, sourceDelegator, sourceFiles)) {

                    /** 7) handleZeroByteFiles on source - throws exception */
                    sourceFiles = sourceZeroBytes.filter(logger, (YADESourceProviderDelegator) sourceDelegator, sourceFiles);

                    /** 8) check forceFiles, resultSet conditions - throws exception */
                    YADEHelper.checkSourceFiles(sourceDelegator, args, sourceFiles);

                    /** 9) connect target provider - throws exception */
                    YADEDelegatorHelper.connect(logger, targetDelegator);

                    /** 10) target provider create directories before commands */
                    YADEDelegatorHelper.createDirectoriesOnTarget(logger, targetDelegator);

                    /** 11) target provider execute commands before operation */
                    YADECommandsHandler.executeBeforeOperation(logger, targetDelegator);

                    /** 12) execute operation: COPY,MOVE,REMOVE or GETLIST - throws exception */
                    YADECommonOperationHandler.execute(logger, args, sourceDelegator, sourceFiles, targetDelegator);

                    /** 13) execute commands after operation on success - throws exception */
                    // TODO handle command exceptions and throw later?
                    YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceDelegator);
                    YADECommandsHandler.executeAfterOperationOnSuccess(logger, targetDelegator);
                }
            }

        } catch (Throwable e) {
            /** execute commands after operation on error */
            YADECommandsHandler.executeAfterOperationOnError(logger, sourceDelegator, e);
            YADECommandsHandler.executeAfterOperationOnError(logger, targetDelegator, e);

            exception = e;
            throw e;
        } finally {
            /** execute commands after operation final */
            YADECommandsHandler.executeAfterOperationFinal(logger, sourceDelegator, exception);
            YADECommandsHandler.executeAfterOperationFinal(logger, targetDelegator, exception);

            YADEDelegatorHelper.disconnect(sourceDelegator, targetDelegator);

            // sendNotifications
            YADEHelper.printSummary(logger, args);
        }
    }

}
