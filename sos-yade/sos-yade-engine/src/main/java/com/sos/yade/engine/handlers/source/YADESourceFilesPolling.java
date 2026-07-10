package com.sos.yade.engine.handlers.source;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.arguments.base.SOSArgumentHelper;
import com.sos.commons.util.loggers.base.ISOSLogger;
import com.sos.commons.vfs.commons.file.ProviderFile;
import com.sos.yade.engine.commons.arguments.YADEArguments;
import com.sos.yade.engine.commons.arguments.YADESourceArguments;
import com.sos.yade.engine.commons.arguments.YADESourcePollingArguments;
import com.sos.yade.engine.commons.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.commons.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.commons.helpers.YADEClientBannerWriter;
import com.sos.yade.engine.commons.helpers.YADEClientHelper;
import com.sos.yade.engine.commons.helpers.YADEProviderDelegatorHelper;
import com.sos.yade.engine.exceptions.YADEEngineSourcePollingException;

public class YADESourceFilesPolling {

    private enum PollingMethod {
        Timeout, ServerDuration, Forever
    }

    private static final int POLLING_MAX_RETRIES_ON_CONNECTION_ERROR = 1_000;
    private static final int WAIT_SECONDS_ON_CONNECTION_ERROR = 10;

    private YADESourceArguments args;
    private PollingMethod method;
    private String mainLogPrefix;
    private String logPrefix;

    private Instant start;
    private long pollInterval;
    private long pollTimeout;
    private long serverDuration;
    private long totalFilesCount;
    private int cycleCounter;

    private boolean ensureConnectedOnStartExecuted;

    public YADESourceFilesPolling(YADESourceProviderDelegator sourceDelegator) {
        this.args = sourceDelegator.getArgs();

        initMethod();
        setMainLogPrefix(sourceDelegator);

        this.start = Instant.now();
    }

    public YADESourceProviderDelegator ensureConnectedOnStart(ISOSLogger logger, YADEArguments args, YADESourceProviderDelegator sourceDelegator)
            throws YADEEngineSourcePollingException {
        if (ensureConnectedOnStartExecuted) {
            ensureConnected(logger, sourceDelegator);
        } else {
            int count = 0;
            while (!ensureConnectedOnStartExecuted) {
                count++;
                try {
                    sourceDelegator = (YADESourceProviderDelegator) YADEProviderDelegatorHelper.ensureConnectedOnStart(logger, args, sourceDelegator);
                    ensureConnectedOnStartExecuted = true;
                } catch (Exception e) {
                    handleConnectException(logger, sourceDelegator, count, e);
                }
            }
        }
        return sourceDelegator;
    }

    public void incrementCycleCounter() {
        cycleCounter++;
        this.logPrefix = mainLogPrefix + "[cycle=" + cycleCounter + "]";
    }

