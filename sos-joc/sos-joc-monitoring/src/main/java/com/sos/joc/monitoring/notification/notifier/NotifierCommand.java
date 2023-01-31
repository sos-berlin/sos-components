package com.sos.joc.monitoring.notification.notifier;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.commons.util.common.SOSTimeout;
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

    private static final SOSTimeout TIMEOUT = new SOSTimeout(5, TimeUnit.MINUTES);
    private static final String VAR_COMMAND = PREFIX_COMMON_VAR + "_COMMAND";

    private final MonitorCommand monitor;

    public NotifierCommand(int nr, MonitorCommand monitor) {
        super.setNr(nr);
        this.monitor = monitor;
    }

    @Override
    public AMonitor getMonitor() {
        return monitor;
    }

    @Override
    public NotifyResult notify(NotificationType type, TimeZone timeZone, SystemMonitoringEvent event, Date dateTime, String exception) {

        set(type, timeZone, event, dateTime, exception);
        String cmd = resolveSystemVars(monitor.getCommand(), false);
        LOGGER.info(getInfo4execute(true, event, type, cmd));

        SOSCommandResult commandResult = JOCSOSShell.executeCommand(cmd, TIMEOUT, getEnvVariables(cmd));
        NotifyResult result = new NotifyResult(commandResult.getCommand(), getSendInfo());
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append("[").append(monitor.getInfo()).append("]");
            info.append(commandResult);

            result.setError(getInfo4executeFailed(event, type, info.toString()));
            return result;
        }

        LOGGER.info(Configuration.LOG_INTENT_2 + getInfo4execute(false, event, type, commandResult.getCommand()));
        return result;
    }

    @Override
    public NotifyResult notify(NotificationType type, TimeZone timeZone, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos,
            DBItemNotification mn) {

        set(type, timeZone, mo, mos, mn);
        String cmd = resolve(monitor.getCommand(), false);
        LOGGER.info(getInfo4execute(true, mo, mos, type, cmd));

        SOSCommandResult commandResult = JOCSOSShell.executeCommand(cmd, TIMEOUT, getEnvVariables(cmd));
        NotifyResult result = new NotifyResult(commandResult.getCommand(), getSendInfo());
        if (commandResult.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append("[").append(monitor.getInfo()).append("]");
            info.append(commandResult);

            result.setError(getInfo4executeFailed(mo, mos, type, info.toString()));
            return result;
        }

        LOGGER.info(Configuration.LOG_INTENT_2 + getInfo4execute(false, mo, mos, type, commandResult.getCommand()));
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
        getJocHref().addEnvs(map);

        // COMMON VARS
        getCommonVars().entrySet().forEach(e -> {
            map.put(PREFIX_ENV_VAR + "_" + e.getKey(), e.getValue());
        });

        // TABLE VARS
        getTableFields().entrySet().forEach(e -> {
            // if (!e.getKey().endsWith("_PARAMETERS")) {
            String val = e.getValue();
            if (e.getKey().endsWith("ERROR_TEXT") || e.getKey().endsWith("WARN_TEXT")) {// TITLE? ....
                val = escape(val);
            }
            map.put(PREFIX_ENV_VAR + "_" + e.getKey(), nl2sp(val));
            // }
        });
        return new SOSEnv(map);
    }

    private String nl2sp(String value) {
        return value.replaceAll("\\r\\n|\\r|\\n", " ");
    }

    private String escape(String val) {
        return SOSShell.IS_WINDOWS ? escape4Windows(val) : escape4Unix(val);
    }

    private String escape4Windows(String s) {
        return s.replaceAll("<", "^<").replaceAll(">", "^>").replaceAll("%", "^%").replaceAll("&", "^&");
    }

    private String escape4Unix(String s) {
        return s.replaceAll("\"", "\\\\\"").replaceAll("<", "\\\\<").replaceAll(">", "\\\\>").replaceAll("%", "\\\\%").replaceAll("&", "\\\\&")
                .replaceAll(";", "\\\\;").replaceAll("'", "\\\\'");
    }

}
