package com.sos.joc.monitoring.model;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.history.JobWarning;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.db.LastWorkflowNotificationDBItemEntity;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.HistoryOrderStepResultWarn;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

public class NotifyAnalyzer {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifyAnalyzer.class);

    /** milliseconds, 1day */
    private static final int MAX_PERIOD_AGE_BY_END = 24 * 60 * 60 * 1_000;
    private static final int MAX_PERIOD_AGE_BY_START = MAX_PERIOD_AGE_BY_END * 2;

    private Long orderId;
    private Long stepId;
    private String workflowPosition;
    private String controllerId;

    private DBItemMonitoringOrder order;
    private DBItemMonitoringOrderStep orderStep;

    private Map<String, LastWorkflowNotificationDBItemEntity> toRecovery;
    private Map<String, Set<JobWarning>> sentWarnings;

    // TODO
    private boolean checkPeriodAge = false;

    protected boolean analyze(NotificationRange range, DBLayerMonitoring dbLayer, List<Notification> list, HistoryOrderBean hob,
            HistoryOrderStepBean hosb, NotificationType type, List<HistoryOrderStepResultWarn> warnings) throws Exception {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        switch (range) {
        case WORKFLOW_JOB:
            orderId = hosb.getHistoryOrderId();
            stepId = hosb.getHistoryId();
            workflowPosition = hosb.getWorkflowPosition();
            controllerId = hosb.getControllerId();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[analyze][%s][%s][notification ids=%s]%s", Configuration.LOG_INTENT, range, type, toString(list),
                        SOSString.toString(hosb)));
            }
            break;
        case WORKFLOW:
            orderId = hob.getHistoryId();
            stepId = hob.getCurrentHistoryOrderStepId();
            workflowPosition = hob.getWorkflowPosition();
            controllerId = hob.getControllerId();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[analyze][%s][%s][notification ids=%s]%s", Configuration.LOG_INTENT, range, type, toString(list),
                        SOSString.toString(hob)));
            }
            break;
        }

        switch (type) {
        case RECOVERED:
            toRecovery = new HashMap<>();
            for (Notification n : list) {
                LastWorkflowNotificationDBItemEntity last = dbLayer.getLastNotification(n.getNotificationId(), range, orderId);
                if (last == null || !last.getType().equals(NotificationType.ERROR.intValue())) {
                    continue;
                }
                toRecovery.put(last.getNotificationId(), last);
            }
            if (toRecovery.size() == 0) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[analyze][%s][%s][notification ids=%s][skip][orderId=%s]nothing to recovery found",
                            Configuration.LOG_INTENT, range, type, toString(list), orderId));
                }
                return false;
            }
            break;
        case WARNING:
            List<DBItemNotification> wns = dbLayer.getNotifications(type, range, orderId, stepId);
            if (wns != null && wns.size() > 0) {
                sentWarnings = new HashMap<>();
                for (DBItemNotification wn : wns) {
                    Set<JobWarning> set = sentWarnings.get(wn.getNotificationId());
                    if (set == null) {
                        set = new HashSet<>();
                    }
                    set.add(wn.getWarnAsEnum());
                    sentWarnings.put(wn.getNotificationId(), set);
                }
            }
            break;
        default:
            break;
        }

        order = dbLayer.getMonitoringOrder(orderId, true);
        orderStep = dbLayer.getMonitoringOrderStep(stepId, true);
        if (order == null && orderStep == null) {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[analyze][%s][%s][notification ids=%s][skip][orderId=%s][stepId=%s]order and step not found",
                        Configuration.LOG_INTENT, range, type, toString(list), orderId, stepId));
            }
            return false;
        }

        if (checkPeriodAge) {
            switch (range) {
            case WORKFLOW:
                if (order != null) {
                    // not send entries older as ...
                    if (isPeriodExeeded(order.getStartTime(), order.getEndTime())) {
                        return false;
                    }
                }
                break;
            case WORKFLOW_JOB:
                if (orderStep != null) {
                    // not send entries older as ...
                    if (isPeriodExeeded(orderStep.getStartTime(), orderStep.getEndTime())) {
                        return false;
                    }
                }
                break;
            }
        }

        return true;
    }

    private String toString(List<Notification> list) {
        return String.join(",", list.stream().map(e -> e.getNotificationId()).collect(Collectors.toList()));
    }

    private boolean isPeriodExeeded(Date startTime, Date endTime) {
        if (endTime == null) {
            long diff = Date.from(Instant.now()).getTime() - startTime.getTime();
            return diff >= MAX_PERIOD_AGE_BY_START;
        } else {
            long diff = Date.from(Instant.now()).getTime() - endTime.getTime();
            return diff >= MAX_PERIOD_AGE_BY_END;
        }
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

    public Map<String, Set<JobWarning>> getSentWarnings() {
        return sentWarnings;
    }

    public DBItemMonitoringOrder getOrder() {
        return order;
    }

    public DBItemMonitoringOrderStep getOrderStep() {
        return orderStep;
    }

}
