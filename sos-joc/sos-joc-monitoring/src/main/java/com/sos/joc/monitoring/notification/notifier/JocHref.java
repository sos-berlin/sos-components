package com.sos.joc.monitoring.notification.notifier;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.Configuration;

public class JocHref {

    private static final String JOC_URI_PART = "/joc/#/";

    private static final String VAR_JOC_HREF_WORKFLOW = "JOC_HREF_WORKFLOW";
    private static final String VAR_JOC_HREF_ORDER = "JOC_HREF_ORDER";
    private static final String VAR_JOC_HREF_ORDER_LOG = "JOC_HREF_ORDER_LOG";
    private static final String VAR_JOC_HREF_JOB = "JOC_HREF_JOB";
    private static final String VAR_JOC_HREF_JOB_LOG = "JOC_HREF_JOB_LOG";

    private static final boolean SET_AS_ENVS = true;

    private String workflow;
    private String workflowOrder;
    private String workflowOrderLog;
    private String workflowJob;
    private String workflowJobLog;

    protected JocHref(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        setWorkflow(mo);
        setWorkflowOrder(mo);
        setWorkflowOrderLog(mo);
        setWorkflowJob(mo, mos);
    }

    protected void addKeys(SOSParameterSubstitutor ps) {
        ps.addKey(VAR_JOC_HREF_WORKFLOW, workflow);
        ps.addKey(VAR_JOC_HREF_ORDER, workflowOrder);
        ps.addKey(VAR_JOC_HREF_ORDER_LOG, workflowOrderLog);
        ps.addKey(VAR_JOC_HREF_JOB, workflowJob);
        ps.addKey(VAR_JOC_HREF_JOB_LOG, workflowJobLog);
    }

    protected void addEnvs(Map<String, String> map) {
        if (!SET_AS_ENVS) {
            return;
        }
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_WORKFLOW, workflow);
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_ORDER, workflowOrder);
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_ORDER_LOG, workflowOrderLog);
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_JOB, workflowJob);
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_HREF_JOB_LOG, workflowJobLog);
    }

    private void setWorkflow(DBItemMonitoringOrder mo) {
        if (workflow == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART);
            sb.append("workflows/workflow?");
            if (mo != null) {
                sb.append("path=").append(encode(mo.getWorkflowName()));
                sb.append("&controllerId=").append(encode(mo.getControllerId()));
            }
            workflow = sb.toString();
        }
    }

    private void setWorkflowOrder(DBItemMonitoringOrder mo) {
        if (workflowOrder == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART);
            sb.append("history/order?");
            if (mo != null) {
                sb.append("orderId=").append(encode(mo.getOrderId()));
                sb.append("&workflow=").append(encode(mo.getWorkflowName()));
                sb.append("&controllerId=").append(encode(mo.getControllerId()));
            }
            workflowOrder = sb.toString();
        }
    }

    private void setWorkflowOrderLog(DBItemMonitoringOrder mo) {
        if (workflowOrderLog == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART);
            sb.append("log?");
            if (mo != null) {
                sb.append("historyId=").append(mo.getHistoryId());
                sb.append("&orderId=").append(encode(mo.getOrderId()));
                sb.append("&workflow=").append(encode(mo.getWorkflowName()));
                sb.append("&controllerId=").append(encode(mo.getControllerId()));
            }
            workflowOrderLog = sb.toString();
        }
    }

    private void setWorkflowJob(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (workflowJobLog == null) {
            StringBuilder sb = new StringBuilder(Configuration.getJocUri());
            sb.append(JOC_URI_PART);
            sb.append("log?");
            if (mo != null) {
                sb.append("controllerId=").append(encode(mo.getControllerId()));
            }
            if (mos != null) {
                sb.append("&job=").append(encode(mos.getJobName()));
                sb.append("&taskId=").append(mos.getHistoryId());
            }
            workflowJobLog = sb.toString();
            workflowJob = workflowJobLog.replace("/log?", "/task?");
        }
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

}
