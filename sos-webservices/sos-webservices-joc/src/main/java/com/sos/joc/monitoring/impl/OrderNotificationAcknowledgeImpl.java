package com.sos.joc.monitoring.impl;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.db.monitoring.DBItemNotificationAcknowledgement;
import com.sos.joc.db.monitoring.DBItemNotificationAcknowledgementId;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.monitoring.notification.common.AcknowledgementItem;
import com.sos.joc.model.monitoring.notification.common.NotificationAcknowledgeAnswer;
import com.sos.joc.model.monitoring.notification.order.OrderNotificationAcknowledgeFilter;
import com.sos.joc.monitoring.resource.IOrderNotificationAcknowledge;
import com.sos.monitoring.notification.NotificationApplication;
import com.sos.monitoring.notification.NotificationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.MONITORING)
public class OrderNotificationAcknowledgeImpl extends JOCResourceImpl implements IOrderNotificationAcknowledge {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            inBytes = initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, OrderNotificationAcknowledgeFilter.class);
            OrderNotificationAcknowledgeFilter in = Globals.objectMapper.readValue(inBytes, OrderNotificationAcknowledgeFilter.class);

            storeAuditLog(in.getAuditLog(), CategoryType.MONITORING);

//            // 1) notification view changes permitted
//            if (!getJocPermissions_(accessToken).getNotification().getManage()) {
//                return initPermissions(in.getControllerId(), false);
//            }
//            // 2) controller permitted (because of controller related monitoring entries)
//            if (!getBasicControllerPermissions(in.getControllerId(), accessToken).getOrders().getView()) {
//                return initPermissions(in.getControllerId(), false);
//            }
            
            boolean perm = getBasicJocPermissions(accessToken).getNotification().getManage() && getBasicControllerPermissions(in.getControllerId(),
                    accessToken).getOrders().getView();
            boolean fourEyesPerm = get4EyesJocPermissions().getNotification().getManage();
            JOCDefaultResponse response = initPermissions(in.getControllerId(), perm, fourEyesPerm);
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            AcknowledgementItem ac = new AcknowledgementItem();

            if (in.getNotificationIds() != null && in.getNotificationIds().size() > 0) {
                session.beginTransaction();

                Date created = new Date();
                String account = getAccount();
                for (Long notificationId : in.getNotificationIds()) {
                    // TODO check controllerId
                    DBItemNotification notification = dbLayer.getOrderNotification(notificationId);
                    if (notification != null) {
                        DBItemNotificationAcknowledgement result = dbLayer.getNotificationAcknowledgement(NotificationApplication.ORDER_NOTIFICATION,
                                notificationId);
                        if (result == null) {
                            result = new DBItemNotificationAcknowledgement();
                            result.setId(new DBItemNotificationAcknowledgementId(notificationId, NotificationApplication.ORDER_NOTIFICATION
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
