package com.sos.joc.monitoring.model;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
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
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.HistoryOrderStepResult;
import com.sos.joc.monitoring.model.HistoryMonitoringModel.ToNotify;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.joc.monitoring.notification.notifier.ANotifier.Status;

public class NotifierModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierModel.class);

    private static final String IDENTIFIER = "notification";

    private final SOSHibernateFactory factory;
    private DBLayerMonitoring dbLayer;
    private String serviceIdentifier;

    private ExecutorService threadPool;
    // tmp solution
    private CopyOnWriteArraySet<Long> notifiedLongerThan;

    protected NotifierModel(ThreadGroup threadGroup, Path hivernateConfigFile, String serviceIdentifier) {
        AJocClusterService.setLogger(serviceIdentifier);

        this.factory = createFactory(hivernateConfigFile);
        if (this.factory == null) {
            LOGGER.info(String.format("[%s][skip]due to the database factory errors", IDENTIFIER));

        } else {
            this.dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
            this.serviceIdentifier = serviceIdentifier;

            // notifySteps,notifyOrders,notifyStepsLongerThan
            this.threadPool = Executors.newFixedThreadPool(3, new JocClusterThreadFactory(threadGroup, IDENTIFIER));

            notifiedLongerThan = new CopyOnWriteArraySet<Long>();
        }

    }

    protected void notify(Configuration conf, ToNotify toNotifyPayloads, ToNotify toNotifyLongerThan) {
        if (!conf.exists() || factory == null) {
            return;
        }

        // TMP solution
        if (notifiedLongerThan != null) {
            for (HistoryOrderStepResult step : toNotifyLongerThan.getSteps()) {
                if (!notifiedLongerThan.contains(step.getStep().getHistoryId())) {
                    notifiedLongerThan.add(step.getStep().getHistoryId());
                }
            }
        }

        Runnable task = new Runnable() {

            @Override
            public void run() {
                AJocClusterService.setLogger(serviceIdentifier);
                notifySteps(conf, toNotifyPayloads.getSteps());
            }
        };
        threadPool.submit(task);

        task = new Runnable() {

            @Override
            public void run() {
                AJocClusterService.setLogger(serviceIdentifier);
                notifyOrders(conf, toNotifyPayloads.getErrorOrders(), toNotifyPayloads.getSuccessOrders());
            }
        };
        threadPool.submit(task);

        task = new Runnable() {

            @Override
            public void run() {
                AJocClusterService.setLogger(serviceIdentifier);
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
            notifyOrder(conf, order, Status.ERROR);
        }
        for (HistoryOrderBean order : success) {
            notifyOrder(conf, order, Status.SUCCESS);
        }
    }

    private void notifyStep(Configuration conf, HistoryOrderStepResult r) {
        List<Notification> result;
        HistoryOrderStepBean hosb = r.getStep();

        if (notifiedLongerThan != null) {
            Optional<Long> rr = notifiedLongerThan.stream().filter(e -> e.equals(r.getStep().getHistoryId())).findAny();
            if (rr.isPresent()) {
                notifiedLongerThan.remove(rr.get());
            } else {
                notifyStepWarning(conf, r);
            }
        } else {
            notifyStepWarning(conf, r);
        }

        if (hosb.getError()) {
            if (conf.getOnError().size() == 0) {
                return;
            }
            result = conf.findWorkflowMatches(conf.getOnError(), hosb.getControllerId(), hosb.getWorkflowName(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, Status.ERROR);
        } else {
            result = conf.findWorkflowMatches(conf.getOnSuccess(), hosb.getControllerId(), hosb.getWorkflowName(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, Status.SUCCESS);

            // TODO check for RECOVERY ????
            result = conf.findWorkflowMatches(conf.getOnError(), hosb.getControllerId(), hosb.getWorkflowName(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            if (result.size() > 0) {
                // check if was send to the same w, order, job position
            }
        }

    }

    private void notifyStepWarning(Configuration conf, HistoryOrderStepResult r) {
        if (conf.getOnWarning().size() > 0 && r.getWarn() != null) {
            HistoryOrderStepBean hosb = r.getStep();
            List<Notification> result = conf.findWorkflowMatches(conf.getOnWarning(), hosb.getControllerId(), hosb.getWorkflowName(), hosb
                    .getJobName(), hosb.getJobLabel(), hosb.getCriticality(), hosb.getReturnCode(), true);
            notify(conf, result, null, hosb, Status.WARNING);
        }
    }

    private void notifyOrder(Configuration conf, HistoryOrderBean hob, Status status) {
        List<Notification> result;
        switch (status) {
        case ERROR:
            if (conf.getOnError().size() == 0) {
                return;
            }
            result = conf.findWorkflowMatches(conf.getOnError(), hob.getControllerId(), hob.getWorkflowName());
            notify(conf, result, hob, null, Status.ERROR);
            break;
        case SUCCESS:
            // see above notify steps
            result = conf.findWorkflowMatches(conf.getOnSuccess(), hob.getControllerId(), hob.getWorkflowName());
            notify(conf, result, hob, null, Status.SUCCESS);

            // TODO check for RECOVERY ????
            result = conf.findWorkflowMatches(conf.getOnError(), hob.getControllerId(), hob.getWorkflowName());
            if (result.size() > 0) {
                // check if was send to the same w, order, job position
            }
            break;
        default:
            break;
        }
    }

    private void notify(Configuration conf, List<Notification> list, HistoryOrderBean hob, HistoryOrderStepBean hosb, Status status) {
        if (list.size() == 0) {
            return;
        }

        DBItemMonitoringOrder mo;
        DBItemMonitoringOrderStep mos;
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            if (hob == null) {
                mo = dbLayer.getMonitoringOrder(hosb.getHistoryOrderId());
                mos = dbLayer.getMonitoringOrderStep(hosb.getHistoryId());
            } else {
                mo = dbLayer.getMonitoringOrder(hob.getHistoryId());
                mos = dbLayer.getMonitoringOrderStep(hob.getCurrentHistoryOrderStepId());
            }
            dbLayer.getSession().commit();
        } catch (Throwable e) {
            dbLayer.rollback();
            return;
        } finally {
            dbLayer.close();
        }

        switch (status) {
        case WARNING:
            if (mos == null || mos.getWarnAsEnum().equals(JobWarning.NONE)) {
                return;
            }
            break;
        default:
            break;
        }

        for (Notification notification : list) {
            for (AMonitor m : notification.getMonitors()) {
                ANotifier n = m.createNotifier(conf);
                if (n != null) {
                    try {
                        n.notify(mo, mos, status);
                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    } finally {
                        n.close();
                    }
                }
            }
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
            factory = new JocClusterHibernateFactory(configFile, 1, 3);
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
