package com.sos.jobscheduler.history.master.configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.history.helper.HistoryUtil;

public class HistoryConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryConfiguration.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    // milliseconds
    private int minExecutionTimeOnNonEmptyEvent = 10; // to avoid master 429 TooManyRequestsException
    // minutes,
    // send ReleaseEvents command
    private int releaseEventsInterval = 15;

    private int maxTransactions = 100;

    private String logDir;
    private boolean logUseLog4j2Writer = false;

    private long diagnosticStartIfNotEmptyEventLongerThan = 0; // seconds
    private long diagnosticStartIfHistoryExecutionLongerThan = 0; // seconds
    private String diagnosticAdditionalScript;
    private String uriHistoryExecutor;

    // TODO
    public void load(final Properties conf) throws Exception {
        if (conf.getProperty("webservice_release_events_interval") != null) {
            releaseEventsInterval = Integer.parseInt(conf.getProperty("webservice_release_events_interval").trim());
        }
        if (conf.getProperty("min_execution_time_on_non_empty_event") != null) {
            minExecutionTimeOnNonEmptyEvent = Integer.parseInt(conf.getProperty("min_execution_time_on_non_empty_event").trim());
        }

        if (conf.getProperty("max_transactions") != null) {
            maxTransactions = Integer.parseInt(conf.getProperty("max_transactions").trim());
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

    public int getReleaseEventsInterval() {
        return releaseEventsInterval;
    }

    public int getMaxTransactions() {
        return maxTransactions;
    }

    public String getLogDir() {
        return logDir;
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
