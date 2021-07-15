package com.sos.joc.monitoring.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

public class NotifyAnalyzer {

    private NotificationRange range;
    private Long orderId;
    private Long stepId;
    private String workflowPosition;

    private DBItemMonitoringOrder order;
    private DBItemMonitoringOrderStep orderStep;

    private Map<String, DBItemNotification> toRecovery;
    private List<String> sendedWarnings;

    protected boolean analyze(DBLayerMonitoring dbLayer, List<Notification> list, HistoryOrderBean hob, HistoryOrderStepBean hosb,
            NotificationType type) throws Exception {
        if (hob == null) {
            range = NotificationRange.WORKFLOW_JOB;
            orderId = hosb.getHistoryOrderId();
            stepId = hosb.getHistoryId();
            workflowPosition = hosb.getWorkflowPosition();
        } else {
            range = NotificationRange.WORKFLOW;
            orderId = hob.getHistoryId();
            stepId = hob.getCurrentHistoryOrderStepId();
            workflowPosition = hob.getWorkflowPosition();
        }

        switch (type) {
        case RECOVERED:
            toRecovery = new HashMap<>();
            for (Notification n : list) {
                DBItemNotification last = dbLayer.getLastNotification(n.getName(), range, orderId);
                if (last == null || !last.getType().equals(NotificationType.ERROR.intValue())) {
                    continue;
                }
                toRecovery.put(n.getName(), last);
            }
            if (toRecovery.size() == 0) {
                return false;
            }
            break;
        case WARNING:
            List<DBItemNotification> ws = dbLayer.getNotifications(type, range, orderId, stepId);
            if (ws != null && ws.size() > 0) {
                sendedWarnings = ws.stream().map(e -> {
                    return e.getName();
                }).collect(Collectors.toList());
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

    public Map<String, DBItemNotification> getToRecovery() {
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
