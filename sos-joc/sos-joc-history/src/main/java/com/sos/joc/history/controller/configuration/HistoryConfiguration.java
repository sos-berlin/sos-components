package com.sos.joc.history.controller.configuration;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.history.helper.HistoryUtil;

public class HistoryConfiguration implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryConfiguration.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    // Directory, History LOGS
    private String logDir = "logs/history";
    // commit after n db operations
    private int maxTransactions = 100;

    // Flux - Collect incoming values into a List that will be pushed into the returned Flux every timespan OR maxSize items.
    private int bufferTimeoutMaxSize = 1000; // the max collected size
    private int bufferTimeoutMaxTime = 1; // the timeout in seconds to use to release a buffered list

    // minutes, JS7 Proxy ControllerApi
    private int releaseEventsInterval = 15;

    // seconds
    private int waitIntervalOnError = 5;

    // MB
    private int logApplicableMBSize = 500;
    private int logMaximumMBSize = 1_000;

    // Bytes
    private int logApplicableByteSize = mb2bytes(logApplicableMBSize);
    private int logMaximumByteSize = mb2bytes(logMaximumMBSize * 1_024 * 1_024);
    private int logTruncateFirstLastByteSize = 100 * 1_024;

    public void load(final Properties conf) throws Exception {
        if (conf.getProperty("history_log_dir") != null) {
            logDir = HistoryUtil.resolveVars(conf.getProperty("history_log_dir").trim());
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[history_log_dir=%s]%s", conf.getProperty("history_log_dir"), logDir));
            }
        }
        Path ld = Paths.get(logDir);
        if (!Files.exists(ld)) {
            try {
                Files.createDirectory(ld);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[history_log_dir=%s]created", logDir));
                }
            } catch (Throwable e) {
                throw new Exception(String.format("[%s][can't create directory]%s", ld.toAbsolutePath(), e.toString()), e);
            }
        }

        if (conf.getProperty("history_max_transactions") != null) {
            maxTransactions = Integer.parseInt(conf.getProperty("history_max_transactions").trim());
        }
        if (conf.getProperty("history_buffer_timeout_max_size") != null) {
            bufferTimeoutMaxSize = Integer.parseInt(conf.getProperty("history_buffer_timeout_max_size").trim());
        }
        if (conf.getProperty("history_buffer_timeout_max_time") != null) {
            bufferTimeoutMaxTime = Integer.parseInt(conf.getProperty("history_buffer_timeout_max_time").trim());
        }
        if (conf.getProperty("history_release_events_interval") != null) {
            releaseEventsInterval = Integer.parseInt(conf.getProperty("history_release_events_interval").trim());
        }
        LOGGER.info(String.format(
                "[history]max_transactions=%s, buffer_timeout_max_size=%s, buffer_timeout_max_time=%ss, release_events_interval=%sm", maxTransactions,
                bufferTimeoutMaxSize, bufferTimeoutMaxTime, releaseEventsInterval));

        if (conf.getProperty("history_wait_interval_on_error") != null) {
            waitIntervalOnError = Integer.parseInt(conf.getProperty("history_wait_interval_on_error").trim());
        }
    }

    private static int mb2bytes(int mb) {
        return mb * 1_024 * 1_024;
    }

    public String getLogDir() {
        return logDir;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public int getReleaseEventsInterval() {
        return releaseEventsInterval;
    }

    public int getBufferTimeoutMaxSize() {
        return bufferTimeoutMaxSize;
    }

    public int getBufferTimeoutMaxTime() {
        return bufferTimeoutMaxTime;
    }

    public int getWaitIntervalOnError() {
        return waitIntervalOnError;
    }

    public void setLogApplicableMBSize(int val) {
        logApplicableMBSize = val;
        logApplicableByteSize = mb2bytes(val);
    }

    public int getLogApplicableMBSize() {
        return logApplicableMBSize;
    }

    public int getLogApplicableByteSize() {
        return logApplicableByteSize;
    }

    public void setLogMaximumMBSize(int val) {
        logMaximumMBSize = val;
        logMaximumByteSize = mb2bytes(val);
    }

    public int getLogMaximumMBSize() {
        return logMaximumMBSize;
    }

    public int getLogMaximumByteSize() {
        return logMaximumByteSize;
    }

    public int getLogTruncateFirstLastByteSize() {
        return logTruncateFirstLastByteSize;
    }
}
