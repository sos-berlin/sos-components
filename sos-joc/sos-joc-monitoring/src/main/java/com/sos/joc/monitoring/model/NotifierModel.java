package com.sos.joc.monitoring.model;

import java.util.List;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.exception.SOSNotifierSendException;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.joc.monitoring.notification.notifier.ANotifier.ServiceMessagePrefix;
import com.sos.joc.monitoring.notification.notifier.ANotifier.ServiceStatus;

public class NotifierModel {

    private static final String IDENTIFIER = "notifier";
    
    private final SOSHibernateFactory factory;
    private final DBLayerMonitoring dbLayer;

    protected NotifierModel(SOSHibernateFactory factory, String serviceIdentifier) {
        this.factory = factory;
        this.dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
    }

    protected void notify(Configuration conf, HistoryOrderStepBean hosb) {
        if (!conf.exists()) {
            return;
        }

        List<Notification> result;
        if (hosb.getError()) {
            if (conf.getTypeOnError().size() == 0) {
                return;
            }
            result = conf.findMatchesAtEnd(conf.getTypeOnError(), hosb.getControllerId(), hosb.getWorkflowName(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());

            notify(conf, result, hosb, ServiceStatus.CRITICAL, ServiceMessagePrefix.ERROR);
        } else {
            result = conf.findMatchesAtEnd(conf.getTypeOnSuccess(), hosb.getControllerId(), hosb.getWorkflowName(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            // TODO check for RECOVERY ????
        }

    }

    private void notify(Configuration conf, List<Notification> list, HistoryOrderStepBean hosb, ServiceStatus status, ServiceMessagePrefix prefix) {
        if (list.size() == 0) {
            return;
        }

        DBItemMonitoringOrder mo;
        DBItemMonitoringOrderStep mos;
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();

            mo = dbLayer.getMonitoringOrder(hosb.getHistoryOrderId());
            mos = dbLayer.getMonitoringOrderStep(hosb.getHistoryId());
            dbLayer.getSession().commit();
        } catch (Throwable e) {
            dbLayer.rollback();
            return;
        }

        for (Notification notification : list) {
            for (AMonitor m : notification.getMonitors()) {
                ANotifier n = m.createNotifier(conf);
                if (n != null) {
                    try {
                        n.notify(mo, mos, status, prefix);
                    } catch (SOSNotifierSendException e) {

                    } finally {
                        n.close();
                    }
                }
            }
        }
    }

}
