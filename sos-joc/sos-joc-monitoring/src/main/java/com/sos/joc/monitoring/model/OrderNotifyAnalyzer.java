package com.sos.joc.monitoring.model;

import java.io.Serializable;
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
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.db.LastWorkflowNotificationDBItemEntity;
import com.sos.joc.monitoring.model.bean.MonitorOrderResult;
import com.sos.joc.monitoring.model.bean.MonitorOrderStepResult;
import com.sos.joc.monitoring.model.bean.MonitorOrderStepResultWarn;
import com.sos.joc.monitoring.model.bean.NotificationObject;
import com.sos.joc.monitoring.model.bean.NotificationRun;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.OrderNotificationRange;

public class OrderNotifyAnalyzer implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderNotifyAnalyzer.class);

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

    protected List<NotificationRun> analyze(OrderNotifierModel notifier, OrderNotificationRange range, DBLayerMonitoring dbLayer,
            List<NotificationRun> notifications, MonitorOrderResult mor, MonitorOrderStepResult mosr, NotificationType type,
            List<MonitorOrderStepResultWarn> warnings) throws Exception {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        boolean isWorkflow = false;
        switch (range) {
        case WORKFLOW_JOB:
            orderId = mosr.getStep().getHistoryOrderId();
            stepId = mosr.getStep().getHistoryId();
            // workflowPosition = mosr.getStep().getWorkflowPosition();
            controllerId = mosr.getStep().getControllerId();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][analyze][%s][%s][notification ids=%s]%s", Configuration.LOG_INTENT, controllerId, range, type,
                        toString(notifications), SOSString.toString(mosr)));
            }

            break;
        case WORKFLOW:
            isWorkflow = true;
            orderId = mor.getOrder().getHistoryId();
            stepId = mor.getOrder().getCurrentHistoryOrderStepId();
            // workflowPosition = mor.getOrder().getWorkflowPosition();
            controllerId = mor.getOrder().getControllerId();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[%s][analyze][%s][%s][notification ids=%s]%s", Configuration.LOG_INTENT, controllerId, range, type,
                        toString(notifications), SOSString.toString(mor)));
            }

            break;
        }

        switch (type) {
        case SUCCESS:
        case ERROR:
            String key = type + "-" + controllerId + "-" + orderId;
            if (isWorkflow) {
                NotificationObject notificationObject = notifier.getNotificationObjects().get(key);
                if (notificationObject != null) {
                    if (stepId.equals(notificationObject.getOrderStepHistoryId())) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format(
                                    "%s[%s][analyze][%s][%s][notification ids=%s][skip][same order step(historyId=%s) has already been sent][order historyId=%s]%s",
                                    Configuration.LOG_INTENT, controllerId, range, type, toString(notifications), stepId, orderId,
                                    notificationObject));
                        }
                        notifier.getNotificationObjects().remove(key);
                        return null;
                    } else {
                        notifier.getNotificationObjects().remove(key);
                    }
                }
            } else {
                notifier.getNotificationObjects().put(key, new NotificationObject(mosr.getStep()));
            }
            break;
        case RECOVERED:
            if (isWorkflow) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][analyze][%s][%s][notification ids=%s][skip][nothing to recovery if %s]orderId=%s",
                            Configuration.LOG_INTENT, controllerId, range, type, toString(notifications), range, orderId));
                }
                return null;
            }

            toRecovery = new HashMap<>();
            for (NotificationRun n : notifications) {
                LastWorkflowNotificationDBItemEntity last = dbLayer.getLastNotification(n.getConfiguredNotification().getNotificationId(), range,
                        orderId);
                if (last == null || !last.getType().equals(NotificationType.ERROR.intValue())) {
                    continue;
                }
                toRecovery.put(last.getNotificationId(), last);
            }
            if (toRecovery.size() == 0) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[%s][analyze][%s][%s][notification ids=%s][skip][nothing to recovery found]orderId=%s",
                            Configuration.LOG_INTENT, controllerId, range, type, toString(notifications), orderId));
                }
                return null;
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
                LOGGER.debug(String.format("%s[%s][analyze][%s][%s][notification ids=%s][skip][orderId=%s][stepId=%s]order and step not found",
                        Configuration.LOG_INTENT, controllerId, range, type, toString(notifications), orderId, stepId));
            }
            return null;
        }

        if (isWorkflow) {
            workflowPosition = order.getEndWorkflowPosition() == null ? order.getWorkflowPosition() : order.getEndWorkflowPosition();
        } else {
            workflowPosition = orderStep.getWorkflowPosition();
        }

        if (checkPeriodAge) {
            switch (range) {
            case WORKFLOW:
                if (order != null) {
                    // not send entries older as ...
                    if (isPeriodExeeded(order.getStartTime(), order.getEndTime())) {
                        return null;
                    }
                }
                break;
            case WORKFLOW_JOB:
                if (orderStep != null) {
                    // not send entries older as ...
                    if (isPeriodExeeded(orderStep.getStartTime(), orderStep.getEndTime())) {
                        return null;
                    }
                }
                break;
            }
        }

        return notifications;
    }

    private String toString(List<NotificationRun> list) {
        return String.join(",", list.stream().map(e -> e.getConfiguredNotification().getNotificationId()).collect(Collectors.toList()));
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
