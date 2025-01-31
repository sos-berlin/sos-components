package com.sos.yade.engine;

import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineSourceZeroByteFilesException;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler.YADECommandsResult;
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
 * - Commands<br/>
 * -- Names/Functionality - see com.sos.yade.engine.arguments.YADEProviderCommandArguments<br/>
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
        List<ProviderFile> files = null;
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
                // TODO check exception handling etc...
                pl: while (true) {
                    /** 5) select files on source */
                    files = sourcePolling.selectFiles(logger, sourceDelegator);

                    /** 6) check source files steady */
                    if (!YADESourceSteadyFilesHandler.checkFilesSteady(logger, sourceDelegator, files)) {
                        break pl;
                    }

                    boolean shouldExecuteOperation = true;
                    try {
                        /** 7) handleZeroByteFiles on source - throws exception */
                        files = sourceZeroBytes.filter(logger, sourceDelegator, files);

                        /** 8) check forceFiles, resultSet conditions - throws exception */
                        YADEHelper.checkSourceFiles(sourceDelegator, args, files);
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

                        exception = null;
                        try {
                            /** 12) execute operation: COPY,MOVE,REMOVE or GETLIST - throws exception */
                            YADECommonOperationHandler.execute(logger, args, sourceDelegator, files, targetDelegator);

                            /** 13) execute commands after operation on success */
                            try {
                                YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator);
                            } catch (Throwable e) {
                                logger.error("%s[%s]%s", sourceDelegator.getLogPrefix(), sourcePolling.getMethod(), e.toString());
                            }
                        } catch (Throwable e) {
                            exception = e;
                            logger.error(e);
                        } finally {
                            YADEDelegatorHelper.disconnect(targetDelegator);
                        }
                    }

                    if (!sourcePolling.startNextPollingCycle(logger)) {
                        break pl;
                    }

                    // sendNotifications
                    YADEHelper.printSummary(logger, args, files, exception);
                }
            } else {
                /** 5) select files on source */
                files = YADESourceFilesSelector.selectFiles(logger, sourceDelegator, false);

                /** 6) check source files steady */
                if (YADESourceSteadyFilesHandler.checkFilesSteady(logger, sourceDelegator, files)) {

                    /** 7) handleZeroByteFiles on source - throws exception */
                    files = sourceZeroBytes.filter(logger, sourceDelegator, files);

                    /** 8) check forceFiles, resultSet conditions - throws exception */
                    YADEHelper.checkSourceFiles(sourceDelegator, args, files);

                    /** 9) connect target provider - throws exception */
                    YADEDelegatorHelper.connect(logger, targetDelegator);

                    /** 10) target provider create directories before commands */
                    YADEDelegatorHelper.createDirectoriesOnTarget(logger, targetDelegator);

                    /** 11) target provider execute commands before operation */
                    YADECommandsHandler.executeBeforeOperation(logger, targetDelegator);

                    /** 12) execute operation: COPY,MOVE,REMOVE or GETLIST - throws exception */
                    YADECommonOperationHandler.execute(logger, args, sourceDelegator, files, targetDelegator);

                    /** 13) execute commands after operation on success - throws exception */
                    YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator);
                }
            }
        } catch (Throwable e) {
            /** execute commands after operation on error */
            YADECommandsResult r = YADECommandsHandler.executeAfterOperationOnError(logger, sourceDelegator, targetDelegator, e);
            // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
            r.logIfErrorOnErrorLevel(logger);
            // r.logIfErrorOnInfoLevel(logger);

            exception = e;
            throw e;
        } finally {
            /** execute commands after operation final */
            YADECommandsResult r = YADECommandsHandler.executeAfterOperationFinal(logger, sourceDelegator, targetDelegator, exception);
            // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
            r.logIfErrorOnErrorLevel(logger);
            // r.logIfErrorOnInfoLevel(logger);

            YADEDelegatorHelper.disconnect(sourceDelegator, targetDelegator);

            // sendNotifications
            YADEHelper.printSummary(logger, args, files, exception);
        }
    }

}
