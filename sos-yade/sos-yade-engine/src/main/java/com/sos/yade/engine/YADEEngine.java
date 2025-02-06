package com.sos.yade.engine;

import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.arguments.YADEArguments;
import com.sos.yade.engine.arguments.YADEClientArguments;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.arguments.YADETargetArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineException;
import com.sos.yade.engine.exceptions.SOSYADEEngineInitializationException;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler.YADECommandsResult;
import com.sos.yade.engine.handlers.operations.YADECommonOperationHandler;
import com.sos.yade.engine.handlers.source.YADESourceFilesSelector;
import com.sos.yade.engine.handlers.source.YADESourcePollingHandler;
import com.sos.yade.engine.handlers.source.YADESourceSteadyFilesHandler;
import com.sos.yade.engine.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.helpers.YADEDelegatorHelper;
import com.sos.yade.engine.helpers.YADEHelper;

/** Different/incompatible with YADE 1:<br/>
 * -------------------------------------------------------------------------------------------------<br/>
 * - Polling (polling_server): Before/After operation commands execution<br/>
 * -- YADE 1<br/>
 * --- execute BeforeOperation commands only once when the program starts<br/>
 * --- execute AfterOperationOnSuccess commands after each operation execution<br/>
 * --- execute AfterOperationOnError commands only once at the end of the program<br/>
 * --- execute AfterOperationFinal commands only once at the end of the program<br/>
 * - YADE JS7<br/>
 * -- Before/After operation commands are executed at each operation execution<br/>
 * -------------------------------------------------------------------------------------------------<br/>
 * - Source file selection check for zero_byte_transfer=RELAXED (no transfer zero byte files):<br/>
 * -- YADE 1<br/>
 * -- force_files, min_files etc. checks are based on selection including zero byte files, e.g.:<br/>
 * --- force_files=true, 1 zero byte source file found<br/>
 * ---- an operation execution is skipped for this file due to zero_byte_transfer=RELAXED<br/>
 * ---- no force_files exception is thrown<br/>
 * --- min_files=10, 10 files selected on the source, 3 of which are zero byte files<br/>
 * ---- an operation execution is performed for 7 non zero byte files<br/>
 * ---- no min_files exception is thrown<br/>
 * -- YADE JS7<br/>
 * --- the source files selection takes zero_byte_transfer=RELAXED into account and in this case only selects the non zero byte files files<br/>
 * --- all further source files checks(force_files etc.) are based on this result<br/>
 * 
 * -------------------------------------------------------------------------------------------------<br/>
 * TODO<br/>
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
 * - Commands<br/>
 * -- Names/Functionality - see com.sos.yade.engine.arguments.YADEProviderCommandArguments<br/>
 * - DMZ<br/>
 */
public class YADEEngine {

    public YADEEngine() {
        // TODO
    }

