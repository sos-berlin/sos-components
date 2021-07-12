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

public class NotifierCommand extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierCommand.class);

    private final MonitorCommand monitor;

    private static final String VAR_COMMAND = "COMMAND";
    private static final boolean SET_HREF_ENVS = false;

    public NotifierCommand(MonitorCommand monitor) {
        this.monitor = monitor;
    }

    @Override
    public NotifyResult notify(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, Status status) {

        set(mo, mos);
        String cmd = resolve(monitor.getCommand(), status, false);
        LOGGER.info(getInfo4execute(true, mo, mos, status, cmd));

        SOSCommandResult result = SOSShell.executeCommand(cmd, getEnvVariables(cmd, status));
        if (result.hasError()) {
            StringBuilder info = new StringBuilder();
            info.append("[").append(monitor.getInfo()).append("]");
            info.append(result);

            String err = getInfo4executeException(mo, mos, status, info.toString(), null);
            LOGGER.error(err);
            return new NotifyResult(result.getCommand(), getSendInfo(), err);
        }

        LOGGER.info("    " + getInfo4execute(false, mo, mos, status, result.getCommand()));
        return new NotifyResult(result.getCommand(), getSendInfo());
    }

    @Override
    public void close() {
    }

    @Override
    public StringBuilder getSendInfo() {
        return null;
    }

    private SOSEnv getEnvVariables(String cmd, Status status) {
        Map<String, String> map = new HashMap<>();
        map.put(PREFIX_ENV_VAR + "_" + VAR_STATUS, status.name());
        map.put(PREFIX_ENV_VAR + "_" + VAR_COMMAND, cmd);
        if (SET_HREF_ENVS) {
            map.put(PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_WORKFLOW, jocHrefWorkflow());
            map.put(PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_ORDER, jocHrefWorkflowOrder());
            map.put(PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_JOB, jocHrefWorkflowJob());
        }
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
