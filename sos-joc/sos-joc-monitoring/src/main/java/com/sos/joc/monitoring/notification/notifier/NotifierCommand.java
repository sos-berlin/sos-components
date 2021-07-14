package com.sos.joc.monitoring.notification.notifier;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.common.SOSCommandResult;
import com.sos.commons.util.common.SOSEnv;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.monitor.MonitorCommand;
import com.sos.monitoring.notification.NotificationType;

public class NotifierCommand extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierCommand.class);

    private static final String VAR_COMMAND = "COMMAND";

    private final MonitorCommand monitor;

    public NotifierCommand(MonitorCommand monitor) {
        this.monitor = monitor;
    }

    @Override
    public NotifyResult notify(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, NotificationType type) {

        set(mo, mos);
        String cmd = resolve(monitor.getCommand(), type, false);
        LOGGER.info(getInfo4execute(true, mo, mos, type, cmd));

        SOSCommandResult result = SOSShell.executeCommand(cmd, getEnvVariables(cmd, type));
        if (result.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append("[").append(monitor.getInfo()).append("]");
            info.append(result);

            String err = getInfo4executeException(mo, mos, type, info.toString(), null);
            LOGGER.error(err);
            return new NotifyResult(result.getCommand(), getSendInfo(), err);
        }

        LOGGER.info("    " + getInfo4execute(false, mo, mos, type, result.getCommand()));
        return new NotifyResult(result.getCommand(), getSendInfo());
    }

    @Override
    public void close() {
    }

    @Override
    public StringBuilder getSendInfo() {
        return null;
    }

    private SOSEnv getEnvVariables(String cmd, NotificationType type) {
        Map<String, String> map = new HashMap<>();
        map.put(PREFIX_ENV_VAR + "_" + VAR_TYPE, type.value());
        map.put(PREFIX_ENV_VAR + "_" + VAR_COMMAND, cmd);

        getJocHref().addEnvs(map);

        getTableFields().entrySet().forEach(e -> {
            // if (!e.getKey().endsWith("_PARAMETERS")) {
            String val = e.getValue();
            if (e.getKey().endsWith("ERROR_TEXT")) {// TITLE? ....
                val = escape(val);
            }
            map.put(PREFIX_ENV_TABLE_FIELD_VAR + "_" + e.getKey(), nl2sp(val));
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
