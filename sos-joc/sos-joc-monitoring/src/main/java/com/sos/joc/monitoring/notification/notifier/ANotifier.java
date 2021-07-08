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

public abstract class ANotifier {

    public enum Status {
        SUCCESS, ERROR, RECOVERED
    }

    protected static final String PREFIX_ENV_VAR = "JS7_MON";
    protected static final String PREFIX_ENV_TABLE_FIELD_VAR = PREFIX_ENV_VAR + "_TABLE";

    protected static final String VAR_STATUS = "STATUS";

    private static final String PREFIX_TABLE_ORDERS = "MON_O";
    private static final String PREFIX_TABLE_ORDER_STEPS = "MON_OS";

    private static final String JOC_URI_PART = "/joc/#/";

    protected static final String VAR_JOC_HREF_WORKFLOW = "JOC_HREF_WORKFLOW";
    protected static final String VAR_JOC_HREF_ORDER = "JOC_HREF_ORDER";
    protected static final String VAR_JOC_HREF_JOB = "JOC_HREF_JOB";

    private Map<String, String> tableFields;

    private String jocHrefWorkflow;
    private String jocHrefWorkflowOrder;
    private String jocHrefWorkflowJob;

    public abstract void notify(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, Status status);

    public abstract void close();

    protected Map<String, String> getTableFields() {
        return tableFields;
    }

    protected String getValue(Status status) {
        return status == null ? "" : status.name();
    }

    protected void set(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        setTableFields(mo, mos);
        setJocHrefs(mo, mos);
    }

    protected String resolve(String msg, Status status, boolean resolveEnv) {
        return resolve(msg, status, resolveEnv, null);
    }

    protected String resolve(String msg, Status status, boolean resolveEnv, Map<String, String> map) {
        SOSParameterSubstitutor ps = new SOSParameterSubstitutor(false, "${", "}");
        ps.addKey(VAR_STATUS, status.name());
        ps.addKey(VAR_JOC_HREF_WORKFLOW, jocHrefWorkflow);
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

    protected String getInfo4execute(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, Status status, String addInfo) {
        StringBuilder sb = new StringBuilder("[notification]");
        sb.append(getInfo(mo, mos, status));
        sb.append("[execute]");
        if (addInfo != null) {
            sb.append(addInfo);
        }
        return sb.toString();
    }

    protected String getInfo4executeException(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, Status status, String addInfo, Throwable e) {
        StringBuilder sb = new StringBuilder("[notification][EXCEPTION]");
        sb.append(getInfo(mo, mos, status));
        if (addInfo != null) {
            sb.append(addInfo);
        }
        if (e != null) {
            sb.append(e.toString());
            e.printStackTrace();
        }
        return sb.toString();
    }

    private StringBuilder getInfo(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos, Status status) {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(status.name()).append("]");
        sb.append("[");
        if (mo != null) {
            sb.append("controllerId=").append(mo.getControllerId());
            sb.append(",workflow=").append(mo.getWorkflowName());
            sb.append(",orderId=").append(mo.getOrderId());
        }
        if (mos != null) {
            sb.append(",job=").append(mos.getJobName());
            sb.append(",label=").append(mos.getJobLabel());
            sb.append(",position=").append(mos.getPosition());
        }
        sb.append("]");
        return sb;
    }

    private void setTableFields(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        tableFields = new HashMap<String, String>();
        if (mo != null) {
            tableFields.putAll(mo.toMap(true, PREFIX_TABLE_ORDERS));
        }
        if (mos != null) {
            tableFields.putAll(mos.toMap(true, PREFIX_TABLE_ORDER_STEPS));
        }
    }

    private void setJocHrefs(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        setJocHrefWorkflow(mo);
        setJocHrefWorkflowOrder(mo);
        setJocHrefWorkflowJob(mo, mos);
    }

    private void setJocHrefWorkflow(DBItemMonitoringOrder mo) {
        if (jocHrefWorkflow == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART);
            sb.append("workflows/workflow?");
            if (mo != null) {
                sb.append("path=").append(encode(mo.getWorkflowName()));
                sb.append("&controllerId=").append(encode(mo.getControllerId()));
            }
            jocHrefWorkflow = sb.toString();
        }
    }

    private void setJocHrefWorkflowOrder(DBItemMonitoringOrder mo) {
        if (jocHrefWorkflowOrder == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART);
            sb.append("history/order?");
            if (mo != null) {
                sb.append("orderId=").append(encode(mo.getOrderId()));
                sb.append("&workflow=").append(encode(mo.getWorkflowName()));
                sb.append("&controllerId=").append(encode(mo.getControllerId()));
            }
            jocHrefWorkflowOrder = sb.toString();
        }
    }

    private void setJocHrefWorkflowJob(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (jocHrefWorkflowJob == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART);
            sb.append("log2?");
            if (mo != null) {
                sb.append("controllerId=").append(encode(mo.getControllerId()));
            }
            if (mos != null) {
                sb.append("&job=").append(encode(mos.getJobName()));
                sb.append("&taskId=").append(mos.getHistoryId());
            }
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

    protected String jocHrefWorkflow() {
        return jocHrefWorkflow;
    }

    protected String jocHrefWorkflowOrder() {
        return jocHrefWorkflowOrder;
    }

    protected String jocHrefWorkflowJob() {
        return jocHrefWorkflowJob;
    }
}
