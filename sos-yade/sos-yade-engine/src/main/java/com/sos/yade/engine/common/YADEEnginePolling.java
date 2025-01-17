package com.sos.yade.engine.common;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.common.logger.ISOSLogger;
import com.sos.commons.vfs.common.IProvider;
import com.sos.yade.engine.exception.SOSYADEEnginePollingException;

public class YADEEnginePolling {

    private enum PollingMethod {
        Timeout, ServerDuration, Forever
    }

    public static final long DEFAULT_POLL_INTERVAL = 60L;// seconds

    private final boolean enabled;

    private ISOSLogger logger;
    private TransferArguments args;
    private IProvider source;
    private PollingMethod method;

    public YADEEnginePolling(ISOSLogger logger, TransferArguments args, IProvider source) {
        this.enabled = !args.getSkipTransfer().getValue() && (args.getPollTimeout().getValue() != null);
        if (this.enabled) {
            this.logger = logger;
            this.args = args;
            this.source = source;
            initMethod();
        }
    }

    private void initMethod() {
        if (!args.getPollingServer().isEmpty()) {
            if (args.getPollingServerDuration().getValue() != null && !args.getPollingServerPollForever().isTrue()) {
                method = PollingMethod.ServerDuration;
            } else {
                method = PollingMethod.Forever;
            }
        } else {
            method = PollingMethod.Timeout;
        }
    }

    public String[] doPolling() throws SOSYADEEnginePollingException {
        String[] fileList = null;
        long interval = getPollInterval();
        long timeout = getPollTimeout();

        String sourceDir = "";

        boolean singleFilesSpecified = args.singleFilesSpecified();
        // not check the source directory when file_path or file_list specified
        boolean sourceDirFound = singleFilesSpecified;
        long currentPollingTime = 0;
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

            if (!sourceDirFound) {
                sourceDirFound = source.exists(sourceDir);
                if (!sourceDirFound) {
                    if (args.getPollingWait4SourceFolder().getValue()) {
                        logger.info("[%s[%s]Source directory not found. Wait for the directory due to polling mode...", logPrefix, sourceDir);
                    } else {
                        throw new SOSYADEEnginePollingException(logPrefix + "[WaitForSourceFolder=false][" + sourceDir
                                + "]Source directory not found. Polling terminated.");
                    }
                }
            }

            if (sourceDirFound) {
                if (singleFilesSpecified) {

                } else {

                }

            }

        }

        return fileList;
    }

    private long getPollInterval() {
        try {
            return SOSDate.resolveAge("s", args.getPollInterval().getValue()).longValue();
        } catch (Throwable e) {
            return DEFAULT_POLL_INTERVAL;
        }
    }

    private long getPollTimeout() {
        if (args.getPollTimeout().getValue() == null) {
            return 0;
        } else {
            return args.getPollTimeout().getValue() * 60;
        }
    }

    public boolean enabled() {
        return enabled;
    }
}
