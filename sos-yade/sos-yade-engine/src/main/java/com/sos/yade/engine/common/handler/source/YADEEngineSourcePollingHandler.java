package com.sos.yade.engine.common.handler.source;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.commons.vfs.common.file.ProviderFile;
import com.sos.yade.engine.common.YADEDirectory;
import com.sos.yade.engine.common.YADEEngineHelper;
import com.sos.yade.engine.common.arguments.YADESourceArguments;
import com.sos.yade.engine.exception.SOSYADEEngineSourcePollingException;

public class YADEEngineSourcePollingHandler {

    private enum PollingMethod {
        Timeout, ServerDuration, Forever
    }

    public static final long DEFAULT_POLL_INTERVAL = 60L;// seconds

    private static final int POLL_FOREVER_MAX_RETRIES_ON_CONNECTION_ERROR = 1_000;
    private static final int WAIT_SECONDS_ON_CONNECTION_ERROR = 10;
    private static final int WAIT_SECONDS_ON_TRANSFER_ERROR = 30;

    private final boolean enabled;

    private ISOSLogger logger;
    private IProvider source;
    private YADESourceArguments args;
    private PollingMethod method;

    private Instant start;
    private long interval;
    private long timeout;
    private long serverDuration;
    private long totalFiles;

    public YADEEngineSourcePollingHandler(ISOSLogger logger, IProvider source, YADESourceArguments args) {
        this.enabled = args.poolTimeoutEnabled();
        if (this.enabled) {
            this.logger = logger;
            this.source = source;
            this.args = args;
            init();
        }
    }

    public List<ProviderFile> selectFiles(YADEDirectory sourceDir) throws SOSYADEEngineSourcePollingException {
        if (start == null) {
            if (args.getPolling().getPollingWait4SourceFolder().getValue() && sourceDir == null) {
                throw new SOSYADEEngineSourcePollingException(args.getPolling().getPollingWait4SourceFolder().getName()
                        + "=true, but source_dir is not set");
            }
            start = Instant.now();
        }

        List<ProviderFile> result = new ArrayList<>();

        boolean singleFilesSpecified = args.singleFilesSpecified();
        long currentPollingTime = 0;

        boolean shouldSelectFiles = false;
        String logPrefix = "[source][polling]";

        pl: while (true) {
            if (currentPollingTime == 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("%s[start]%s s...", logPrefix, timeout);
                }
            }
            if (currentPollingTime > timeout) {
                if (logger.isDebugEnabled()) {
                    logger.debug("%s[end]%s s", logPrefix, timeout);
                }
                break pl;
            }

            if (!shouldSelectFiles && args.getPolling().getPollingWait4SourceFolder().getValue()) {
                // sourceDir!=null is already checked on method begin
                if (source.exists(sourceDir.getPath())) {
                    shouldSelectFiles = true;
                } else {
                    logger.info("[%s[%s]Source directory not found. Wait for the directory due to polling mode...", logPrefix, sourceDir);
                    shouldSelectFiles = false;
                }
            } else {
                shouldSelectFiles = true;
            }

            if (shouldSelectFiles) {
                if (singleFilesSpecified) {

                } else {

                }
            }

            if (logger.isDebugEnabled()) {
                logger.debug("[wait]%s seconds...", interval);
            }
            YADEEngineHelper.waitFor(interval);
            currentPollingTime += interval;
            // if (filesCount >= currentFilesCount && filesCount != 0) {
            // if (args.getWaitingForLateComers().isTrue()) {
            // args.getWaitingForLateComers().setValue(Boolean.valueOf(true));
            // } else {
            // break pl;
            // }
            // }

            // ensureConnected();
        }

        return result;
    }

    public boolean startNextPollingCycle(int filesProcessed, boolean hasError) {
        totalFiles += filesProcessed; // set total in all cases

        if (!isPollingServer() || isPollingServerDurationElapsed()) {
            return false;
        }

        // YADEEngineHelper.printSummary(logger, args);
        // sendNotifications

        return true;
    }

    private void ensureConnected(long pollingServerStartTime, long currentPollingTime) throws SOSYADEEngineSourcePollingException {
        try {
            boolean run = true;
            int count = 0;
            while (run) {
                count++;
                try {
                    source.ensureConnected();
                    run = false;
                } catch (Throwable e) {
                    if (PollingMethod.Forever.equals(method)) {
                        if (count >= POLL_FOREVER_MAX_RETRIES_ON_CONNECTION_ERROR) {
                            throw new SOSYADEEngineSourcePollingException(String.format("Maximum reconnect retries(%s) reached",
                                    POLL_FOREVER_MAX_RETRIES_ON_CONNECTION_ERROR), e);
                        }
                    }
                }
            }
        } catch (Throwable e) {
            throw new SOSYADEEngineSourcePollingException(e);
        }
    }

    private void init() {
        interval = YADEEngineHelper.getIntervalInSeconds(args.getPolling().getPollInterval(), DEFAULT_POLL_INTERVAL);
        timeout = getPollTimeout();

        if (isPollingServer()) {
            if (args.getPolling().getPollingServerDuration().getValue() != null && !args.getPolling().getPollingServerPollForever().isTrue()) {
                method = PollingMethod.ServerDuration;
                serverDuration = YADEEngineHelper.getIntervalInSeconds(args.getPolling().getPollingServerDuration(), DEFAULT_POLL_INTERVAL);
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

    private boolean isServerDuration() {
        return PollingMethod.ServerDuration.equals(method);
    }

    public boolean isPollingServerDurationElapsed() {
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
