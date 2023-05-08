package com.sos.joc.cluster.configuration;

import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc.LogMove;

public class JocHistoryConfiguration implements Serializable {

    public static final Long ID_NOT_STARTED_ORDER = new Long(0);

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JocHistoryConfiguration.class);

    // Directory, History LOGS - see CleanupTaskHistory
    private Path logDir = SOSPath.toAbsolutePath("logs/history");
    // Directory, History LOGS of not started orders - see CleanupTaskHistory
    private Path logDirTmpOrders = logDir.resolve(ID_NOT_STARTED_ORDER.toString());
    // commit after n db operations
    private int maxTransactions = 100;

    // Flux - Collect incoming values into a List that will be pushed into the returned Flux every timespan OR maxSize items.
    private int bufferTimeoutMaxSize = 1000; // the max collected size
    private int bufferTimeoutMaxTime = 1; // the timeout in seconds to use to release a buffered list

    // seconds, JS7 Proxy ControllerApi
    private long releaseEventsInterval = 15 * 60;

    // seconds, clear HistoryModel cache - workflows etc
    private long cacheAge = 60 * 60;

    // seconds
    private long waitIntervalOnError = 5;
    // seconds
    private long waitIntervalOnProcessingError = 30;
    // seconds
    private long waitIntervalStopProcessingOnErrors = 2 * 60;

    private int maxStopProcessingOnErrors = 5;

    private Path logMoveDir;
    private LogMove logMoveOrder;
    private LogMove logMoveTask;

    // MB
    private int logApplicableMBSize = 500;
    private int logMaximumMBSize = 1_000;
    private int logMaximumDisplayMBSize = 10;

    // Bytes
    private int logApplicableByteSize = JocClusterUtil.mb2bytes(logApplicableMBSize);
    private int logMaximumByteSize = JocClusterUtil.mb2bytes(logMaximumMBSize);
    private int logMaximumDisplayByteSize = JocClusterUtil.mb2bytes(logMaximumDisplayMBSize);

    public void load(final Properties conf) throws Exception {
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (conf.getProperty("history_log_dir") != null) {
            logDir = SOSPath.toAbsolutePath(JocClusterUtil.resolveVars(conf.getProperty("history_log_dir").trim()));
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[history_log_dir=%s]%s", conf.getProperty("history_log_dir"), logDir));
            }
        }
        if (!Files.exists(logDir)) {
            try {
                Files.createDirectory(logDir);
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[history_log_dir=%s]created", logDir));
                }
            } catch (Throwable e) {
                throw new Exception(String.format("[%s][can't create directory]%s", logDir, e.toString()), e);
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
            releaseEventsInterval = SOSDate.resolveAge("s", conf.getProperty("history_release_events_interval").trim());
        }
        if (conf.getProperty("history_cache_age") != null) {
            cacheAge = SOSDate.resolveAge("s", conf.getProperty("history_cache_age").trim());
        }
        if (conf.getProperty("history_wait_interval_on_error") != null) {
            waitIntervalOnError = SOSDate.resolveAge("s", conf.getProperty("history_wait_interval_on_error").trim());
        }
        if (conf.getProperty("history_wait_interval_on_processing_error") != null) {
            waitIntervalOnProcessingError = SOSDate.resolveAge("s", conf.getProperty("history_wait_interval_on_processing_error").trim());
        }
        if (conf.getProperty("history_wait_interval_stop_processing_on_errors") != null) {
            waitIntervalStopProcessingOnErrors = SOSDate.resolveAge("s", conf.getProperty("history_wait_interval_stop_processing_on_errors").trim());
        }
        if (conf.getProperty("history_max_stop_processing_on_errors") != null) {
            maxStopProcessingOnErrors = Integer.parseInt(conf.getProperty("history_max_stop_processing_on_errors").trim());
        }

        LOGGER.info(String.format(
                "[history]max_transactions=%s, buffer_timeout_max_size=%s, buffer_timeout_max_time=%ss, release_events_interval=%ss, cache_age=%ss, wait_interval_stop_processing_on_errors=%ss, max_stop_processing_on_errors=%s",
                maxTransactions, bufferTimeoutMaxSize, bufferTimeoutMaxTime, releaseEventsInterval, cacheAge, waitIntervalStopProcessingOnErrors,
                maxStopProcessingOnErrors));
    }

    public Path getLogDir() {
        return logDir;
    }

    public Path getLogDirTmpOrders() {
        return logDirTmpOrders;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public int getBufferTimeoutMaxSize() {
        return bufferTimeoutMaxSize;
    }

    public int getBufferTimeoutMaxTime() {
        return bufferTimeoutMaxTime;
    }

    public long getReleaseEventsInterval() {
        return releaseEventsInterval;
    }

    public long getCacheAge() {
        return cacheAge;
    }

    public long getWaitIntervalOnError() {
        return waitIntervalOnError;
    }

    public long getWaitIntervalOnProcessingError() {
        return waitIntervalOnProcessingError;
    }

    public long getWaitIntervalStopProcessingOnErrors() {
        return waitIntervalStopProcessingOnErrors;
    }

    public int getMaxStopProcessingOnErrors() {
        return maxStopProcessingOnErrors;
    }

    public boolean isLogMoveDirEquals(Path val) {
        if (val == null && logMoveDir == null) {
            return true;
        } else if (val != null && logDir != null) {
            return val.equals(logMoveDir);
        }
        return false;
    }

    public boolean isLogMoveOrderEquals(LogMove val) {
        if (val == null && logMoveOrder == null) {
            return true;
        } else if (val != null && logMoveOrder != null) {
            return val.equals(logMoveOrder);
        }
        return false;
    }

    public boolean isLogMoveTaskEquals(LogMove val) {
        if (val == null && logMoveTask == null) {
            return true;
        } else if (val != null && logMoveTask != null) {
            return val.equals(logMoveTask);
        }
        return false;
    }

    public StringBuilder setLogMove(ConfigurationGlobalsJoc joc) {
        logMoveDir = null;
        logMoveOrder = null;
        logMoveTask = null;

        StringBuilder sb = new StringBuilder();
        if (!SOSString.isEmpty(joc.getLogMoveDirectory().getValue())) {
            logMoveDir = Paths.get(joc.getLogMoveDirectory().getValue()).toAbsolutePath();
            if (!Files.exists(logMoveDir)) {
                sb.append("[").append(joc.getLogMoveDirectory().getName()).append("=").append(logMoveDir).append(" not found]");

                logMoveDir = null;
                return sb;
            }
            if (!SOSString.isEmpty(joc.getLogMoveOrder().getValue())) {
                try {
                    logMoveOrder = LogMove.valueOf(joc.getLogMoveOrder().getValue());
                } catch (Throwable e) {
                    sb.append("[").append(joc.getLogMoveOrder().getName()).append("=").append(joc.getLogMoveOrder().getValue()).append(
                            " invalid value]");
                }
            }
            if (!SOSString.isEmpty(joc.getLogMoveTask().getValue())) {
                try {
                    logMoveTask = LogMove.valueOf(joc.getLogMoveTask().getValue());
                } catch (Throwable e) {
                    sb.append("[").append(joc.getLogMoveTask().getName()).append("=").append(joc.getLogMoveTask().getValue()).append(
                            " invalid value]");
                }
            }
        }
        return sb.length() == 0 ? null : sb;
    }

    public Path getLogMoveDir() {
        return logMoveDir;
    }

    public LogMove getLogMoveOrder() {
        return logMoveOrder;
    }

    public LogMove getLogMoveTask() {
        return logMoveTask;
    }

    public void setLogApplicableMBSize(int val) {
        logApplicableMBSize = val;
        logApplicableByteSize = JocClusterUtil.mb2bytes(val);
    }

    public int getLogApplicableMBSize() {
        return logApplicableMBSize;
    }

    public int getLogApplicableByteSize() {
        return logApplicableByteSize;
    }

    public void setLogMaximumMBSize(int val) {
        logMaximumMBSize = val;
        logMaximumByteSize = JocClusterUtil.mb2bytes(val);
    }

    public int getLogMaximumMBSize() {
        return logMaximumMBSize;
    }

    public int getLogMaximumByteSize() {
        return logMaximumByteSize;
    }

    public void setLogMaximumDisplayMBSize(int val) {
        logMaximumDisplayMBSize = val;
        logMaximumDisplayByteSize = JocClusterUtil.mb2bytes(val);
    }

    public int getLogMaximumDisplayMBSize() {
        return logMaximumDisplayMBSize;
    }

    public int getLogMaximumDisplayByteSize() {
        return logMaximumDisplayByteSize;
    }
}
