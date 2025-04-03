package com.sos.yade.engine;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.engine.addons.YADEEngineJumpHostAddon;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.delegators.YADEProviderDelegatorFactory;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEArgumentsChecker;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.commons.helpers.YADEClientHelper;
import com.sos.yade.engine.commons.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.exceptions.YADEEngineException;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;
import com.sos.yade.engine.handlers.command.YADECommandExecutor;
import com.sos.yade.engine.handlers.command.YADECommandExecutor.YADECommandResult;
import com.sos.yade.engine.handlers.operations.YADEOperationsManager;
import com.sos.yade.engine.handlers.source.YADESourceFilesPolling;
import com.sos.yade.engine.handlers.source.YADESourceFilesSelector;
import com.sos.yade.engine.handlers.source.YADESourceFilesSteadyStateChecker;

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

    public YADEEngine() {
    }

    public List<ProviderFile> execute(ISOSLogger logger, AYADEArgumentsLoader argsLoader, boolean writeYADEBanner) throws YADEEngineException {

        YADESourceProviderDelegator sourceDelegator;
        YADETargetProviderDelegator targetDelegator;
        YADEEngineJumpHostAddon jumpHostAddon;
        try {

            /** 1) Write transfer configuration */
            YADEClientBannerWriter.writeHeader(logger, argsLoader, writeYADEBanner);

            /** 2) Check/Initialize configuration */
            YADEArgumentsChecker.validateOrExit(logger, argsLoader);

            /** 3) Set System properties */
            YADEClientHelper.setSystemPropertiesFromFiles(logger, argsLoader.getClientArgs());

            /** 4) Initialize and invoke a JumpHost add-on when Jump configuration is enabled */
            jumpHostAddon = YADEEngineJumpHostAddon.initialize(logger, argsLoader);
            if (jumpHostAddon != null) {
                jumpHostAddon.onBeforeDelegatorInitialized();
            }

            /** 5) Source/Target: create provider delegator */
            sourceDelegator = YADEProviderDelegatorFactory.createSourceDelegator(logger, argsLoader.getArgs(), argsLoader.getSourceArgs());
            targetDelegator = YADEProviderDelegatorFactory.createTargetDelegator(logger, argsLoader.getArgs(), argsLoader.getTargetArgs());
        } catch (YADEEngineInitializationException e) {
            throw e;
        } catch (Throwable e) {
            throw new YADEEngineInitializationException(e);
        }

        Duration operationDuration = null;
        Throwable exception = null;
        List<ProviderFile> files = null;

        String sourceExcludedFileExtension = YADESourceFilesSelector.getExcludedFileExtension(argsLoader.getArgs(), sourceDelegator, targetDelegator);
        // All steps may trigger an exception
        if (!argsLoader.getSourceArgs().isPollingEnabled()) {
            try {
                /** 6) Source: connect */
                YADEProviderDelegatorHelper.ensureConnected(logger, sourceDelegator);

                /** 7) Invoke a JumpHost add-on when Jump configuration is enabled */
                if (jumpHostAddon != null) {
                    jumpHostAddon.onAfterSourceDelegatorConnected(sourceDelegator);
                }

                /** 8) Source: execute commands before operation */
                YADECommandExecutor.executeBeforeOperation(logger, sourceDelegator);

                /** 9) Source: select files */
                files = YADESourceFilesSelector.selectFiles(logger, sourceDelegator, sourceExcludedFileExtension);

                /** 10) Source: check files steady state */
                YADESourceFilesSteadyStateChecker.check(logger, sourceDelegator, files);

                /** 11) Source: check zeroByteFiles, forceFiles, resultSet conditions */
                YADESourceFilesSelector.checkSelectionResult(logger, sourceDelegator, argsLoader.getClientArgs(), files);

                if (!files.isEmpty()) {
                    /** 12) Target: connect */
                    YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator);

                    /** 13) Invoke a JumpHost add-on when Jump configuration is enabled */
                    if (jumpHostAddon != null) {
                        jumpHostAddon.onAfterTargetDelegatorConnected(targetDelegator);
                    }

                    /** 14) Target: execute commands before operation */
                    YADECommandExecutor.executeBeforeOperation(logger, targetDelegator);

                    /** 15) Source/Target: process operation(COPY,MOVE,GETLIST,REMOVE) */
                    operationDuration = YADEOperationsManager.process(logger, argsLoader.getArgs(), argsLoader.getClientArgs(), sourceDelegator,
                            files, targetDelegator, cancel);
                }

                /** 16) Source/Target: execute commands after operation on success */
                YADECommandExecutor.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator);
            } catch (Throwable e) {
                onError(logger, sourceDelegator, targetDelegator, exception);
                exception = e;
            } finally {
                /** 14) Finalize */
                onFinally(logger, argsLoader, operationDuration, sourceDelegator, targetDelegator, jumpHostAddon, files, exception, true);
            }
        } else {
            YADESourceFilesPolling sourcePolling = new YADESourceFilesPolling(sourceDelegator);
            boolean addonExecutedAfterSourceDelegatorConnected = false;
            boolean addonExecutedAfterTargetDelegatorConnected = false;
            pl: while (true) {
                exception = null;
                operationDuration = null;

                sourcePolling.incrementCycleCounter();
                try {
                    /** 6) Source: connect/reconnect */
                    sourcePolling.ensureConnected(logger, sourceDelegator);

                    /** 7) Invoke a JumpHost add-on when Jump configuration is enabled */
                    if (jumpHostAddon != null && !addonExecutedAfterSourceDelegatorConnected) {
                        jumpHostAddon.onAfterSourceDelegatorConnected(sourceDelegator);
                        addonExecutedAfterSourceDelegatorConnected = true;
                    }

                    /** 8) Source: execute commands before operation */
                    YADECommandExecutor.executeBeforeOperation(logger, sourceDelegator);

                    /** 9) Source: select files */
                    files = sourcePolling.selectFiles(logger, sourceDelegator, sourceExcludedFileExtension);

                    /** 10) Source: check files steady state */
                    YADESourceFilesSteadyStateChecker.check(logger, sourceDelegator, files);

                    /** 11) Source: check zeroByteFiles, forceFiles, resultSet conditions */
                    YADESourceFilesSelector.checkSelectionResult(logger, sourceDelegator, argsLoader.getClientArgs(), files);

                    if (!files.isEmpty()) {
                        /** 12) Target: connect */
                        YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator);

                        /** 13) Invoke a JumpHost add-on when Jump configuration is enabled */
                        if (jumpHostAddon != null && !addonExecutedAfterTargetDelegatorConnected) {
                            jumpHostAddon.onAfterTargetDelegatorConnected(targetDelegator);
                            addonExecutedAfterTargetDelegatorConnected = true;
                        }

                        /** 14) Target: execute commands before operation */
                        YADECommandExecutor.executeBeforeOperation(logger, targetDelegator);

                        /** 15) Source/Target: process operation(COPY,MOVE,GETLIST,REMOVE) */
                        operationDuration = YADEOperationsManager.process(logger, argsLoader.getArgs(), argsLoader.getClientArgs(), sourceDelegator,
                                files, targetDelegator, cancel);
                    }

                    /** 16) Source/Target: execute commands after operation on success */
                    YADECommandExecutor.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator);
                } catch (Throwable e) {
                    onError(logger, sourceDelegator, targetDelegator, exception);
                    exception = e;
                } finally {
                    /** 17) Finalize */
                    boolean startNextPollingCycle = sourcePolling.startNextPollingCycle(logger);
                    onFinally(logger, argsLoader, operationDuration, sourceDelegator, targetDelegator, jumpHostAddon, files, exception,
                            !startNextPollingCycle);
                    if (!startNextPollingCycle) {
                        break pl;
                    }
                }
            }
        }
        return files;
    }

    /** Other thread */
    public void cancel(ISOSLogger logger) {
        logger.info("[cancel]...");
        cancel.set(true);

        // TODO wait ...
    }

    private void onError(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator,
            Throwable exception) {
        /** Source/Target: execute commands after operation on error */
        YADECommandResult r = YADECommandExecutor.executeAfterOperationOnError(logger, sourceDelegator, targetDelegator, exception);
        // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
        r.logIfErrorOnErrorLevel(logger);
        // r.logIfErrorOnInfoLevel(logger);
    }

    private void onFinally(ISOSLogger logger, AYADEArgumentsLoader argsLoader, Duration operationDuration,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, YADEEngineJumpHostAddon jumpHostAddon,
            List<ProviderFile> files, Throwable exception, boolean disconnectSource) throws YADEEngineException {
        /** Source/Target: execute commands after operation final */
        YADECommandResult r = YADECommandExecutor.executeAfterOperationFinal(logger, sourceDelegator, targetDelegator, exception);
        // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
        r.logIfErrorOnErrorLevel(logger);
        // r.logIfErrorOnInfoLevel(logger);

        if (jumpHostAddon != null) {
            jumpHostAddon.onBeforeDelegatorDisconnected(sourceDelegator, targetDelegator, disconnectSource);
        }

        if (disconnectSource) {
            YADEProviderDelegatorHelper.disconnect(sourceDelegator);
        }
        YADEProviderDelegatorHelper.disconnect(targetDelegator);

        if (exception == null) {
            try {
                YADEClientHelper.writeResultSet(logger, argsLoader.getArgs().getOperation().getValue(), argsLoader.getClientArgs(), files);
            } catch (Throwable e) {
                exception = e;
            }
        }

        // sendNotifications
        YADEClientBannerWriter.writeSummary(logger, argsLoader.getArgs(), operationDuration, targetDelegator == null ? null : targetDelegator
                .getArgs(), files, exception);

        // disconnectSource means - YADE execution(one-time operation or polling) is completed
        if (exception != null && disconnectSource) {
            if (exception instanceof YADEEngineException) {
                throw (YADEEngineException) exception;
            }
            throw new YADEEngineException(exception);
        }
    }

}
