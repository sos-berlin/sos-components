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
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.exception.SOSNotifierSendException;

public class NotifierCommand extends ANotifier {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierCommand.class);

    private final MonitorCommand monitor;

    private static final String VAR_SERVICE_COMMAND = "SERVICE_COMMAND";
    private static final boolean SET_HREF_ENVS = false;

    public NotifierCommand(MonitorCommand monitor) throws Exception {
        this.monitor = monitor;
    }

    @Override
    public void notify(DBLayerMonitoring dbLayer, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, ServiceStatus status,
            ServiceMessagePrefix prefix) throws SOSNotifierSendException {

        evaluate(mo, mos, status, prefix);
        String cmd = resolve(monitor.getCommand(), false);
        LOGGER.info(String.format("[%s-%s][command][execute]%s", getServiceStatus(), getServiceMessagePrefix(), cmd));

        SOSCommandResult result = SOSShell.executeCommand(cmd, getEnvVariables(cmd));
        if (result.hasError()) {
            throw new SOSNotifierSendException(String.format("[%s name=\"%s\"]%s", monitor.getRefElementName(), monitor.getMonitorName(), result
                    .toString()), result.getException());
        }
        LOGGER.info(String.format("[%s-%s][command][executed exitCode=%s]%s", getServiceStatus(), getServiceMessagePrefix(), result.getExitCode(),
                result.getCommand()));
    }

    @Override
    public void close() {
    }

    private SOSEnv getEnvVariables(String cmd) {
        Map<String, String> map = new HashMap<>();
        map.put(PREFIX_ENV_VAR + "_" + VAR_SERVICE_STATUS, getServiceStatus());
        map.put(PREFIX_ENV_VAR + "_" + VAR_SERVICE_MESSAGE_PREFIX, getServiceMessagePrefix());
        map.put(PREFIX_ENV_VAR + "_" + VAR_SERVICE_COMMAND, cmd);
        if (SET_HREF_ENVS) {
            map.put(PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_ORDER, jocHrefWorkflowOrder());
            map.put(PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_JOB, jocHrefWorkflowJob());
        }
        getTableFields().entrySet().forEach(e -> {
            map.put(PREFIX_ENV_TABLE_FIELD_VAR + "_" + e.getKey(), nl2sp(e.getValue()));
        });
        return new SOSEnv(map);
    }

}
