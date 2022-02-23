package com.sos.joc.monitoring.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.monitoring.DBItemNotification;
import com.sos.joc.db.monitoring.DBItemNotificationAcknowledgement;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.monitoring.NotificationAcknowledgeAnswer;
import com.sos.joc.model.monitoring.NotificationAcknowledgeFilter;
import com.sos.joc.model.monitoring.NotificationItemAcknowledgementItem;
import com.sos.joc.monitoring.resource.INotificationAcknowledge;
import com.sos.monitoring.notification.NotificationType;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.MONITORING)
public class NotificationAcknowledgeImpl extends JOCResourceImpl implements INotificationAcknowledge {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, NotificationAcknowledgeFilter.class);
            NotificationAcknowledgeFilter in = Globals.objectMapper.readValue(inBytes, NotificationAcknowledgeFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermitted(accessToken, in));
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            NotificationItemAcknowledgementItem ac = new NotificationItemAcknowledgementItem();

            if (in.getNotificationIds() != null && in.getNotificationIds().size() > 0) {
                session.beginTransaction();

                Date created = new Date();
                String account = getAccount();
                for (Long notificationId : in.getNotificationIds()) {
                    DBItemNotification notification = dbLayer.getNotification(notificationId);
                    if (notification != null) {
                        DBItemNotificationAcknowledgement result = dbLayer.getNotificationAcknowledgement(notificationId);
                        if (result == null) {
                            result = new DBItemNotificationAcknowledgement();
                            result.setNotificationId(notificationId);
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

    private boolean getPermitted(String accessToken, NotificationAcknowledgeFilter in) {
        String controllerId = in.getControllerId();
        Set<String> allowedControllers = Collections.emptySet();
        boolean permitted = false;
        if (controllerId == null || controllerId.isEmpty()) {
            controllerId = "";
            allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                    availableController, accessToken).getOrders().getModify()).collect(Collectors.toSet());
            permitted = !allowedControllers.isEmpty();
            if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                allowedControllers = Collections.emptySet();
            }
        } else {
            allowedControllers = Collections.singleton(controllerId);
            permitted = getControllerPermissions(controllerId, accessToken).getOrders().getModify();
        }
        return permitted;
    }
}
