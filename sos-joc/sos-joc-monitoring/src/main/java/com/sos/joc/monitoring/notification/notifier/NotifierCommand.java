package com.sos.joc.monitoring.notification.notifier;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.beans.SOSCommandResult;
import com.sos.commons.util.beans.SOSEnv;
import com.sos.commons.util.beans.SOSTimeout;
import com.sos.joc.classes.JOCSOSShell;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.monitoring.bean.SystemMonitoringEvent;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.configuration.monitor.MonitorCommand;
import com.sos.monitoring.notification.NotificationType;

public class NotifierCommand extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierCommand.class);

    private static final Path COMMAND_EXECUTER_WORKING_DIR;
    private static final SOSTimeout TIMEOUT = new SOSTimeout(5, TimeUnit.MINUTES);
    private static final String VAR_COMMAND = PREFIX_COMMON_VAR + "_COMMAND";

    private final MonitorCommand monitor;

    static {
        COMMAND_EXECUTER_WORKING_DIR = getWorkingDir();
    }

    public NotifierCommand(String identifier, MonitorCommand monitor) {
        super.setIdentifier(identifier);
        this.monitor = monitor;
    }

    @Override
    public AMonitor getMonitor() {
        return monitor;
    }

    @Override
    public NotifyResult notifyOrderNotification(NotificationType type, TimeZone timeZone, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos,
            DBItemNotification mn) {

        set(type, timeZone, mo, mos, mn);
        String cmd = resolve(monitor.getCommand(), false);
        Instant start = Instant.now();
        LOGGER.info(getOrderNotificationExecutionInfo(start, null, true, mo, mos, cmd));

        SOSCommandResult commandResult = JOCSOSShell.executeCommand(cmd, TIMEOUT, getEnvVariables(cmd), COMMAND_EXECUTER_WORKING_DIR);
        NotifyResult result = new NotifyResult(commandResult.getCommand(), getSendInfo());
        Instant end = Instant.now();
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append("[").append(monitor.getInfo()).append("]");
            info.append(commandResult);

            result.setError(getOrderNotificationExecutionFailedInfo(start, end, mo, mos, info.toString()), commandResult.getException());
            return result;
        }

        LOGGER.info(Configuration.LOG_INTENT_2 + getOrderNotificationExecutionInfo(start, end, false, mo, mos, commandResult.getCommand()));
        return result;
    }

    @Override
    public NotifyResult notifySystemNotification(NotificationType type, TimeZone timeZone, String jocId, SystemMonitoringEvent event, Date dateTime,
            String exception) {

        set(type, timeZone, jocId, event, dateTime, exception);
        String cmd = resolveSystemVars(monitor.getCommand(), false);
        Instant start = Instant.now();
        LOGGER.info(getSystemNotificationExecutionInfo(start, null, true, event, cmd));

        SOSCommandResult commandResult = JOCSOSShell.executeCommand(cmd, TIMEOUT, getEnvVariables(cmd), COMMAND_EXECUTER_WORKING_DIR);
        NotifyResult result = new NotifyResult(commandResult.getCommand(), getSendInfo());
        Instant end = Instant.now();
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append("[").append(monitor.getInfo()).append("]");
            info.append(commandResult);

            result.setError(getSystemNotificationExecutionFailedInfo(start, end, event, info.toString()));
            return result;
        }

        LOGGER.info(Configuration.LOG_INTENT_2 + getSystemNotificationExecutionInfo(start, end, false, event, commandResult.getCommand()));
        return result;
    }

    @Override
    public void close() {
    }

    @Override
    public StringBuilder getSendInfo() {
        return null;
    }

    private SOSEnv getEnvVariables(String cmd) {
        Map<String, String> map = new HashMap<>();
        map.put(PREFIX_ENV_VAR + "_" + VAR_COMMAND, cmd); // extra var

        // JOC VARS
        getJocVariables().addEnvs(map);

        // COMMON VARS
        getCommonVars().entrySet().forEach(e -> {
            map.put(PREFIX_ENV_VAR + "_" + e.getKey(), e.getValue());
        });

        // SYSTEM VARS
        if (getSystemVars() != null) {
            getSystemVars().entrySet().forEach(e -> {
                String val = e.getValue();
                if (e.getKey().endsWith("MESSAGE") || e.getKey().endsWith("EXCEPTION")) {
                    val = SOSEnv.escapeValue(val);
                }
                map.put(PREFIX_ENV_VAR + "_" + e.getKey(), SOSEnv.newLine2Space(val));
            });
        }

        // TABLE VARS
        if (getTableFields() != null) {
            getTableFields().entrySet().forEach(e -> {
                // if (!e.getKey().endsWith("_PARAMETERS")) {
                String val = e.getValue();
                if (e.getKey().endsWith("ERROR_TEXT") || e.getKey().endsWith("WARN_TEXT")) {// TITLE? ....
                    val = SOSEnv.escapeValue(val);
                }
                map.put(PREFIX_ENV_VAR + "_" + e.getKey(), SOSEnv.newLine2Space(val));
                // }
            });
        }
        return new SOSEnv(map);
    }

    private static Path getWorkingDir() {
        try {
            return Paths.get(System.getProperty("java.io.tmpdir"));
        } catch (Throwable e) {
            LOGGER.error("[getWorkingDir]" + e.toString(), e);
            return null;
        }
    }

}
