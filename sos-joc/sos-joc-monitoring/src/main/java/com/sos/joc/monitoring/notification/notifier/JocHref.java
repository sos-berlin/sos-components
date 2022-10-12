package com.sos.joc.monitoring.notification.notifier;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.sos.commons.util.SOSParameterSubstitutor;
import com.sos.commons.util.SOSString;
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

    private static final String VAR_JOC_REVERSE_PROXY_HREF_WORKFLOW = "JOC_REVERSE_PROXY_HREF_WORKFLOW";
    private static final String VAR_JOC_REVERSE_PROXY_HREF_ORDER = "JOC_REVERSE_PROXY_HREF_ORDER";
    private static final String VAR_JOC_REVERSE_PROXY_HREF_ORDER_LOG = "JOC_REVERSE_PROXY_HREF_ORDER_LOG";
    private static final String VAR_JOC_REVERSE_PROXY_HREF_JOB = "JOC_REVERSE_PROXY_HREF_JOB";
    private static final String VAR_JOC_REVERSE_PROXY_HREF_JOB_LOG = "JOC_REVERSE_PROXY_HREF_JOB_LOG";

    private static final boolean SET_AS_ENVS = true;

    private String workflow;
    private String workflowOrder;
    private String workflowOrderLog;
    private String workflowJob;
    private String workflowJobLog;

    private String reverseProxyWorkflow;
    private String reverseProxyWorkflowOrder;
    private String reverseProxyWorkflowOrderLog;
    private String reverseProxyWorkflowJob;
    private String reverseProxyWorkflowJobLog;

    protected JocHref(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        setWorkflow(mo);
        setWorkflowOrder(mo);
        setWorkflowOrderLog(mo);
        setWorkflowJob(mo, mos);
        setWorkflowJobLog(mo, mos);

        setReverseProxyWorkflow(mo);
        setReverseProxyWorkflowOrder(mo);
        setReverseProxyWorkflowOrderLog(mo);
        setReverseProxyWorkflowJob(mo, mos);
        setReverseProxyWorkflowJobLog(mo, mos);
    }

    protected void addKeys(SOSParameterSubstitutor ps) {
        ps.addKey(VAR_JOC_HREF_WORKFLOW, workflow);
        ps.addKey(VAR_JOC_HREF_ORDER, workflowOrder);
        ps.addKey(VAR_JOC_HREF_ORDER_LOG, workflowOrderLog);
        ps.addKey(VAR_JOC_HREF_JOB, workflowJob);
        ps.addKey(VAR_JOC_HREF_JOB_LOG, workflowJobLog);

        ps.addKey(VAR_JOC_REVERSE_PROXY_HREF_WORKFLOW, reverseProxyWorkflow);
        ps.addKey(VAR_JOC_REVERSE_PROXY_HREF_ORDER, reverseProxyWorkflowOrder);
        ps.addKey(VAR_JOC_REVERSE_PROXY_HREF_ORDER_LOG, reverseProxyWorkflowOrderLog);
        ps.addKey(VAR_JOC_REVERSE_PROXY_HREF_JOB, reverseProxyWorkflowJob);
        ps.addKey(VAR_JOC_REVERSE_PROXY_HREF_JOB_LOG, reverseProxyWorkflowJobLog);
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

        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_REVERSE_PROXY_HREF_WORKFLOW, reverseProxyWorkflow);
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_REVERSE_PROXY_HREF_ORDER, reverseProxyWorkflowOrder);
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_REVERSE_PROXY_HREF_ORDER_LOG, reverseProxyWorkflowOrderLog);
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_REVERSE_PROXY_HREF_JOB, reverseProxyWorkflowJob);
        map.put(ANotifier.PREFIX_ENV_VAR + "_" + VAR_JOC_REVERSE_PROXY_HREF_JOB_LOG, reverseProxyWorkflowJobLog);
    }

    private void setWorkflow(DBItemMonitoringOrder mo) {
        if (workflow == null) {
            workflow = getWorkflowUri(Configuration.getJocUri(), mo);
        }
    }

    private void setReverseProxyWorkflow(DBItemMonitoringOrder mo) {
        if (reverseProxyWorkflow == null) {
            reverseProxyWorkflow = SOSString.isEmpty(Configuration.getJocReverseProxyUri()) ? "" : getWorkflowUri(Configuration
                    .getJocReverseProxyUri(), mo);
        }
    }

    private String getWorkflowUri(String jocUri, DBItemMonitoringOrder mo) {
        StringBuilder sb = new StringBuilder(jocUri);
        sb.append(JOC_URI_PART);
        sb.append("workflows/workflow?");
        if (mo != null) {
            sb.append("path=").append(encode(mo.getWorkflowName()));
            sb.append("&controllerId=").append(encode(mo.getControllerId()));
        }
        return sb.toString();
    }

    private void setWorkflowOrder(DBItemMonitoringOrder mo) {
        if (workflowOrder == null) {
            workflowOrder = getWorkflowOrderUri(Configuration.getJocUri(), mo);
        }
    }

    private void setReverseProxyWorkflowOrder(DBItemMonitoringOrder mo) {
        if (reverseProxyWorkflowOrder == null) {
            reverseProxyWorkflowOrder = SOSString.isEmpty(Configuration.getJocReverseProxyUri()) ? "" : getWorkflowOrderUri(Configuration
                    .getJocReverseProxyUri(), mo);
        }
    }

    private String getWorkflowOrderUri(String jocUri, DBItemMonitoringOrder mo) {
        StringBuilder sb = new StringBuilder(jocUri);
        sb.append(JOC_URI_PART);
        sb.append("history/order?");
        if (mo != null) {
            sb.append("orderId=").append(encode(mo.getOrderId()));
            sb.append("&workflow=").append(encode(mo.getWorkflowName()));
            sb.append("&controllerId=").append(encode(mo.getControllerId()));
        }
        return sb.toString();
    }

    private void setWorkflowOrderLog(DBItemMonitoringOrder mo) {
        if (workflowOrderLog == null) {
            workflowOrderLog = getWorkflowOrderLogUri(Configuration.getJocUri(), mo);
        }
    }

    private void setReverseProxyWorkflowOrderLog(DBItemMonitoringOrder mo) {
        if (reverseProxyWorkflowOrderLog == null) {
            reverseProxyWorkflowOrderLog = SOSString.isEmpty(Configuration.getJocReverseProxyUri()) ? "" : getWorkflowOrderLogUri(Configuration
                    .getJocReverseProxyUri(), mo);
        }
    }

    private String getWorkflowOrderLogUri(String jocUri, DBItemMonitoringOrder mo) {
        StringBuilder sb = new StringBuilder(jocUri);
        sb.append(JOC_URI_PART);
        sb.append("log?");
        if (mo != null) {
            sb.append("historyId=").append(mo.getHistoryId());
            sb.append("&orderId=").append(encode(mo.getOrderId()));
            sb.append("&workflow=").append(encode(mo.getWorkflowName()));
            sb.append("&controllerId=").append(encode(mo.getControllerId()));
        }
        return sb.toString();
    }

    private void setWorkflowJob(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (workflowJob == null) {
            workflowJob = getWorkflowJobUri(Configuration.getJocUri(), mo, mos);
        }
    }

    private void setReverseProxyWorkflowJob(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (reverseProxyWorkflowJob == null) {
            reverseProxyWorkflowJob = SOSString.isEmpty(Configuration.getJocReverseProxyUri()) ? "" : getWorkflowJobUri(Configuration
                    .getJocReverseProxyUri(), mo, mos);
        }
    }

    private String getWorkflowJobUri(String jocUri, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        StringBuilder sb = new StringBuilder(jocUri);
        sb.append(JOC_URI_PART);
        sb.append("history/task?");
        if (mo != null) {
            sb.append("controllerId=").append(encode(mo.getControllerId()));
            // sb.append("&workflow=").append(encode(mo.getWorkflowName()));
        }
        if (mos != null) {
            // sb.append("&job=").append(encode(mos.getJobName()));
            sb.append("&taskId=").append(mos.getHistoryId());
        }
        return sb.toString();
    }

    private void setWorkflowJobLog(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (workflowJobLog == null) {
            workflowJobLog = getWorkflowJobLogUri(Configuration.getJocUri(), mo, mos);
        }
    }

    private void setReverseProxyWorkflowJobLog(DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        if (reverseProxyWorkflowJobLog == null) {
            reverseProxyWorkflowJobLog = SOSString.isEmpty(Configuration.getJocReverseProxyUri()) ? "" : getWorkflowJobLogUri(Configuration
                    .getJocReverseProxyUri(), mo, mos);
        }
    }

    private String getWorkflowJobLogUri(String jocUri, DBItemMonitoringOrder mo, DBItemMonitoringOrderStep mos) {
        StringBuilder sb = new StringBuilder(jocUri);
        sb.append(JOC_URI_PART);
        sb.append("log?");
        if (mo != null) {
            sb.append("controllerId=").append(encode(mo.getControllerId()));
        }
        if (mos != null) {
            sb.append("&job=").append(encode(mos.getJobName()));
            sb.append("&taskId=").append(mos.getHistoryId());
        }
        return sb.toString();
    }

    private String encode(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

}
