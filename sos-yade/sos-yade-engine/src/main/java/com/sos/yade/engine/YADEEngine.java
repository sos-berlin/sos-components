package com.sos.yade.engine;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.sos.commons.mail.SOSMail;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSCollection;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.engine.addons.YADEEngineJumpHostAddon;
import com.sos.yade.engine.commons.YADEProviderFile;
import com.sos.yade.engine.commons.arguments.YADENotificationArguments;
import com.sos.yade.engine.commons.arguments.YADENotificationMailArguments;
import com.sos.yade.engine.commons.arguments.YADENotificationMailServerArguments;
import com.sos.yade.engine.commons.arguments.loaders.AYADEArgumentsLoader;
import com.sos.yade.engine.commons.delegators.YADEProviderDelegatorFactory;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.delegators.YADETargetProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEArgumentsChecker;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.commons.helpers.YADEClientHelper;
import com.sos.yade.engine.commons.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.commons.simulators.YADEProviderConnectivityFaultSimulator;
import com.sos.yade.engine.exceptions.YADEEngineException;
import com.sos.yade.engine.exceptions.YADEEngineInitializationException;
import com.sos.yade.engine.handlers.command.YADECommandExecutor;
import com.sos.yade.engine.handlers.command.YADECommandExecutor.YADECommandResult;
import com.sos.yade.engine.handlers.operations.YADEOperationsManager;
import com.sos.yade.engine.handlers.operations.copymove.file.commons.YADETargetProviderFile;
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

            /** 4) Initialize the JumpHost add-on when Jump configuration is enabled */
            jumpHostAddon = YADEEngineJumpHostAddon.initialize(logger, argsLoader);

            /** 5) Source/Target: create provider delegator */
            sourceDelegator = YADEProviderDelegatorFactory.createSourceDelegator(logger, argsLoader.getArgs(), argsLoader.getSourceArgs());
            targetDelegator = YADEProviderDelegatorFactory.createTargetDelegator(logger, argsLoader.getArgs(), argsLoader.getTargetArgs());
        } catch (YADEEngineInitializationException e) {
            throw e;
        } catch (Exception e) {
            throw new YADEEngineInitializationException(e);
        }

        /** 6) Source/Target (test mode only): initialize connectivity fault simulation */
        YADEProviderConnectivityFaultSimulator simulator = new YADEProviderConnectivityFaultSimulator();

        Duration operationDuration = null;
        Throwable exception = null;
        List<ProviderFile> files = null;

        boolean selectFiles = selectFiles(argsLoader, sourceDelegator, jumpHostAddon);
        String sourceExcludedFileExtension = YADESourceFilesSelector.getExcludedFileExtension(argsLoader.getArgs(), sourceDelegator, targetDelegator);
        // All steps may trigger an exception
        if (!argsLoader.getSourceArgs().isPollingEnabled()) {
            try {
                /** 7) Source: connect */
                YADEProviderDelegatorHelper.ensureConnected(logger, sourceDelegator, argsLoader.getArgs().getRetryOnConnectionError());

                /** 8) Source (test mode only): start connectivity fault simulation if enabled */
                simulator.simulate(logger, sourceDelegator);

                /** 9) Invoke a JumpHost add-on when Jump configuration is enabled */
                if (jumpHostAddon != null) {
                    jumpHostAddon.onAfterSourceDelegatorConnected(sourceDelegator);
                }

                /** 10) Source: execute commands before operation */
                YADECommandExecutor.executeBeforeOperation(logger, sourceDelegator, jumpHostAddon);

                if (selectFiles) {
                    /** 11) Source: select files */
                    files = YADESourceFilesSelector.selectFiles(logger, sourceDelegator, sourceExcludedFileExtension);

                    /** 12) Source: check files steady state */
                    YADESourceFilesSteadyStateChecker.check(logger, sourceDelegator, files);

                    /** 13) Source: check zeroByteFiles, forceFiles, resultSet conditions */
                    YADESourceFilesSelector.checkSelectionResult(logger, sourceDelegator, argsLoader.getClientArgs(), files);
                }
                if (!SOSCollection.isEmpty(files)) {
                    /** 14) Target: connect */
                    YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator, argsLoader.getArgs().getRetryOnConnectionError());

                    /** 15) Target (test mode only): start connectivity fault simulation if enabled */
                    simulator.simulate(logger, targetDelegator);

                    /** 16) Invoke a JumpHost add-on when Jump configuration is enabled */
                    if (jumpHostAddon != null) {
                        jumpHostAddon.onAfterTargetDelegatorConnected(targetDelegator);
                    }

                    /** 17) Target: execute commands before operation */
                    YADECommandExecutor.executeBeforeOperation(logger, targetDelegator);

                    /** 18) Source/Target: process operation(COPY,MOVE,GETLIST,REMOVE) */
                    operationDuration = YADEOperationsManager.process(logger, argsLoader.getArgs(), argsLoader.getClientArgs(), sourceDelegator,
                            targetDelegator, files, cancel);
                }

                /** 19) Source/Target: execute commands after operation on success */
                YADECommandExecutor.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator, argsLoader.getArgs()
                        .getRetryOnConnectionError());
            } catch (Exception e) {
                onError(logger, argsLoader, sourceDelegator, targetDelegator, exception);
                exception = e;
            } finally {
                /** 20) Source/Target (test mode only) - stop connectivity fault simulation if enabled */
                simulator.shutdown(logger);

                /** 21) Finalize */
                onFinally(logger, argsLoader, operationDuration, sourceDelegator, targetDelegator, jumpHostAddon, files, exception, true);
            }
        } else {
            YADESourceFilesPolling sourcePolling = new YADESourceFilesPolling(sourceDelegator);
            boolean jumpHostAddonExecutedAfterSourceDelegatorConnected = false;
            boolean jumpHostAddonExecutedAfterTargetDelegatorConnected = false;
            pl: while (true) {
                exception = null;
                operationDuration = null;

                sourcePolling.incrementCycleCounter();
                try {
                    /** 7) Source: connect/reconnect */
                    sourcePolling.ensureConnected(logger, sourceDelegator);

                    /** 8) Source (test mode only): start connectivity fault simulation if enabled */
                    simulator.simulate(logger, sourceDelegator);

                    /** 9) Invoke a JumpHost add-on when Jump configuration is enabled */
                    if (jumpHostAddon != null && !jumpHostAddonExecutedAfterSourceDelegatorConnected) {
                        jumpHostAddon.onAfterSourceDelegatorConnected(sourceDelegator);
                        jumpHostAddonExecutedAfterSourceDelegatorConnected = true;
                    }

                    /** 10) Source: execute commands before operation */
                    YADECommandExecutor.executeBeforeOperation(logger, sourceDelegator, jumpHostAddon);

                    if (selectFiles) {
                        /** 11) Source: select files */
                        files = sourcePolling.selectFiles(logger, sourceDelegator, sourceExcludedFileExtension);

                        /** 12) Source: check files steady state */
                        YADESourceFilesSteadyStateChecker.check(logger, sourceDelegator, files);

                        /** 13) Source: check zeroByteFiles, forceFiles, resultSet conditions */
                        YADESourceFilesSelector.checkSelectionResult(logger, sourceDelegator, argsLoader.getClientArgs(), files);
                    }
                    if (!SOSCollection.isEmpty(files)) {
                        /** 14) Target: connect */
                        YADEProviderDelegatorHelper.ensureConnected(logger, targetDelegator, argsLoader.getArgs().getRetryOnConnectionError());

                        /** 15) Target (test mode only): start connectivity fault simulation if enabled */
                        simulator.simulate(logger, targetDelegator);

                        /** 16) Invoke a JumpHost add-on when Jump configuration is enabled */
                        if (jumpHostAddon != null && !jumpHostAddonExecutedAfterTargetDelegatorConnected) {
                            jumpHostAddon.onAfterTargetDelegatorConnected(targetDelegator);
                            jumpHostAddonExecutedAfterTargetDelegatorConnected = true;
                        }

                        /** 17) Target: execute commands before operation */
                        YADECommandExecutor.executeBeforeOperation(logger, targetDelegator);

                        /** 18) Source/Target: process operation(COPY,MOVE,GETLIST,REMOVE) */
                        operationDuration = YADEOperationsManager.process(logger, argsLoader.getArgs(), argsLoader.getClientArgs(), sourceDelegator,
                                targetDelegator, files, cancel);
                    }

                    /** 19) Source/Target: execute commands after operation on success */
                    YADECommandExecutor.executeAfterOperationOnSuccess(logger, sourceDelegator, targetDelegator, argsLoader.getArgs()
                            .getRetryOnConnectionError());
                } catch (Exception e) {
                    onError(logger, argsLoader, sourceDelegator, targetDelegator, exception);
                    exception = e;
                } finally {
                    /** 20) Source/Target (test mode only) - stop connectivity fault simulation if enabled */
                    simulator.shutdown(logger);

                    /** 21) Finalize */
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

    private boolean selectFiles(AYADEArgumentsLoader argsLoader, YADESourceProviderDelegator sourceDelegator, YADEEngineJumpHostAddon jumpHostAddon) {
        if (jumpHostAddon != null && sourceDelegator.isJumpHost()) {
            // Current Source is Jump Host - if GETLIST or REMOVE - no selection should be performed because it happens on the really Source(SSHProvider)
            if (argsLoader.getArgs().isOperationGETLIST() || argsLoader.getArgs().isOperationREMOVE()) {
                return false;
            }
        }
        return true;
    }

    private void onError(ISOSLogger logger, AYADEArgumentsLoader argsLoader, YADESourceProviderDelegator sourceDelegator,
            YADETargetProviderDelegator targetDelegator, Throwable exception) {
        /** Source/Target: execute commands after operation on error */
        YADECommandResult r = YADECommandExecutor.executeAfterOperationOnError(logger, sourceDelegator, targetDelegator, exception, argsLoader
                .getArgs().getRetryOnConnectionError());
        // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
        r.logIfErrorOnErrorLevel(logger);
        // r.logIfErrorOnInfoLevel(logger);
    }

    private void onFinally(ISOSLogger logger, AYADEArgumentsLoader argsLoader, Duration operationDuration,
            YADESourceProviderDelegator sourceDelegator, YADETargetProviderDelegator targetDelegator, YADEEngineJumpHostAddon jumpHostAddon,
            List<ProviderFile> files, Throwable exception, boolean disconnectSource) throws YADEEngineException {
        /** Source/Target: execute commands after operation final */
        YADECommandResult r = YADECommandExecutor.executeAfterOperationFinal(logger, sourceDelegator, targetDelegator, exception, argsLoader.getArgs()
                .getRetryOnConnectionError());
        // YADE1 behavior - TODO or provide possible commands exceptions to printSummary?
        r.logIfErrorOnErrorLevel(logger);
        // r.logIfErrorOnInfoLevel(logger);

        List<Throwable> exceptions = new ArrayList<>();
        if (exception != null) {
            exceptions.add(exception);
        }

        if (jumpHostAddon != null) {
            try {
                jumpHostAddon.onBeforeDelegatorDisconnected(sourceDelegator, targetDelegator, files, exception == null, disconnectSource);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        if (disconnectSource) {
            YADEProviderDelegatorHelper.disconnect(sourceDelegator);
        }
        YADEProviderDelegatorHelper.disconnect(targetDelegator);

        if (exceptions.size() == 0) {
            try {
                YADEClientHelper.writeResultSet(logger, argsLoader.getArgs().getOperation().getValue(), argsLoader.getClientArgs(), files);
            } catch (Exception e) {
                exceptions.add(e);
            }
        }

        sendNotifications(logger, argsLoader, files, exceptions);
        YADEClientBannerWriter.writeSummary(logger, argsLoader.getArgs(), operationDuration, sourceDelegator, targetDelegator, jumpHostAddon, files,
                exception);

        // disconnectSource means - YADE execution(one-time operation or polling) is completed
        if (exceptions.size() > 0 && disconnectSource) {
            if (exceptions.size() == 1) {
                Throwable e = exceptions.get(0);
                if (e instanceof YADEEngineException) {
                    throw (YADEEngineException) e;
                } else {
                    throw new YADEEngineException(e);
                }
            } else {
                StringBuilder msg = new StringBuilder();
                for (Throwable e : exceptions) {
                    msg.append("[").append(e.toString()).append("]");
                }
                throw new YADEEngineException(msg.toString(), exceptions.get(0));
            }
        }
    }

    // TODO currently - quick solution - reorganize code ...
    private void sendNotifications(ISOSLogger logger, AYADEArgumentsLoader argsLoader, List<ProviderFile> files, List<Throwable> exceptions) {
        if (argsLoader.getNotificationArgs() == null || !argsLoader.getNotificationArgs().isEnabled()) {
            return;
        }

        YADENotificationArguments args = argsLoader.getNotificationArgs();
        YADENotificationMailServerArguments mailServerArgs = args.getMailServer();
        if (exceptions.size() > 0) {
            if (args.getMailOnError() == null) {
                return;
            }
            // onError
            YADENotificationMailArguments mailArgs = args.getMailOnError();
            String body = mailArgs.getBody().getValue() == null ? "" : mailArgs.getBody().getValue() + mailArgs.getNewLine();

            StringBuilder sb = new StringBuilder();
            sb.append(body);
            for (Throwable e : exceptions) {
                sb.append(SOSClassUtil.getStackTrace(e, mailArgs.getNewLine()));
            }
            mailArgs.getBody().setValue(sb.toString());
            sendMail(logger, mailServerArgs, mailArgs, YADENotificationArguments.LABEL_ON_ERROR);
        } else {
            // onSuccess
            if (argsLoader.getNotificationArgs().getMailOnSuccess() != null) {
                YADENotificationMailArguments mailArgs = args.getMailOnSuccess();
                String body = mailArgs.getBody().getValue() == null ? "" : mailArgs.getBody().getValue() + mailArgs.getNewLine();

                StringBuilder sb = new StringBuilder();
                for (ProviderFile f : files) {
                    YADEProviderFile file = (YADEProviderFile) f;
                    YADETargetProviderFile targetFile = file.getTarget();
                    sb.append(file.getFinalFullPath());
                    if (targetFile == null) {
                        if (file.getState() != null) {
                            sb.append("[").append(YADEClientBannerWriter.formatState(file.getState())).append("]");
                        }
                        if (file.getSubState() != null) {
                            sb.append("[").append(YADEClientBannerWriter.formatState(file.getSubState())).append("]");
                        }
                    } else {
                        if (targetFile.getState() != null) {
                            sb.append("[").append(YADEClientBannerWriter.formatState(targetFile.getState())).append("]");
                        }
                        if (targetFile.getSubState() != null) {
                            sb.append("[").append(YADEClientBannerWriter.formatState(targetFile.getSubState())).append("]");
                        }
                    }
                    sb.append("[Bytes ").append(file.getSize()).append("]");
                    sb.append(mailArgs.getNewLine());
                }

                mailArgs.getBody().setValue(body + sb.toString());
                sendMail(logger, mailServerArgs, mailArgs, YADENotificationArguments.LABEL_ON_SUCCESS);
            }
            // onEmptyFiles
            if (argsLoader.getNotificationArgs().getMailOnEmptyFiles() != null) {
                YADENotificationMailArguments mailArgs = args.getMailOnEmptyFiles();
                String body = mailArgs.getBody().getValue() == null ? "" : mailArgs.getBody().getValue() + mailArgs.getNewLine();

                StringBuilder sb = new StringBuilder();
                int counterEmpyFiles = 0;
                for (ProviderFile f : files) {
                    YADEProviderFile file = (YADEProviderFile) f;
                    if (file.getSize() > 0) {
                        continue;
                    }

                    counterEmpyFiles++;
                    YADETargetProviderFile targetFile = file.getTarget();
                    sb.append(file.getFinalFullPath());
                    if (targetFile == null) {
                        if (file.getState() != null) {
                            sb.append("[").append(YADEClientBannerWriter.formatState(file.getState())).append("]");
                        }
                        if (file.getSubState() != null) {
                            sb.append("[").append(YADEClientBannerWriter.formatState(file.getSubState())).append("]");
                        }
                    } else {
                        if (targetFile.getState() != null) {
                            sb.append("[").append(YADEClientBannerWriter.formatState(targetFile.getState())).append("]");
                        }
                        if (targetFile.getSubState() != null) {
                            sb.append("[").append(YADEClientBannerWriter.formatState(targetFile.getSubState())).append("]");
                        }
                    }
                    sb.append("[Bytes ").append(file.getSize()).append("]");
                    sb.append(mailArgs.getNewLine());
                }
                if (counterEmpyFiles > 0) {
                    mailArgs.getBody().setValue(body + sb.toString());
                    sendMail(logger, mailServerArgs, mailArgs, YADENotificationArguments.LABEL_ON_EMPTY_FILES);
                }
            }
        }
    }

    private void sendMail(ISOSLogger logger, YADENotificationMailServerArguments mailServerArgs, YADENotificationMailArguments mailArgs,
            String label) {
        try {
            SOSMail mail = new SOSMail(mailServerArgs.getMailSettings());

            mail.setFrom(mailArgs.getHeaderFrom().getValue());
            for (String to : mailArgs.getHeaderTo().getValue()) {
                mail.addRecipient(to);
            }
            if (!mailArgs.getHeaderCC().isEmpty()) {
                for (String cc : mailArgs.getHeaderCC().getValue()) {
                    mail.addCC(cc);
                }
            }
            if (!mailArgs.getHeaderBCC().isEmpty()) {
                for (String bcc : mailArgs.getHeaderBCC().getValue()) {
                    mail.addBCC(bcc);
                }
            }
            if (!mailArgs.getAttachment().isEmpty()) {
                for (Path attachment : mailArgs.getAttachment().getValue()) {
                    mail.addAttachment(SOSPath.toAbsoluteNormalizedPath(attachment).toString());
                }
            }

            mail.setSubject(mailArgs.getHeaderSubject().getValue() == null ? label : mailArgs.getHeaderSubject().getValue());
            mail.setBody(mailArgs.getBody().getValue());

            mail.setContentType(mailArgs.getContentType().getValue());
            mail.setEncoding(mailArgs.getEncoding().getValue());

            if (!mailServerArgs.getQueueDirectory().isEmpty()) {
                mail.setQueueMailOnError(true);
                mail.setQueueDir(mailServerArgs.getQueueDirectory().getValue());
            }

            // on error an exception is thrown or, if queueMailOnError is enabled, false is returned
            if (!mail.send(logger)) {
                logger.warn("[sendMail]failed");
            }
        } catch (Exception e) {
            logger.warn("[sendMail]" + e, e);
        }

    }

}
