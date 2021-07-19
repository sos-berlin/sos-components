package com.sos.joc.monitoring.model;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
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
import com.sos.joc.monitoring.model.HistoryMonitoringModel.HistoryOrderStepResult;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.ToNotify;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.joc.monitoring.notification.notifier.NotifyResult;
import com.sos.monitoring.notification.NotificationType;

public class NotifierModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierModel.class);

    private static final String IDENTIFIER = "notification";

    private final SOSHibernateFactory factory;
    private DBLayerMonitoring dbLayer;
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

    protected void notify(Configuration conf, ToNotify toNotifyPayloads, ToNotify toNotifyLongerThan) {
        if (!conf.exists() || factory == null) {
            return;
        }

        Runnable task = new Runnable() {

            @Override
            public void run() {
                AJocClusterService.setLogger(serviceIdentifier);
                notifySteps(conf, toNotifyPayloads.getSteps());
                notifyOrders(conf, toNotifyPayloads.getErrorOrders(), toNotifyPayloads.getSuccessOrders());
                notifyStepsLongerThan(conf, toNotifyLongerThan.getSteps());
            }
        };
        threadPool.submit(task);
    }

    private void notifySteps(Configuration conf, List<HistoryOrderStepResult> steps) {
        for (HistoryOrderStepResult step : steps) {
            notifyStep(conf, step);
        }
    }

    private void notifyStepsLongerThan(Configuration conf, List<HistoryOrderStepResult> steps) {
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
            notify(conf, result, null, hosb, NotificationType.ERROR);
        } else {
            // RECOVERY
            result = conf.findWorkflowMatches(conf.getOnError(), hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, NotificationType.RECOVERED);
            // SUCCESS
            result = conf.findWorkflowMatches(conf.getOnSuccess(), hosb.getControllerId(), hosb.getWorkflowPath(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, NotificationType.SUCCESS);

        }
    }

    private void notifyOrder(Configuration conf, HistoryOrderBean hob, NotificationType type) {
        List<Notification> result;
        switch (type) {
        case ERROR:
            result = conf.findWorkflowMatches(conf.getOnError(), hob.getControllerId(), hob.getWorkflowPath());
            notify(conf, result, hob, null, NotificationType.ERROR);
            break;
        case SUCCESS:
            // RECOVERY
            result = conf.findWorkflowMatches(conf.getOnError(), hob.getControllerId(), hob.getWorkflowPath());
            notify(conf, result, hob, null, NotificationType.RECOVERED);
            // SUCCESS
            result = conf.findWorkflowMatches(conf.getOnSuccess(), hob.getControllerId(), hob.getWorkflowPath());
            notify(conf, result, hob, null, NotificationType.SUCCESS);

            break;
        default:
            break;
        }
    }

    private boolean notify(Configuration conf, List<Notification> list, HistoryOrderBean hob, HistoryOrderStepBean hosb, NotificationType type) {
        if (list.size() == 0) {
            return false;
        }

        NotifyAnalyzer analyzer = new NotifyAnalyzer();
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();

            if (!analyzer.analyze(dbLayer, list, hob, hosb, type)) {
                return false;
            }

            Map<Long, DBItemMonitoringOrderStep> steps = new HashMap<>();
            boolean notified = false;
            for (Notification notification : list) {
                if (notify(conf, analyzer, notification, type, steps)) {
                    notified = true;
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
            Map<Long, DBItemMonitoringOrderStep> steps) throws Exception {

        DBItemMonitoringOrderStep os = analyzer.getOrderStep();
        Long recoveredId = null;
        switch (type) {
        case ERROR:
        case SUCCESS:
            break;
        case RECOVERED:
            if (analyzer.getToRecovery() != null) {
                DBItemNotification r = analyzer.getToRecovery().get(notification.getName());
                if (r == null) {
                    return false;
                }
                recoveredId = r.getId();
                if (steps.containsKey(r.getStepId())) {
                    os = steps.get(r.getStepId());
                } else {
                    os = dbLayer.getMonitoringOrderStep(r.getStepId(), true);
                    if (os == null) {
                        return false;
                    }
                    steps.put((r.getStepId()), os);
                }
            }
            break;
        case WARNING:
            if (type.equals(NotificationType.WARNING)) {
                if (analyzer.getSendedWarnings() != null && analyzer.getSendedWarnings().contains(notification.getName())) {
                    return false;
                }
            }
            break;
        }

        DBItemNotification mn = dbLayer.saveNotification(notification, analyzer, type, recoveredId);
        if (mn == null) {
            LOGGER.error(String.format("[save new notification failed][%s][%s][orderId=%s][stepId=%s][workflowPosition=%s][recoveredId=%s]", type
                    .value(), analyzer.getRange().value(), analyzer.getOrderId(), analyzer.getStepId(), analyzer.getWorkflowPosition(), recoveredId));
        }

        for (AMonitor m : notification.getMonitors()) {
            ANotifier n = m.createNotifier(conf);
            if (n != null) {
                try {
                    NotifyResult nr = n.notify(type, analyzer.getOrder(), os, mn);
                    if (mn == null) {
                        LOGGER.info(String.format("[skip save monitor type=%s name=%s]due to save new notification failed", m.getType().value(), m
                                .getMonitorName()));
                    } else {
                        dbLayer.saveNotificationMonitor(mn, m, nr);
                    }
                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                } finally {
                    n.close();
                }
            }
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
        if (conf.getOnWarning().size() > 0 && r.getWarn() != null) {
            HistoryOrderStepBean hosb = r.getStep();
            List<Notification> result = conf.findWorkflowMatches(conf.getOnWarning(), hosb.getControllerId(), hosb.getWorkflowPath(), hosb
                    .getJobName(), hosb.getJobLabel(), hosb.getCriticality(), hosb.getReturnCode(), true);
            notify(conf, result, null, hosb, NotificationType.WARNING);
        }
    }

    protected JocClusterAnswer close(StartupMode mode) {
        AJocClusterService.setLogger(serviceIdentifier);

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
