package com.sos.joc.monitoring.model;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSString;
import com.sos.history.JobWarning;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.NotificationCreated;
import com.sos.joc.monitoring.MonitorService;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.db.LastWorkflowNotificationDBItemEntity;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.HistoryOrderStepResult;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.HistoryOrderStepResultWarn;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.ToNotify;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.joc.monitoring.notification.notifier.NotifyResult;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

public class HistoryNotifierModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryNotifierModel.class);

    private static final String LOG_IDENTIFIER = String.format("[%s][%s]", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY,
            MonitorService.NOTIFICATION_IDENTIFIER);
    private static final String IDENTIFIER = String.format("%s_%s", MonitorService.SUB_SERVICE_IDENTIFIER_HISTORY,
            MonitorService.NOTIFICATION_IDENTIFIER);

    private final SOSHibernateFactory factory;

    private DBLayerMonitoring dbLayer;
    private AtomicBoolean closed = new AtomicBoolean();

    private ExecutorService threadPool;

    protected HistoryNotifierModel(ThreadGroup threadGroup, Path hibernateConfigFile) {
        MonitorService.setLogger();

        this.factory = createFactory(hibernateConfigFile);
        if (factory == null) {
            LOGGER.info(String.format("[%s][skip]due to the database factory errors", LOG_IDENTIFIER));
        } else {
            dbLayer = new DBLayerMonitoring(IDENTIFIER);
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(threadGroup, IDENTIFIER));
        }
    }

    protected void notify(ToNotify toNotifyPayloads, ToNotify toNotifyExtraStepsWarnings) {
        if (!Configuration.INSTANCE.hasNotifications() || factory == null) {
            return;
        }

        Runnable task = new Runnable() {

            @Override
            public void run() {
                MonitorService.setLogger();
                if (!closed.get()) {
                    notifySteps(toNotifyPayloads.getSteps());
                    notifyOrders(toNotifyPayloads.getErrorOrders(), toNotifyPayloads.getSuccessOrders());
                    notifyStepsWarnings(toNotifyExtraStepsWarnings.getSteps());
                }
            }
        };
        if (!closed.get()) {
            threadPool.submit(task);
        }
    }

    private void notifySteps(List<HistoryOrderStepResult> steps) {
        for (HistoryOrderStepResult step : steps) {
            notifyStep(step);
        }
    }

    private void notifyStepsWarnings(List<HistoryOrderStepResult> steps) {
        for (HistoryOrderStepResult step : steps) {
            notifyStepWarning(step);
        }
    }

    private void notifyOrders(List<HistoryOrderBean> error, List<HistoryOrderBean> success) {
        for (HistoryOrderBean order : error) {
            notifyOrder(order, NotificationType.ERROR);
        }
        for (HistoryOrderBean order : success) {
            notifyOrder(order, NotificationType.SUCCESS);
        }
    }

    private void notifyStep(HistoryOrderStepResult r) {
        List<Notification> result;
        HistoryOrderStepBean hosb = r.getStep();
        NotificationRange range = NotificationRange.WORKFLOW_JOB;

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[notifyStep][start][%s]%s", range, r.toString()));
        }

        notifyStepWarning(r);

        if (hosb.getError()) {
            result = Configuration.INSTANCE.findWorkflowMatches(range, Configuration.INSTANCE.getOnError(), hosb.getControllerId(), hosb
                    .getWorkflowPath(), hosb.getJobName(), hosb.getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(range, result, null, hosb, NotificationType.ERROR, null);
        } else {
            // RECOVERY
            result = Configuration.INSTANCE.findWorkflowMatches(range, Configuration.INSTANCE.getOnError(), hosb.getControllerId(), hosb
                    .getWorkflowPath(), hosb.getJobName(), hosb.getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(range, result, null, hosb, NotificationType.RECOVERED, null);
            // SUCCESS
            result = Configuration.INSTANCE.findWorkflowMatches(range, Configuration.INSTANCE.getOnSuccess(), hosb.getControllerId(), hosb
                    .getWorkflowPath(), hosb.getJobName(), hosb.getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(range, result, null, hosb, NotificationType.SUCCESS, null);
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[notifyStep][end][%s]%s", range, r.toString()));
        }
    }

    private void notifyOrder(HistoryOrderBean hob, NotificationType type) {
        List<Notification> result;
        NotificationRange range = NotificationRange.WORKFLOW;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[notifyOrder][start][%s][%s]%s", range, type, SOSString.toString(hob)));
        }
        switch (type) {
        case ERROR:
            result = Configuration.INSTANCE.findWorkflowMatches(NotificationRange.WORKFLOW, Configuration.INSTANCE.getOnError(), hob
                    .getControllerId(), hob.getWorkflowPath());
            notify(range, result, hob, null, NotificationType.ERROR, null);
            break;
        case SUCCESS:
            // RECOVERY
            result = Configuration.INSTANCE.findWorkflowMatches(NotificationRange.WORKFLOW, Configuration.INSTANCE.getOnError(), hob
                    .getControllerId(), hob.getWorkflowPath());
            notify(range, result, hob, null, NotificationType.RECOVERED, null);
            // SUCCESS
            result = Configuration.INSTANCE.findWorkflowMatches(NotificationRange.WORKFLOW, Configuration.INSTANCE.getOnSuccess(), hob
                    .getControllerId(), hob.getWorkflowPath());
            notify(range, result, hob, null, NotificationType.SUCCESS, null);

            break;
        default:
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[notifyOrder][skip][%s][%s]because NotificationType=%s", range, type, type));
            }
            break;
        }
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[notifyOrder][end][%s][%s]%s", range, type, SOSString.toString(hob)));
        }
    }

    private boolean notify(NotificationRange range, List<Notification> list, HistoryOrderBean hob, HistoryOrderStepBean hosb, NotificationType type,
            List<HistoryOrderStepResultWarn> warnings) {
        if (list.size() == 0) {
            return false;
        }

        HistoryNotifyAnalyzer analyzer = new HistoryNotifyAnalyzer();
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));

            if (!analyzer.analyze(range, dbLayer, list, hob, hosb, type, warnings)) {
                return false;
            }

            dbLayer.getSession().beginTransaction();
            Map<Long, DBItemMonitoringOrderStep> steps = new HashMap<>();
            boolean notified = false;
            boolean isWarning = NotificationType.WARNING.equals(type) && warnings != null;
            for (Notification notification : list) {
                if (isWarning) {
                    for (HistoryOrderStepResultWarn warning : warnings) {
                        if (notify(range, analyzer, notification, type, steps, warning)) {
                            notified = true;
                        }
                    }
                } else {
                    if (notify(range, analyzer, notification, type, steps, null)) {
                        notified = true;
                    }
                }
            }
            dbLayer.getSession().commit();
            return notified;
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
            dbLayer.rollback();
            return false;
        } finally {
            dbLayer.close();
        }
    }

    private boolean notify(NotificationRange range, HistoryNotifyAnalyzer analyzer, Notification notification, NotificationType type,
            Map<Long, DBItemMonitoringOrderStep> steps, HistoryOrderStepResultWarn warning) throws Exception {

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        DBItemMonitoringOrderStep os = analyzer.getOrderStep();
        Long recoveredId = null;
        JobWarning warn = JobWarning.NONE;
        String warnText = null;
        switch (type) {
        case ERROR:
        case SUCCESS:
            break;
        case RECOVERED:
            if (analyzer.getToRecovery() != null) {
                LastWorkflowNotificationDBItemEntity r = analyzer.getToRecovery().get(notification.getNotificationId());
                if (r == null) {
                    return false;
                }
                recoveredId = r.getId();
                if (steps.containsKey(r.getStepId())) {
                    os = steps.get(r.getStepId());
                } else {
                    os = dbLayer.getMonitoringOrderStep(r.getStepId(), true);
                    if (os == null) {
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("%s[notification id=%s][%s][%s][skip][monitoringOrderStep not found]%s",
                                    Configuration.LOG_INTENT, notification.getNotificationId(), range, ANotifier.getTypeAsString(type), r
                                            .getStepId()));
                        }
                        return false;
                    }
                    steps.put((r.getStepId()), os);
                }
            }
            break;
        case WARNING:
            if (warning == null || warning.getReason() == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("%s[notification id=%s][%s][%s][skip]warning or warning reason is null", Configuration.LOG_INTENT,
                            notification.getNotificationId(), range, ANotifier.getTypeAsString(type)));
                }
                return false;
            }
            if (analyzer.getSentWarnings() != null && analyzer.getSentWarnings().containsKey(notification.getNotificationId())) {
                if (analyzer.getSentWarnings().get(notification.getNotificationId()).contains(warning.getReason())) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%s[notification id=%s][%s][%s %s][skip][already sent]%s", Configuration.LOG_INTENT, notification
                                .getNotificationId(), range, ANotifier.getTypeAsString(type), warning.getReason(), analyzer.getSentWarnings()));
                    }
                    return false;
                }
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s[notification id=%s][%s][%s][warning=%s]sentWarnings=%s", Configuration.LOG_INTENT, notification
                        .getNotificationId(), range, ANotifier.getTypeAsString(type), SOSString.toString(warning), analyzer.getSentWarnings()));
            }

            warn = warning.getReason();
            warnText = warning.getText();
            break;
        case ACKNOWLEDGED:
            return false;
        }

        if (notification.getMonitors().size() == 0) {
            LOGGER.info(String.format("[notification id=%s][%s][%s][store to database only]%s%s", notification.getNotificationId(), range, ANotifier
                    .getTypeAsString(type), ANotifier.getInfo(analyzer), (warnText == null ? "" : warnText)));
        } else {
            LOGGER.info(String.format("[notification id=%s][%s][%s][send to %s monitors]%s", notification.getNotificationId(), range, ANotifier
                    .getTypeAsString(type), notification.getMonitors().size(), notification.getMonitorsAsString()));
        }

        DBItemNotification mn = null;
        try {
            mn = dbLayer.saveNotification(notification, analyzer, range, type, recoveredId, warn, warnText);
        } catch (Throwable e) {
            LOGGER.error(String.format("[notification id=%s][%s][%s]%s[failed]%s", notification.getNotificationId(), range, ANotifier.getTypeAsString(
                    type), ANotifier.getInfo(analyzer), e.toString()), e);
        }
        int i = 1;
        for (AMonitor m : notification.getMonitors()) {
            ANotifier n = null;
            try {
                n = m.createNotifier(i);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);// contains all informations about the type etc
                if (mn == null) {
                    LOGGER.info(String.format("%s[%s][notification id=%s][%s]%s[skip save notification monitor]due to save notification failed",
                            Configuration.LOG_INTENT, i, notification.getNotificationId(), range, ANotifier.getInfo(analyzer, m, type)));
                } else {
                    dbLayer.saveNotificationMonitor(mn, m, e);
                }
                n = null;
            }
            if (n != null) {
                try {
                    NotifyResult nr = n.notify(type, m.getTimeZone(), analyzer.getOrder(), os, mn);
                    if (nr != null && nr.getError() != null) {
                        LOGGER.error(nr.getError().getMessage(), nr.getError().getException());
                    }
                    if (mn == null) {
                        LOGGER.info(String.format("%s[%s][notification id=%s][%s]%s[skip save notification result]due to save notification failed",
                                Configuration.LOG_INTENT, i, notification.getNotificationId(), range, ANotifier.getInfo(analyzer, m, type)));
                    } else {
                        if (nr.getSkipCause() == null) {
                            dbLayer.saveNotificationMonitor(mn, m, nr);
                        } else {
                            LOGGER.info(String.format("%s[%s][notification id=%s][%s][skip]%s%s%s", Configuration.LOG_INTENT, i, notification
                                    .getNotificationId(), range, ANotifier.getMainInfo(m), nr.getSkipCause(), ANotifier.getInfo(analyzer)));
                        }
                    }
                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                } finally {
                    n.close();
                }
            }
            i++;
        }
        postEvent(mn, analyzer);
        return true;
    }

    private void postEvent(DBItemNotification mn, HistoryNotifyAnalyzer analyzer) {
        if (mn == null || analyzer == null) {
            return;
        }
        EventBus.getInstance().post(new NotificationCreated(analyzer.getControllerId(), mn.getId()));
    }

    private void notifyStepWarning(HistoryOrderStepResult r) {
        if (Configuration.INSTANCE.getOnWarning().size() > 0 && r.getWarnings().size() > 0) {
            NotificationRange range = NotificationRange.WORKFLOW_JOB;
            HistoryOrderStepBean hosb = r.getStep();
            List<Notification> result = Configuration.INSTANCE.findWorkflowMatches(range, Configuration.INSTANCE.getOnWarning(), hosb
                    .getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), hosb.getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(range, result, null, hosb, NotificationType.WARNING, r.getWarnings());
        }
    }

    protected JocClusterAnswer close(StartupMode mode) {
        MonitorService.setLogger();
        closed.set(true);

        if (threadPool != null) {
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        }
        closeFactory();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    private JocClusterHibernateFactory createFactory(Path configFile) {
        JocClusterHibernateFactory factory = null;
        try {
            factory = new JocClusterHibernateFactory(configFile, 1, 1);
            factory.setIdentifier(IDENTIFIER);
            factory.setAutoCommit(false);
            factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            factory.addClassMapping(DBLayer.getMonitoringClassMapping());
            factory.build();
        } catch (SOSHibernateException e) {
            LOGGER.error(e.toString(), e);
        }
        return factory;
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            LOGGER.info(String.format("[%s]database factory closed", LOG_IDENTIFIER));
        }
    }

}
