package com.sos.yade.engine;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.common.arguments.YADEArguments;
import com.sos.yade.engine.common.arguments.YADEClientArguments;
import com.sos.yade.engine.common.arguments.YADESourceArguments;
import com.sos.yade.engine.common.arguments.YADETargetArguments;
import com.sos.yade.engine.common.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.common.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.common.helpers.YADEArgumentsChecker;
import com.sos.yade.engine.common.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.common.helpers.YADEClientHelper;
import com.sos.yade.engine.common.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.exceptions.YADEEngineException;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler;
import com.sos.yade.engine.handlers.commands.YADECommandsHandler.YADECommandsResult;
import com.sos.yade.engine.handlers.operations.YADEOperationsManager;
import com.sos.yade.engine.handlers.source.YADESourceFilesSelector;
import com.sos.yade.engine.handlers.source.YADESourceFilesSteadyStateChecker;
import com.sos.yade.engine.handlers.source.YADESourcePollingHandler;

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
 * - Cumulative file(file with extension) and Compress - should the Compress extension be used or not? <br/>
 * - CumulativeFileSeparator - TODO replace variables .... XML Schema description for CumulativeFileSeparator is wrong<br/>
 * - DMZ<br/>
 */
public class YADEEngine {

    private AtomicBoolean cancel = new AtomicBoolean(false);
    private final Instant start;

    public YADEEngine() {
        start = Instant.now();
    }

    // static ???
    public void execute(ISOSLogger logger, YADEArguments args, YADEClientArguments clientArgs, YADESourceArguments sourceArgs,
            YADETargetArguments targetArgs, boolean writeYADEBanner) throws YADEEngineException {

        YADESourceProviderDelegator sourceDelegator = null;
        YADETargetProviderDelegator targetDelegator = null;
        try {
            /** 1) Write transfer configuration */
            YADEClientBannerWriter.writeHeader(logger, args, clientArgs, sourceArgs, targetArgs, writeYADEBanner);

            /** 2) Check/Initialize configuration */
            YADEArgumentsChecker.validateOrExit(logger, args, clientArgs, sourceArgs, targetArgs);

            // Source/Target: initialize delegator/provider
            sourceDelegator = YADEProviderDelegatorHelper.initializeSourceDelegator(logger, sourceArgs);
            targetDelegator = YADEProviderDelegatorHelper.initializeTargetDelegator(logger, args, targetArgs);
        } catch (YADEEngineInitializationException e) {
            throw e;
        } catch (Throwable e) {
            throw new YADEEngineInitializationException(e);
        }

        Duration operationDuration = null;
        Throwable exception = null;
        List<ProviderFile> files = null;

        String sourceExcludedFileExtension = YADESourceFilesSelector.getExcludedFileExtension(args, sourceDelegator, targetDelegator);
        // All steps may trigger an exception
        if (!sourceArgs.isPollingEnabled()) {
            try {
                /** 3) Source: connect */
                YADEProviderDelegatorHelper.ensureConnected(logger, sourceDelegator);

                /** 4) Source: execute commands before operation */
                YADECommandsHandler.executeBeforeOperation(logger, sourceDelegator);

                /** 5) Source: select files */
                files = YADESourceFilesSelector.selectFiles(logger, sourceDelegator, sourceExcludedFileExtension);

                /** 6) Source: check files steady state */
                YADESourceFilesSteadyStateChecker.check(logger, sourceDelegator, files);

                /** 7) Source: check zeroByteFiles, forceFiles, resultSet conditions */
                YADESourceFilesSelector.checkSelectionResult(logger, sourceDelegator, clientArgs, files);

                if (!files.isEmpty()) {
                    /** 8) Target: connect */
                    YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator);

                    /** 9) Target: execute commands before operation */
                    YADECommandsHandler.executeBeforeOperation(logger, targetDelegator);

                    /** 10) Source/Target: process operation(COPY,MOVE,GETLIST,REMOVE) */
                    operationDuration = YADEOperationsManager.process(logger, args, clientArgs, sourceDelegator, files, targetDelegator, cancel);
                }

                /** 11) Source/Target: execute commands after operation on success */
                YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator);
            } catch (Throwable e) {
                onError(logger, sourceDelegator, targetDelegator, exception);
                exception = e;
            } finally {
                onFinally(logger, operationDuration, args, clientArgs, sourceDelegator, targetDelegator, files, exception, true);
            }
        } else {
            YADESourcePollingHandler sourcePolling = new YADESourcePollingHandler(sourceDelegator);
            pl: while (true) {
                exception = null;
                operationDuration = null;

                sourcePolling.incrementCycleCounter();
                try {
                    /** 3) Source: connect/reconnect */
                    sourcePolling.ensureConnected(logger, sourceDelegator);

                    /** 4) Source: execute commands before operation */
                    YADECommandsHandler.executeBeforeOperation(logger, sourceDelegator);

                    /** 5) Source: select files */
                    files = sourcePolling.selectFiles(logger, sourceDelegator, sourceExcludedFileExtension);

                    /** 6) Source: check files steady state */
                    YADESourceFilesSteadyStateChecker.check(logger, sourceDelegator, files);

                    /** 7) Source: check zeroByteFiles, forceFiles, resultSet conditions */
                    YADESourceFilesSelector.checkSelectionResult(logger, sourceDelegator, clientArgs, files);

                    if (!files.isEmpty()) {
                        /** 8) Target: connect */
                        YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator);

                        /** 9) Target: execute commands before operation */
                        YADECommandsHandler.executeBeforeOperation(logger, targetDelegator);

                        /** 10) Source/Target: process operation(COPY,MOVE,GETLIST,REMOVE) */
                        operationDuration = YADEOperationsManager.process(logger, args, clientArgs, sourceDelegator, files, targetDelegator, cancel);
                    }

                    /** 11) Source/Target: execute commands after operation on success */
                    YADECommandsHandler.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator);
                } catch (Throwable e) {
                    onError(logger, sourceDelegator, targetDelegator, exception);
                    exception = e;
                } finally {
                    boolean startNextPollingCycle = sourcePolling.startNextPollingCycle(logger);
                    onFinally(logger, operationDuration, args, clientArgs, sourceDelegator, targetDelegator, files, exception,
                            !startNextPollingCycle);
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

    private void onFinally(ISOSLogger logger, Duration operationDuration, YADEArguments args, YADEClientArguments clientArgs,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, List<ProviderFile> files, Throwable exception,
            boolean disconnectSource) throws YADEEngineException {
        /** Source/Target: execute commands after operation final */
        YADECommandsResult r = YADECommandsHandler.executeAfterOperationFinal(logger, sourceDelegator, targetDelegator, exception);
        // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
        r.logIfErrorOnErrorLevel(logger);
        // r.logIfErrorOnInfoLevel(logger);

        if (disconnectSource) {
            YADEProviderDelegatorHelper.disconnect(sourceDelegator);
        }
        YADEProviderDelegatorHelper.disconnect(targetDelegator);

        if (exception == null) {
            try {
                YADEClientHelper.writeResultSet(logger, args.getOperation().getValue(), clientArgs, files);
            } catch (Throwable e) {
                exception = e;
            }
        }

        // sendNotifications
        YADEClientBannerWriter.writeSummary(logger, start, operationDuration, args, targetDelegator == null ? null : targetDelegator.getArgs(), files,
                exception);

        // disconnectSource means - YADE execution(one-time operation or polling) is completed
        if (exception != null && disconnectSource) {
            if (exception instanceof YADEEngineException) {
                throw (YADEEngineException) exception;
            }
            throw new YADEEngineException(exception);
        }
    }

}
