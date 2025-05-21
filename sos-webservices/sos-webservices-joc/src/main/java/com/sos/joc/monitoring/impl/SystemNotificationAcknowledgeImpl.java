package com.sos.joc.monitoring.impl;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.db.monitoring.DBItemNotificationAcknowledgement;
import com.sos.joc.db.monitoring.DBItemNotificationAcknowledgementId;
import com.sos.joc.db.monitoring.DBItemSystemNotification;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.monitoring.notification.common.AcknowledgementItem;
import com.sos.joc.model.monitoring.notification.common.NotificationAcknowledgeAnswer;
import com.sos.joc.model.monitoring.notification.system.SystemNotificationAcknowledgeFilter;
import com.sos.joc.monitoring.resource.ISystemNotificationAcknowledge;
import com.sos.monitoring.notification.NotificationApplication;
import com.sos.monitoring.notification.NotificationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.MONITORING)
public class SystemNotificationAcknowledgeImpl extends JOCResourceImpl implements ISystemNotificationAcknowledge {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken, CategoryType.MONITORING);
            JsonValidator.validateFailFast(inBytes, SystemNotificationAcknowledgeFilter.class);
            SystemNotificationAcknowledgeFilter in = Globals.objectMapper.readValue(inBytes, SystemNotificationAcknowledgeFilter.class);

            // 1) notification view changes permitted
            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).map(p -> p.getNotification().getManage()));
            if (response != null) {
                return response;
            }

            storeAuditLog(in.getAuditLog());

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            AcknowledgementItem ac = new AcknowledgementItem();

            if (in.getNotificationIds() != null && in.getNotificationIds().size() > 0) {
                session.beginTransaction();

                Date created = new Date();
                String account = getAccount();
                for (Long notificationId : in.getNotificationIds()) {
                    DBItemSystemNotification notification = dbLayer.getSystemNotification(notificationId);
                    if (notification != null) {
                        DBItemNotificationAcknowledgement result = dbLayer.getNotificationAcknowledgement(NotificationApplication.SYSTEM_NOTIFICATION,
                                notificationId);
                        if (result == null) {
                            result = new DBItemNotificationAcknowledgement();
                            result.setId(new DBItemNotificationAcknowledgementId(notificationId, NotificationApplication.SYSTEM_NOTIFICATION
                                    .intValue()));
                            result.setAccount(account);
                            result.setComment(in.getComment());
                            result.setCreated(created);
                            session.save(result);

                            notification.setType(NotificationType.ACKNOWLEDGED);
                            session.update(notification);
                        }

                        ac.setAccount(result.getAccount());
                        ac.setComment(result.getComment());
                        ac.setCreated(result.getCreated());
                    }
                }

                session.commit();
            }

            NotificationAcknowledgeAnswer answer = new NotificationAcknowledgeAnswer();
            answer.setDeliveryDate(new Date());
            answer.setAcknowledgement(ac);
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            Globals.rollback(session);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(session);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
}
