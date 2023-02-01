package com.sos.joc.monitoring.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.db.monitoring.DBItemNotificationMonitor;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.MonitoringMonitorTypeText;
import com.sos.joc.model.monitoring.notification.common.MonitorItem;
import com.sos.joc.model.monitoring.notification.common.NotificationAnswer;
import com.sos.joc.model.monitoring.notification.system.SystemNotificationFilter;
import com.sos.joc.monitoring.resource.ISystemNotification;
import com.sos.monitoring.MonitorType;
import com.sos.monitoring.notification.NotificationApplication;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.MONITORING)
public class SystemNotificationImpl extends JOCResourceImpl implements ISystemNotification {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, SystemNotificationFilter.class);
            SystemNotificationFilter in = Globals.objectMapper.readValue(inBytes, SystemNotificationFilter.class);

            // 1) notification view permitted
            if (!getJocPermissions(accessToken).getNotification().getView()) {
                return initPermissions(null, false);
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);

            List<MonitorItem> monitors = new ArrayList<MonitorItem>();
            List<DBItemNotificationMonitor> result = dbLayer.getNotificationMonitors(NotificationApplication.SYSTEM_NOTIFICATION, in
                    .getNotificationId());
            if (result != null) {
                for (DBItemNotificationMonitor monitor : result) {
                    monitors.add(convert(monitor));
                }
            }

            NotificationAnswer answer = new NotificationAnswer();
            answer.setDeliveryDate(new Date());
            answer.setMonitors(monitors);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private MonitorItem convert(DBItemNotificationMonitor entity) {
        MonitorItem item = new MonitorItem();
        item.setType(getType(entity.getType()));
        item.setName(entity.getName());
        item.setConfiguration(entity.getConfiguration());
        item.setMessage(entity.getMessage());
        item.setError(entity.getErrorText());
        item.setCreated(entity.getCreated());

        return item;
    }

    private MonitoringMonitorTypeText getType(Integer type) {
        try {
            return MonitoringMonitorTypeText.fromValue(MonitorType.fromValue(type).value());
        } catch (Throwable e) {
            return MonitoringMonitorTypeText.COMMAND;
        }
    }
}
