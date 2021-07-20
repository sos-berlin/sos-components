package com.sos.joc.monitoring.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.db.LastWorkflowNotificationDBItemEntity;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

public class NotifyAnalyzer {

    private NotificationRange range;
    private Long orderId;
    private Long stepId;
    private String workflowPosition;
    private String controllerId;

    private DBItemMonitoringOrder order;
    private DBItemMonitoringOrderStep orderStep;

    private Map<String, LastWorkflowNotificationDBItemEntity> toRecovery;
    private List<String> sendedWarnings;

    protected boolean analyze(DBLayerMonitoring dbLayer, List<Notification> list, HistoryOrderBean hob, HistoryOrderStepBean hosb,
            NotificationType type) throws Exception {
        if (hob == null) {
            range = NotificationRange.WORKFLOW_JOB;
            orderId = hosb.getHistoryOrderId();
            stepId = hosb.getHistoryId();
            workflowPosition = hosb.getWorkflowPosition();
            controllerId = hosb.getControllerId();
        } else {
            range = NotificationRange.WORKFLOW;
            orderId = hob.getHistoryId();
            stepId = hob.getCurrentHistoryOrderStepId();
            workflowPosition = hob.getWorkflowPosition();
            controllerId = hob.getControllerId();
        }

        switch (type) {
        case RECOVERED:
            toRecovery = new HashMap<>();
            for (Notification n : list) {
                LastWorkflowNotificationDBItemEntity last = dbLayer.getLastNotification(n.getName(), range, orderId);
                if (last == null || !last.getType().equals(NotificationType.ERROR.intValue())) {
                    continue;
                }
                toRecovery.put(last.getName(), last);
            }
            if (toRecovery.size() == 0) {
                return false;
            }
            break;
        case WARNING:
            List<String> ws = dbLayer.getNotificationNames(type, range, orderId, stepId);
            if (ws != null && ws.size() > 0) {
                sendedWarnings = ws;
            }
            break;
        default:
            break;
        }

        order = dbLayer.getMonitoringOrder(orderId, true);
        orderStep = dbLayer.getMonitoringOrderStep(stepId, true);
        if (order == null && orderStep == null) {
            return false;
        }

        return true;
    }

    public NotificationRange getRange() {
        return range;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getStepId() {
        return stepId;
    }

    public String getWorkflowPosition() {
        return workflowPosition;
    }

    public String getControllerId() {
        return controllerId;
    }

    public Map<String, LastWorkflowNotificationDBItemEntity> getToRecovery() {
        return toRecovery;
    }

    public List<String> getSendedWarnings() {
        return sendedWarnings;
    }

    public DBItemMonitoringOrder getOrder() {
        return order;
    }

    public DBItemMonitoringOrderStep getOrderStep() {
        return orderStep;
    }

}
