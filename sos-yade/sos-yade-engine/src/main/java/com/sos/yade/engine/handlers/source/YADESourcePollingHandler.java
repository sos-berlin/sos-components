package com.sos.yade.engine.handlers.source;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.file.ProviderDirectoryPath;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.arguments.YADESourceArguments;
import com.sos.yade.engine.arguments.YADESourcePollingArguments;
import com.sos.yade.engine.delegators.YADESourceProviderDelegator;
import com.sos.yade.engine.exceptions.SOSYADEEngineSourcePollingException;
import com.sos.yade.engine.helpers.YADEArgumentsHelper;
import com.sos.yade.engine.helpers.YADEHelper;

public class YADESourcePollingHandler {

    private enum PollingMethod {
        Timeout, ServerDuration, Forever
    }

    private static final int POLLING_MAX_RETRIES_ON_CONNECTION_ERROR = 1_000;
    private static final int WAIT_SECONDS_ON_CONNECTION_ERROR = 10;

    private final boolean enabled;

    // private ISOSLogger logger;
    // private IProvider source;
    private YADESourceArguments args;
    private PollingMethod method;

    private Instant start;
    private long interval;
    private long timeout;
    private long serverDuration;
    private long totalFilesCount;
    private boolean firstCycle;

    public YADESourcePollingHandler(YADESourceProviderDelegator sourceDelegator) {
        YADESourceArguments args = sourceDelegator.getArgs();
        this.enabled = args.isPoolTimeoutEnabled();
        if (this.enabled) {
            // this.logger = logger;
            // this.source = source;
            this.args = args;
            init();
        }
    }

