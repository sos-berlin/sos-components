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
import com.sos.joc.cluster.AJocClusterService;
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
import com.sos.monitoring.notification.NotificationType;

public class NotifierModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierModel.class);

    private static final String IDENTIFIER = "notification";

    private final SOSHibernateFactory factory;
    private DBLayerMonitoring dbLayer;
    private AtomicBoolean closed = new AtomicBoolean();
    private String serviceIdentifier;

    private ExecutorService threadPool;

    protected NotifierModel(ThreadGroup threadGroup, Path hibernateConfigFile, String serviceIdentifier) {
        AJocClusterService.setLogger(serviceIdentifier);

        factory = createFactory(hibernateConfigFile);
        if (factory == null) {
            LOGGER.info(String.format("[%s][skip]due to the database factory errors", IDENTIFIER));
        } else {
            this.serviceIdentifier = serviceIdentifier;
            dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(threadGroup, IDENTIFIER));
        }
    }

    protected void notify(Configuration conf, ToNotify toNotifyPayloads, ToNotify toNotifyExtraStepsWarnings) {
        if (!conf.exists() || factory == null) {
            return;
        }

        Runnable task = new Runnable() {

            @Override
            public void run() {
                AJocClusterService.setLogger(serviceIdentifier);
                if (!closed.get()) {
                    notifySteps(conf, toNotifyPayloads.getSteps());
                    notifyOrders(conf, toNotifyPayloads.getErrorOrders(), toNotifyPayloads.getSuccessOrders());
                    notifyStepsWarnings(conf, toNotifyExtraStepsWarnings.getSteps());
                }
            }
        };
        if (!closed.get()) {
            threadPool.submit(task);
        }
    }

    private void notifySteps(Configuration conf, List<HistoryOrderStepResult> steps) {
        for (HistoryOrderStepResult step : steps) {
            notifyStep(conf, step);
        }
    }

    private void notifyStepsWarnings(Configuration conf, List<HistoryOrderStepResult> steps) {
        for (HistoryOrderStepResult step : steps) {
            notifyStepWarning(conf, step);
        }
    }

    private void notifyOrders(Configuration conf, List<HistoryOrderBean> error, List<HistoryOrderBean> success) {
        for (HistoryOrderBean order : error) {
            notifyOrder(conf, order, NotificationType.ERROR);
        }
        for (HistoryOrderBean order : success) {
            notifyOrder(conf, order, NotificationType.SUCCESS);
        }
    }

    private void notifyStep(Configuration conf, HistoryOrderStepResult r) {
        List<Notification> result;
        HistoryOrderStepBean hosb = r.getStep();

        notifyStepWarning(conf, r);

        if (hosb.getError()) {
            result = conf.findWorkflowMatches(conf.getOnError(), hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, NotificationType.ERROR, null);
        } else {
            // RECOVERY
            result = conf.findWorkflowMatches(conf.getOnError(), hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, NotificationType.RECOVERED, null);
            // SUCCESS
            result = conf.findWorkflowMatches(conf.getOnSuccess(), hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, NotificationType.SUCCESS, null);

        }
    }

    private void notifyOrder(Configuration conf, HistoryOrderBean hob, NotificationType type) {
        List<Notification> result;
        switch (type) {
        case ERROR:
            result = conf.findWorkflowMatches(conf.getOnError(), hob.getControllerId(), hob.getWorkflowPath());
            notify(conf, result, hob, null, NotificationType.ERROR, null);
            break;
        case SUCCESS:
            // RECOVERY
            result = conf.findWorkflowMatches(conf.getOnError(), hob.getControllerId(), hob.getWorkflowPath());
            notify(conf, result, hob, null, NotificationType.RECOVERED, null);
            // SUCCESS
            result = conf.findWorkflowMatches(conf.getOnSuccess(), hob.getControllerId(), hob.getWorkflowPath());
            notify(conf, result, hob, null, NotificationType.SUCCESS, null);

            break;
        default:
            break;
        }
    }

    private boolean notify(Configuration conf, List<Notification> list, HistoryOrderBean hob, HistoryOrderStepBean hosb, NotificationType type,
            List<HistoryOrderStepResultWarn> warnings) {
        if (list.size() == 0) {
            return false;
        }

        NotifyAnalyzer analyzer = new NotifyAnalyzer();
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();

            if (!analyzer.analyze(dbLayer, list, hob, hosb, type, warnings)) {
                return false;
            }

            Map<Long, DBItemMonitoringOrderStep> steps = new HashMap<>();
            boolean notified = false;
            boolean isWarning = NotificationType.WARNING.equals(type) && warnings != null;
            for (Notification notification : list) {
                if (isWarning) {
                    for (HistoryOrderStepResultWarn warning : warnings) {
                        if (notify(conf, analyzer, notification, type, steps, warning)) {
                            notified = true;
                        }
                    }
                } else {
                    if (notify(conf, analyzer, notification, type, steps, null)) {
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

    private boolean notify(Configuration conf, NotifyAnalyzer analyzer, Notification notification, NotificationType type,
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
                            LOGGER.debug(String.format("[notification id=%s][%s][skip][monitoringOrderStep not found]%s", notification
                                    .getNotificationId(), ANotifier.getTypeAsString(type), r.getStepId()));
                        }
                        return false;
                    }
                    steps.put((r.getStepId()), os);
                }
            }
            break;
        case WARNING:
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[notification id=%s][%s][warning=%s]sentWarnings=%s", notification.getNotificationId(), ANotifier
                        .getTypeAsString(type), SOSString.toString(warning), analyzer.getSentWarnings()));
            }

            if (warning == null || warning.getReason() == null) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[notification id=%s][%s][skip]warning or warning reason is null", notification.getNotificationId(),
                            ANotifier.getTypeAsString(type)));
                }
                return false;
            }
            if (analyzer.getSentWarnings() != null && analyzer.getSentWarnings().containsKey(notification.getNotificationId())) {
                if (analyzer.getSentWarnings().get(notification.getNotificationId()).contains(warning.getReason())) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[notification id=%s][%s %s][skip][already sent]%s", notification.getNotificationId(), ANotifier
                                .getTypeAsString(type), warning.getReason(), analyzer.getSentWarnings()));
                    }
                    return false;
                }
            }

            warn = warning.getReason();
            warnText = warning.getText();
            break;
        case ACKNOWLEDGED:
            return false;
        }

        if (notification.getMonitors().size() == 0) {
            LOGGER.info(String.format("[notification id=%s][%s][store to database only]%s%s", notification.getNotificationId(), ANotifier
                    .getTypeAsString(type), ANotifier.getInfo(analyzer), (warnText == null ? "" : warnText)));
        } else {
            LOGGER.info(String.format("[notification id=%s][%s][send to %s monitors]%s", notification.getNotificationId(), ANotifier.getTypeAsString(
                    type), notification.getMonitors().size(), notification.getMonitorsAsString()));
        }

        DBItemNotification mn = null;
        try {
            mn = dbLayer.saveNotification(notification, analyzer, type, recoveredId, warn, warnText);
        } catch (Throwable e) {
            LOGGER.error(String.format("[notification id=%s][%s]%s[failed]%s", notification.getNotificationId(), ANotifier.getTypeAsString(type),
                    ANotifier.getInfo(analyzer), e.toString()), e);
        }
        int i = 1;
        for (AMonitor m : notification.getMonitors()) {
            ANotifier n = null;
            try {
                n = m.createNotifier(i, conf);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);// contains all informations about the type etc
                if (mn == null) {
                    LOGGER.info(String.format("[%s]%s[skip save notification monitor]due to save notification failed", i, ANotifier.getInfo(analyzer,
                            m, type)));
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
                        LOGGER.info(String.format("[%s]%s[skip save notification result]due to save notification failed", i, ANotifier.getInfo(
                                analyzer, m, type)));
                    } else {
                        if (nr.getSkipCause() == null) {
                            dbLayer.saveNotificationMonitor(mn, m, nr);
                        } else {
                            LOGGER.info(String.format("[%s][skip]%s%s%s", i, ANotifier.getMainInfo(m), nr.getSkipCause(), ANotifier.getInfo(
                                    analyzer)));
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

    private void postEvent(DBItemNotification mn, NotifyAnalyzer analyzer) {
        if (mn == null || analyzer == null) {
            return;
        }
        EventBus.getInstance().post(new NotificationCreated(analyzer.getControllerId(), mn.getId()));
    }

    private void notifyStepWarning(Configuration conf, HistoryOrderStepResult r) {
        if (conf.getOnWarning().size() > 0 && r.getWarnings().size() > 0) {
            HistoryOrderStepBean hosb = r.getStep();
            List<Notification> result = conf.findWorkflowMatches(conf.getOnWarning(), hosb.getControllerId(), hosb.getWorkflowPath(), hosb
                    .getJobName(), hosb.getJobLabel(), hosb.getCriticality(), hosb.getReturnCode(), true);
            notify(conf, result, null, hosb, NotificationType.WARNING, r.getWarnings());
        }
    }

    protected JocClusterAnswer close(StartupMode mode) {
        AJocClusterService.setLogger(serviceIdentifier);
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
            AJocClusterService.setLogger(serviceIdentifier);
            LOGGER.error(e.toString(), e);
        }
        return factory;
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            LOGGER.info(String.format("[%s]database factory closed", IDENTIFIER));
        }
    }

}
