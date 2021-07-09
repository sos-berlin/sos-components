package com.sos.joc.monitoring.model;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.joc.cluster.bean.history.HistoryOrderBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.monitoring.configuration.Configuration;
import com.sos.joc.monitoring.configuration.Notification;
import com.sos.joc.monitoring.configuration.monitor.AMonitor;
import com.sos.joc.monitoring.db.DBLayerMonitoring;
import com.sos.joc.monitoring.notification.notifier.ANotifier;
import com.sos.joc.monitoring.notification.notifier.ANotifier.Status;

public class NotifierModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotifierModel.class);

    private static final String IDENTIFIER = "notificationr";

    private final SOSHibernateFactory factory;
    private final DBLayerMonitoring dbLayer;

    protected NotifierModel(SOSHibernateFactory factory, String serviceIdentifier) {
        this.factory = factory;
        this.dbLayer = new DBLayerMonitoring(serviceIdentifier + "_" + IDENTIFIER, serviceIdentifier);
    }

    protected void notify(Configuration conf, HistoryOrderStepBean hosb) {
        List<Notification> result;
        if (hosb.getError()) {
            if (conf.getTypeOnError().size() == 0) {
                return;
            }
            result = conf.findWorkflowMatches(conf.getTypeOnError(), hosb.getControllerId(), hosb.getWorkflowName(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, Status.ERROR);
        } else {
            result = conf.findWorkflowMatches(conf.getTypeOnSuccess(), hosb.getControllerId(), hosb.getWorkflowName(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            notify(conf, result, null, hosb, Status.SUCCESS);

            // TODO check for RECOVERY ????
            result = conf.findWorkflowMatches(conf.getTypeOnError(), hosb.getControllerId(), hosb.getWorkflowName(), hosb.getJobName(), hosb
                    .getJobLabel(), hosb.getCriticality(), hosb.getReturnCode());
            if (result.size() > 0) {
                // check if was send to the same w, order, job position
            }
        }
    }

    protected void notify(Configuration conf, HistoryOrderBean hob, Status status) {
        List<Notification> result;
        switch (status) {
        case ERROR:
            if (conf.getTypeOnError().size() == 0) {
                return;
            }
            result = conf.findWorkflowMatches(conf.getTypeOnError(), hob.getControllerId(), hob.getWorkflowName());
            notify(conf, result, hob, null, Status.ERROR);
            break;
        case SUCCESS:
            // see above notify steps
            result = conf.findWorkflowMatches(conf.getTypeOnSuccess(), hob.getControllerId(), hob.getWorkflowName());
            notify(conf, result, hob, null, Status.SUCCESS);

            // TODO check for RECOVERY ????
            result = conf.findWorkflowMatches(conf.getTypeOnError(), hob.getControllerId(), hob.getWorkflowName());
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
            // dbLayer.getSession().beginTransaction();
            if (hob == null) {
                mo = dbLayer.getMonitoringOrder(hosb.getHistoryOrderId());
                mos = dbLayer.getMonitoringOrderStep(hosb.getHistoryId());
            } else {
                mo = dbLayer.getMonitoringOrder(hob.getHistoryId());
                mos = dbLayer.getMonitoringOrderStep(hob.getCurrentHistoryOrderStepId());
            }
            // dbLayer.getSession().commit();
        } catch (Throwable e) {
            // dbLayer.rollback();
            return;
        } finally {
            dbLayer.close();
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

}
