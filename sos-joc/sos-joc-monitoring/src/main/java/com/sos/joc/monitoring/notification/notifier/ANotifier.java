package com.sos.joc.monitoring.notification.notifier;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.exception.SOSNotifierSendException;

public abstract class ANotifier {

    protected enum ServiceStatus {
        OK, CRITICAL;
    }

    protected enum ServiceMessagePrefix {
        SUCCESS, ERROR, RECOVERED
    }

    protected static final String PREFIX_ENV_VAR = "JS7_MON";
    protected static final String PREFIX_ENV_TABLE_FIELD_VAR = PREFIX_ENV_VAR + "_TABLE";

    protected static final String VAR_SERVICE_STATUS = "SERVICE_STATUS";
    protected static final String VAR_SERVICE_MESSAGE_PREFIX = "SERVICE_MESSAGE_PREFIX";

    private static final String PREFIX_TABLE_ORDERS = "MON_O";
    private static final String PREFIX_TABLE_ORDER_STEPS = "MON_OS";

    private static final String JOC_URI_PART_LOG = "/joc/#/log2?";
    protected static final String VAR_JOC_HREF_ORDER = "JOC_HREF_ORDER";
    protected static final String VAR_JOC_HREF_JOB = "JOC_HREF_JOB";

    private Map<String, String> tableFields;
    private String serviceStatus;
    private String serviceMessagePrefix;
    private String jocHrefWorkflowOrder;
    private String jocHrefWorkflowJob;

    public abstract void notify(DBLayerMonitoring dbLayer, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, ServiceStatus status,
            ServiceMessagePrefix prefix) throws SOSNotifierSendException;

    public abstract void close();

    protected void setTableFields(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        tableFields = new HashMap<String, String>();
        if (mo != null) {
            tableFields.putAll(mo.toMap(true, PREFIX_TABLE_ORDERS));
        }
        if (mos != null) {
            tableFields.putAll(mos.toMap(true, PREFIX_TABLE_ORDER_STEPS));
        }
    }

    protected Map<String, String> getTableFields() {
        return tableFields;
    }

    protected String getValue(ServiceMessagePrefix prefix) {
        return prefix == null ? "" : prefix.name();
    }

    protected String getValue(ServiceStatus status) {
        return status == null ? "" : status.name();
    }

    protected void evaluate(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, ServiceStatus status, ServiceMessagePrefix prefix) {
        setTableFields(mo, mos);

        serviceStatus = getValue(status);
        serviceMessagePrefix = getValue(prefix);

        setJocHrefWorkflowOrder(mo);
        setJocHrefWorkflowJob(mo, mos);
    }

    protected String resolve(String msg, boolean resolveEnv) {
        return resolve(msg, resolveEnv, null);
    }

    protected String resolve(String msg, boolean resolveEnv, Map<String, String> map) {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor(false, "${", "}");
        ps.addKey(VAR_SERVICE_STATUS, serviceStatus);
        ps.addKey(VAR_SERVICE_MESSAGE_PREFIX, serviceMessagePrefix);
        ps.addKey(VAR_JOC_HREF_ORDER, jocHrefWorkflowOrder);
        ps.addKey(VAR_JOC_HREF_JOB, jocHrefWorkflowJob);
        getTableFields().entrySet().forEach(e -> {
            ps.addKey(e.getKey(), e.getValue());
        });
        if (map != null) {
            map.entrySet().forEach(e -> {
                ps.addKey(e.getKey(), e.getValue());
            });
        }
        String m = ps.replace(msg);
        return resolveEnv ? ps.replaceEnvVars(m) : m;
    }

    private void setJocHrefWorkflowOrder(DBItemMonitoringOrder mo) {
        if (jocHrefWorkflowOrder == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART_LOG);
            sb.append("controller_id=").append(encode(mo.getControllerId()));
            sb.append("&orderId=").append(encode(mo.getOrderId()));
            sb.append("&workflow=").append(encode(mo.getWorkflowName()));
            sb.append("&historyId=").append(mo.getHistoryId());
            jocHrefWorkflowOrder = sb.toString();
        }
    }

    private void setJocHrefWorkflowJob(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (jocHrefWorkflowJob == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART_LOG);
            sb.append("controller_id=").append(encode(mo.getControllerId()));
            sb.append("&job=").append(encode(mos.getJobName()));
            sb.append("&taskId=").append(mos.getHistoryId());
            jocHrefWorkflowJob = sb.toString();
        }
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    protected String nl2sp(String value) {
        return value.replaceAll("\\r\\n|\\r|\\n", " ");
    }

    protected String getServiceStatus() {
        return serviceStatus;
    }

    protected String getServiceMessagePrefix() {
        return serviceMessagePrefix;
    }
    
    protected String jocHrefWorkflowOrder() {
        return jocHrefWorkflowOrder;
    }
    
    protected String jocHrefWorkflowJob() {
        return jocHrefWorkflowJob;
    }
}