    public List<ProviderFile> selectFiles(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator) throws SOSYADEEngineSourcePollingException {

        ProviderDirectoryPath sourceDir = sourceDelegator.getDirectory();
        if (start == null) {
            if (args.getPolling().getPollingWait4SourceFolder().getValue() && sourceDir == null) {
                throw new SOSYADEEngineSourcePollingException(args.getPolling().getPollingWait4SourceFolder().getName()
                        + "=true, but source_dir is not set");
            }
            start = Instant.now();
            firstCycle = true;
        } else {
            firstCycle = false;
        }

        List<ProviderFile> result = new ArrayList<>();

        int currentFilesCount = 0;
        int filesCount = 0;// TODO unclear ...
        long currentPollingTime = 0;

        boolean shouldSelectFiles = false;
        String lp = sourceDelegator.getLogPrefix() + "[polling]";

        pl: while (true) {
            if (currentPollingTime == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("%s[start]%s s...", lp, timeout);
                }
            }
            if (currentPollingTime > timeout) {
                if (logger.isDebugEnabled()) {
                    logger.debug("%s[end]%s s", lp, timeout);
                }
                break pl;
            }

            if (!shouldSelectFiles && args.getPolling().getPollingWait4SourceFolder().getValue()) {
                // sourceDir!=null is already checked on method begin
                if (sourceDelegator.getProvider().exists(sourceDir.getPath())) {
                    shouldSelectFiles = true;
                } else {
                    logger.info("[%s[%s]Source directory not found. Wait for the directory due to polling mode...", lp, sourceDir);
                    shouldSelectFiles = false;
                }
            } else {
                shouldSelectFiles = true;
            }

            if (shouldSelectFiles) {
                try {
                    result = YADESourceFilesSelector.selectFiles(logger, sourceDelegator, true);
                    currentFilesCount = result.size();
                } catch (Throwable e) {
                    logger.error("%s[selectFiles]%s", lp, e.toString());
                }

                if (args.getPolling().isPollMinFilesEnabled()) {
                    int pollMinFiles = args.getPolling().getPollMinFiles().getValue();
                    if (logger.isDebugEnabled()) {
                        logger.debug("%s[pollMinFiles=%s][currentFilesCount=%s]", lp, pollMinFiles, currentFilesCount);
                    }

                    // if ((pollMinFiles == 0 && filesCount > 0) || (pollMinFiles > 0 && filesCount >= pollMinFiles)) {
                    if (currentFilesCount >= Math.max(1, pollMinFiles)) {
                        logger.debug("%s[pollMinFiles]break", lp);
                        break pl;
                    }
                }

            }

            if (logger.isDebugEnabled()) {
                logger.debug("[wait]%s seconds...", interval);
            }
            YADEHelper.waitFor(interval);
            currentPollingTime += interval;

            // TODO ???? YADE 1...
            if (filesCount >= currentFilesCount && filesCount != 0) {
                if (args.getPolling().getWaitingForLateComers().isTrue()) {
                    args.getPolling().getWaitingForLateComers().setValue(Boolean.valueOf(false));
                } else {
                    break pl;
                }
            }

            ensureConnected(logger, sourceDelegator, lp, currentPollingTime);
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

    // TODO - from YADE 1 - optimize...
    private void ensureConnected(ISOSLogger logger, YADESourceProviderDelegator sourceDelegator, String lp, long currentPollingTime)
            throws SOSYADEEngineSourcePollingException {
        try {
            int count = 0;
            while (true) {
                count++;
                try {
                    sourceDelegator.getProvider().ensureConnected();
                    return;
                } catch (Throwable e) {
                    if (PollingMethod.Forever.equals(method)) {
                        if (count >= POLLING_MAX_RETRIES_ON_CONNECTION_ERROR) {
                            throw new SOSYADEEngineSourcePollingException(String.format("Maximum reconnect retries(%s) reached",
                                    POLLING_MAX_RETRIES_ON_CONNECTION_ERROR), YADEHelper.getConnectionException(sourceDelegator, e));
                        }
                    } else {
                        long currentTime = System.currentTimeMillis() / 1_000;
                        long pollingTime = PollingMethod.ServerDuration.equals(method) ? start.getEpochSecond() : currentPollingTime;
                        long duration = currentTime - pollingTime;
                        if (duration >= getPollTimeout()) {
                            throw new SOSYADEEngineSourcePollingException(YADEHelper.getConnectionException(sourceDelegator, e));
                        }
                    }

                    String error = String.format("%s[reconnect][exception occured, wait %ss and try again (%s of %s)]%s", lp,
                            WAIT_SECONDS_ON_CONNECTION_ERROR, count, POLLING_MAX_RETRIES_ON_CONNECTION_ERROR, e.toString());
                    if (count % 100 == 1) {
                        // JobSchedulerException.LastErrorMessage = error;
                        // doProcessMail(enuMailClasses.MailOnError);
                        // JobSchedulerException.LastErrorMessage = "";
                    }
                    logger.warn(error);
                    YADEHelper.waitFor(WAIT_SECONDS_ON_CONNECTION_ERROR);
                }
            }
        } catch (Throwable e) {
            throw new SOSYADEEngineSourcePollingException(YADEHelper.getConnectionException(sourceDelegator, e));
        }
    }

    private void init() {
        interval = YADEArgumentsHelper.getIntervalInSeconds(args.getPolling().getPollInterval(), YADESourcePollingArguments.DEFAULT_POLL_INTERVAL);
        timeout = getPollTimeout();

        if (isPollingServer()) {
            if (args.getPolling().getPollingServerDuration().getValue() != null && !args.getPolling().getPollingServerPollForever().isTrue()) {
                method = PollingMethod.ServerDuration;
                serverDuration = YADEArgumentsHelper.getIntervalInSeconds(args.getPolling().getPollingServerDuration(),
                        YADESourcePollingArguments.DEFAULT_POLL_INTERVAL);
            } else {
                method = PollingMethod.Forever;
            }
        } else {
            method = PollingMethod.Timeout;
        }
    }

    private long getPollTimeout() {
        if (args.getPolling().getPollTimeout().getValue() == null) {
            return 0L;
        } else {
            return args.getPolling().getPollTimeout().getValue() * 60;
        }
    }

    public boolean enabled() {
        return enabled;
    }

    public PollingMethod getMethod() {
        return method;
    }

    public boolean isPollingServer() {
        return enabled ? args.getPolling().getPollingServer().isTrue() : false;
    }

    public boolean isFirstCycle() {
        return firstCycle;
    }

    private boolean isServerDuration() {
        return PollingMethod.ServerDuration.equals(method);
    }

    private boolean isPollingServerDurationElapsed(ISOSLogger logger) {
        if (!enabled || !isServerDuration()) {
            return false;
        }
        long differenceInSeconds = Math.abs(Instant.now().getEpochSecond() - start.getEpochSecond());
        if (differenceInSeconds >= serverDuration) {
            if (logger.isDebugEnabled()) {
                logger.debug("[%s][PollingServerDuration=%s][duration=%ss]time elapsed. terminate polling server", method, args.getPolling()
                        .getPollingServerDuration().getValue(), differenceInSeconds);
            }
            return true;
        }

        return false;
    }

}