    public List<ProviderFile> selectFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, String excludedFileExtension)
            throws YADEEngineSourcePollingException {

        List<ProviderFile> result = new ArrayList<>();

        int currentFilesCount = 0;
        long currentPollingTime = 0L;

        boolean shouldSelectFiles = false;
        String debugLogPrefix = "[polling][cycle=" + cycleCounter + "]";
        pl: while (true) {
            if (currentPollingTime == 0) {
                logger.info(YADEClientBannerWriter.SEPARATOR_LINE_DETAILS);
                logger.info("%sstart ...", logPrefix);
            }
            if (currentPollingTime >= pollTimeout) {
                if (logger.isDebugEnabled()) {
                    logger.debug("%s[end]currentPollingTime=%s >= pollTimeout=%s", debugLogPrefix, currentPollingTime, pollTimeout);
                }
                break pl;
            }

            if (!shouldSelectFiles && args.getPolling().getWaitForSourceFolder().isTrue()) {
                // sourceDir!=null is already checked on method begin
                ensureConnected(logger, sourceDelegator, currentPollingTime);
                try {
                    if (sourceDelegator.getProvider().exists(sourceDelegator.getDirectory())) {
                        shouldSelectFiles = true;
                    } else {
                        logger.info("[%s[%s]Source directory not found. Wait for the directory due to polling mode...", logPrefix, sourceDelegator
                                .getDirectory());
                        shouldSelectFiles = false;
                    }
                } catch (Exception e) {
                    throw new YADEEngineSourcePollingException(e);
                }
            } else {
                shouldSelectFiles = true;
            }

            if (shouldSelectFiles) {
                try {
                    ensureConnected(logger, sourceDelegator, currentPollingTime);

                    result = YADESourceFilesSelector.selectFiles(logger, sourceDelegator, excludedFileExtension, true);
                    currentFilesCount = result.size();
                } catch (Exception e) {
                    logger.error("%s[selectFiles]%s", logPrefix, e.toString());
                }

                if (args.getPolling().isPollMinFilesEnabled()) {
                    int pollMinFiles = args.getPolling().getPollMinFiles().getValue();
                    if (logger.isDebugEnabled()) {
                        logger.debug("%s[pollMinFiles=%s]currentFilesCount=%s", debugLogPrefix, pollMinFiles, currentFilesCount);
                    }

                    // if ((pollMinFiles == 0 && filesCount > 0) || (pollMinFiles > 0 && filesCount >= pollMinFiles)) {
                    if (currentFilesCount >= Math.max(1, pollMinFiles)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("%s[pollMinFiles=%s][currentFilesCount=%s]break", debugLogPrefix, pollMinFiles, currentFilesCount);
                        }
                        break pl;
                    }
                } else {
                    if (logger.isDebugEnabled()) {
                        logger.debug("%scurrentFilesCount=%s", debugLogPrefix, currentFilesCount);
                    }
                    if (currentFilesCount > 0) {
                        break pl;
                    }
                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("%s[wait][pollInterval]%s seconds...", debugLogPrefix, pollInterval);
            }
            YADEClientHelper.waitFor(pollInterval);
            currentPollingTime += pollInterval;
        }

        totalFilesCount += result.size();
        return result;
    }

    public boolean startNextPollingCycle(ISOSLogger logger) {
        if (!isPollingServer() || isPollingServerDurationElapsed(logger)) {
            return false;
        }
        return true;
    }

    private void ensureConnected(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator) throws YADEEngineSourcePollingException {
        ensureConnected(logger, sourceDelegator, -1L);
    }

    private void ensureConnected(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, long currentPollingTime)
            throws YADEEngineSourcePollingException {
        try {
            int count = 0;
            while (true) {
                count++;
                try {
                    sourceDelegator.getProvider().ensureConnected();
                    return;
                } catch (Exception e) {
                    handleConnectException(logger, sourceDelegator, currentPollingTime, count, e);
                }
            }
        } catch (Exception e) {
            throw new YADEEngineSourcePollingException(YADEProviderDelegatorHelper.getConnectionException(sourceDelegator, e));
        }
    }

    private void handleConnectException(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, int count, Exception e)
            throws YADEEngineSourcePollingException {
        handleConnectException(logger, sourceDelegator, -1L, count, e);
    }

    private void handleConnectException(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, long currentPollingTime, int count,
            Exception e) throws YADEEngineSourcePollingException {

        switch (method) {
        case Forever:
            if (count >= POLLING_MAX_RETRIES_ON_CONNECTION_ERROR) {
                Exception ex = sourceDelegator == null ? e : YADEProviderDelegatorHelper.getConnectionException(sourceDelegator, e);
                throw new YADEEngineSourcePollingException(String.format("Maximum reconnect retries(%s) reached",
                        POLLING_MAX_RETRIES_ON_CONNECTION_ERROR), ex);
            }
            break;
        case ServerDuration:
            if (isPollingServerDurationElapsed()) {
                Exception ex = sourceDelegator == null ? e : YADEProviderDelegatorHelper.getConnectionException(sourceDelegator, e);
                throw new YADEEngineSourcePollingException(ex);
            }
            break;
        case Timeout:
            long currentTime = System.currentTimeMillis() / 1_000;
            long pollingTime = currentPollingTime >= 0 ? currentPollingTime : start.getEpochSecond();
            long duration = currentTime - pollingTime;
            if (duration >= pollTimeout) {
                Exception ex = sourceDelegator == null ? e : YADEProviderDelegatorHelper.getConnectionException(sourceDelegator, e);
                throw new YADEEngineSourcePollingException(ex);
            }
            break;
        }

        String error = String.format("%s[exception occured][count=%s][reconnect]wait %ss and try again]%s", logPrefix, count,
                WAIT_SECONDS_ON_CONNECTION_ERROR, e.toString());
        if (count % 100 == 1) {
            // JobSchedulerException.LastErrorMessage = error;
            // doProcessMail(enuMailClasses.MailOnError);
            // JobSchedulerException.LastErrorMessage = "";
        }
        logger.info(error);
        YADEClientHelper.waitFor(WAIT_SECONDS_ON_CONNECTION_ERROR);
    }

    private void initMethod() {
        pollInterval = SOSArgumentHelper.asSeconds(args.getPolling().getPollInterval(), YADESourcePollingArguments.DEFAULT_POLL_INTERVAL);
        pollTimeout = args.getPolling().getPollTimeoutAsSeconds().getValue();

        if (isPollingServer()) {
            if (args.getPolling().getPollingServerDuration().getValue() != null && !args.getPolling().getPollingServerPollForever().isTrue()) {
                method = PollingMethod.ServerDuration;
                serverDuration = SOSArgumentHelper.asSeconds(args.getPolling().getPollingServerDuration(),
                        YADESourcePollingArguments.DEFAULT_POLL_INTERVAL);
            } else {
                method = PollingMethod.Forever;
            }
        } else {
            method = PollingMethod.Timeout;
        }
    }

    public PollingMethod getMethod() {
        return method;
    }

    public boolean isPollingServer() {
        return args.getPolling().getPollingServer().isTrue();
    }

    public String getMainLogPrefix() {
        return mainLogPrefix;
    }

    public Instant getStart() {
        return start;
    }

    public long getTotalFilesCount() {
        return totalFilesCount;
    }

    private void setMainLogPrefix(YADESourceProviderDelegator sourceDelegator) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(sourceDelegator.getLabel()).append("]");
        sb.append("[Polling]");
        sb.append("[").append(method).append("]");
        if (args.getPolling().getWaitForSourceFolder().isTrue()) {
            sb.append("[").append(YADEArgumentsHelper.toString(args.getPolling().getWaitForSourceFolder())).append("]");
        }
        if (isServerDurationMethod()) {
            sb.append("[").append(YADEArgumentsHelper.toString(args.getPolling().getPollingServerDuration())).append("]");
        }
        sb.append("[").append(args.getPolling().getPollTimeoutName()).append("=").append(args.getPolling().getPollTimeoutValue()).append("]");
        sb.append("[").append(YADEArgumentsHelper.toString(args.getPolling().getPollInterval())).append("]");
        if (args.getPolling().isPollMinFilesEnabled()) {
            sb.append("[").append(YADEArgumentsHelper.toString(args.getPolling().getPollMinFiles())).append("]");
        }

        this.mainLogPrefix = sb.toString();
    }

    private boolean isServerDurationMethod() {
        return PollingMethod.ServerDuration.equals(method);
    }

    private boolean isPollingServerDurationElapsed() {
        return isPollingServerDurationElapsed(null);
    }

    private boolean isPollingServerDurationElapsed(ISOSLogger logger) {
        if (!isServerDurationMethod()) {
            return false;
        }
        long differenceInSeconds = Math.abs(Instant.now().getEpochSecond() - start.getEpochSecond());
        if (differenceInSeconds >= serverDuration) {
            if (logger != null && logger.isDebugEnabled()) {
                logger.debug("[polling][%s][terminate polling server][time elapsed]duration=%ss >= serverDuration=%ss", method, differenceInSeconds,
                        serverDuration);
            }
            return true;
        }
        return false;
    }

}
