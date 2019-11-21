package com.sos.jobscheduler.history.master.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.jobscheduler.history.helper.HistoryUtil;

public class HistoryMasterConfiguration extends MasterConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMasterConfiguration.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    // milliseconds
    private int minExecutionTimeOnNonEmptyEvent = 10; // to avoid master 429 TooManyRequestsException
    // minutes,
    // send KeepEvents command
    private int keepEventsInterval = 15;

    private int maxTransactions = 100;
    private boolean saveOrderStatus = false;

    private String logDir;
    private boolean logStoreLog2Db;
    private boolean logUseLog4j2Writer = false;

    private long diagnosticStartIfNotEmptyEventLongerThan = 0; // seconds
    private long diagnosticStartIfHistoryExecutionLongerThan = 0; // seconds
    private String diagnosticAdditionalScript;
    private String uriHistoryExecutor;

    // TODO
    public void load(final Properties conf) throws Exception {
        super.load(conf);

        if (conf.getProperty("webservice_keep_events_interval") != null) {
            keepEventsInterval = Integer.parseInt(conf.getProperty("webservice_keep_events_interval").trim());
        }
        if (conf.getProperty("min_execution_time_on_non_empty_event") != null) {
            minExecutionTimeOnNonEmptyEvent = Integer.parseInt(conf.getProperty("min_execution_time_on_non_empty_event").trim());
        }

        if (conf.getProperty("max_transactions") != null) {
            maxTransactions = Integer.parseInt(conf.getProperty("max_transactions").trim());
        }
        if (conf.getProperty("save_order_status") != null) {
            saveOrderStatus = Boolean.parseBoolean(conf.getProperty("save_order_status").trim());
        }

        if (conf.getProperty("log_dir") != null) {
            logDir = HistoryUtil.resolveVars(conf.getProperty("log_dir").trim());
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[log_dir=%s]%s", conf.getProperty("log_dir"), logDir));
            }
            Path ld = Paths.get(logDir);
            if (!Files.exists(ld)) {
                try {
                    Files.createDirectory(ld);
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[log_dir=%s]created", logDir));
                    }
                } catch (Throwable e) {
                    throw new Exception(String.format("[%s][can't create directory]%s", logDir, e.toString()), e);
                }
            }
        }
        if (conf.getProperty("log_store_log2db") != null) {
            logStoreLog2Db = Boolean.parseBoolean(conf.getProperty("log_store_log2db").trim());
        }
        if (conf.getProperty("log_use_log4j2_writer") != null) {
            logUseLog4j2Writer = Boolean.parseBoolean(conf.getProperty("log_use_log4j2_writer").trim());
        }

        if (conf.getProperty("diagnostic_additional_script") != null) {
            diagnosticAdditionalScript = conf.getProperty("diagnostic_additional_script").trim();
        }

        if (conf.getProperty("diagnostic_start_if_not_empty_event_longer_than") != null) {
            diagnosticStartIfNotEmptyEventLongerThan = Long.parseLong(conf.getProperty("diagnostic_start_if_not_empty_event_longer_than").trim());
        }
        if (conf.getProperty("diagnostic_start_if_history_execution_longer_than") != null) {
            diagnosticStartIfHistoryExecutionLongerThan = Long.parseLong(conf.getProperty("diagnostic_start_if_history_execution_longer_than")
                    .trim());
        }

        if (conf.getProperty("uri_history_executor") != null) {
            uriHistoryExecutor = conf.getProperty("uri_history_executor").trim();
        }
    }

    public int getMinExecutionTimeOnNonEmptyEvent() {
        return minExecutionTimeOnNonEmptyEvent;
    }

    public int getKeepEventsInterval() {
        return keepEventsInterval;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public boolean getSaveOrderStatus() {
        return saveOrderStatus;
    }

    public String getLogDir() {
        return logDir;
    }

    public boolean getLogStoreLog2Db() {
        return logStoreLog2Db;
    }

    public boolean getLogUseLog4j2Writer() {
        return logUseLog4j2Writer;
    }

    public long getDiagnosticStartIfNotEmptyEventLongerThan() {
        return diagnosticStartIfNotEmptyEventLongerThan;
    }

    public long getDiagnosticStartIfHistoryExecutionLongerThan() {
        return diagnosticStartIfHistoryExecutionLongerThan;
    }

    public String getDiagnosticAdditionalScript() {
        return diagnosticAdditionalScript;
    }

    public String getUriHistoryExecutor() {
        return uriHistoryExecutor;
    }
}