    // static ???
    public void execute(ISOSLogger logger, YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourceArgs,
            YADETargetArguments targetArgs) throws SOSYADEEngineException {
        YADESourceProviderDelegator sourceDelegator = null;
        YADETargetProviderDelegator targetDelegator = null;
        // Source handlers
        YADESourcePollingHandler sourcePolling = null;
        try {
            /** 1) print transfer configuration */
            YADEHelper.printBanner(logger, args);

            /** 2) check/initialize configuration */
            YADEArgumentsHelper.checkConfiguration(logger, args, sourceArgs);
            YADEHelper.setConfiguredSystemProperties(logger, clientArgs);

            // Source/Target initialize delegator/provider
            sourceDelegator = YADEDelegatorHelper.getSourceDelegator(logger, sourceArgs);
            targetDelegator = YADEDelegatorHelper.getTargetDelegator(logger, args, targetArgs);

            sourcePolling = new YADESourcePollingHandler(sourceDelegator);
            sourcePolling.checkConfiguration(sourceDelegator);
        } catch (Throwable e) {
            // TODO printSummary???
            throw new SOSYADEEngineInitializationException(e);
        }

        Throwable exception = null;
        List<ProviderFile> files = null;
        // All steps may trigger an exception
        if (!sourcePolling.enabled()) {
            try {
                /** 3) Source: connect */
                YADEDelegatorHelper.connect(logger, sourceDelegator);

                /** 4) Source: execute commands before operation */
                YADECommandsHandler.executeBeforeOperation(logger, sourceDelegator);

                /** 5) Source: select files */
                files = YADESourceFilesSelector.selectFiles(logger, sourceDelegator, false);

                /** 6) Source: check files steady */
                YADESourceSteadyFilesHandler.checkFilesSteady(logger, sourceDelegator, files);

                /** 7) Source: check zeroByteFiles, forceFiles, resultSet conditions */
                YADESourceFilesSelector.checkSelectionResult(logger, sourceDelegator, clientArgs, files);

                /** 8) Target: connect */
                YADEDelegatorHelper.connect(logger, targetDelegator);

                /** 9) Target: create directories before commands */
                YADEDelegatorHelper.createDirectoriesOnTarget(logger, targetDelegator);

                /** 10) Target: execute commands before operation */
                YADECommandsHandler.executeBeforeOperation(logger, targetDelegator);

                /** 11) Source/Target: execute operation(COPY,MOVE,REMOVE,GETLIST) */
                YADECommonOperationHandler.execute(logger, args, clientArgs, sourceDelegator, files, targetDelegator);

                /** 12) Source/Target: execute commands after operation on success */
                YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator);
            } catch (Throwable e) {
                onError(logger, sourceDelegator, targetDelegator, exception);
                exception = e;
            } finally {
                onFinally(logger, args, sourceDelegator, targetDelegator, files, exception, true);
            }

        } else {
            pl: while (true) {
                exception = null;
                sourcePolling.incrementCycleCounter();
                try {
                    /** 3) Source: connect */
                    sourcePolling.ensureConnected(logger, sourceDelegator);

                    /** 4) Source: execute commands before operation */
                    YADECommandsHandler.executeBeforeOperation(logger, sourceDelegator);

                    /** 5) Source: select files */
                    files = sourcePolling.selectFiles(logger, sourceDelegator);

                    /** 6) Source: check files steady */
                    YADESourceSteadyFilesHandler.checkFilesSteady(logger, sourceDelegator, files);

                    /** 7) Source: check zeroByteFiles, forceFiles, resultSet conditions */
                    YADESourceFilesSelector.checkSelectionResult(logger, sourceDelegator, clientArgs, files);

                    /** 8) Target: connect */
                    YADEDelegatorHelper.connect(logger, targetDelegator);

                    /** 9) Target: create directories before commands */
                    YADEDelegatorHelper.createDirectoriesOnTarget(logger, targetDelegator);

                    /** 10) Target: execute commands before operation */
                    YADECommandsHandler.executeBeforeOperation(logger, targetDelegator);

                    /** 11) Source/Target: execute operation(COPY,MOVE,REMOVE,GETLIST) */
                    YADECommonOperationHandler.execute(logger, args, clientArgs, sourceDelegator, files, targetDelegator);

                    /** 12) Source/Target: execute commands after operation on success */
                    YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator);

                } catch (Throwable e) {
                    onError(logger, sourceDelegator, targetDelegator, exception);
                    exception = e;
                } finally {
                    boolean startNextPollingCycle = sourcePolling.startNextPollingCycle(logger);
                    onFinally(logger, args, sourceDelegator, targetDelegator, files, exception, !startNextPollingCycle);
                    if (!startNextPollingCycle) {
                        break pl;
                    }
                }
            }
        }
    }

    private void onError(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            Throwable exception) {
        /** Source/Target: execute commands after operation on error */
        YADECommandsResult r = YADECommandsHandler.executeAfterOperationOnError(logger, sourceDelegator, targetDelegator, exception);
        // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
        r.logIfErrorOnErrorLevel(logger);
        // r.logIfErrorOnInfoLevel(logger);
    }

    private void onFinally(ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, List<ProviderFile> files, Throwable exception, boolean disconnectSource)
            throws SOSYADEEngineException {
        /** Source/Target: execute commands after operation final */
        YADECommandsResult r = YADECommandsHandler.executeAfterOperationFinal(logger, sourceDelegator, targetDelegator, exception);
        // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
        r.logIfErrorOnErrorLevel(logger);
        // r.logIfErrorOnInfoLevel(logger);

        if (disconnectSource) {
            YADEDelegatorHelper.disconnect(sourceDelegator);
        }
        YADEDelegatorHelper.disconnect(targetDelegator);

        // sendNotifications
        YADEHelper.printSummary(logger, args, files, exception);

        // disconnectSource means - YADE execution(one-time operation or polling) is completed
        if (exception != null && disconnectSource) {
            if (exception instanceof SOSYADEEngineException) {
                throw (SOSYADEEngineException) exception;
            }
            throw new SOSYADEEngineException(exception);
        }
    }

}
