package com.sos.joc.monitoring.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.ScrollableResults;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.db.monitoring.SystemNotificationDBItemEntity;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.monitoring.NotificationItemAcknowledgementItem;
import com.sos.joc.model.monitoring.NotificationsFilter;
import com.sos.joc.model.monitoring.SystemNotificationItem;
import com.sos.joc.model.monitoring.SystemNotificationsAnswer;
import com.sos.joc.monitoring.resource.ISystemNotifications;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.SystemNotificationCategory;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.MONITORING)
public class SystemNotificationsImpl extends JOCResourceImpl implements ISystemNotifications {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, NotificationsFilter.class);
            NotificationsFilter in = Globals.objectMapper.readValue(inBytes, NotificationsFilter.class);

            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getOrders().getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
            }

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }
            if (in.getLimit() == null) {
                in.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
            }

            List<Integer> types = getTypes(in);
            if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                allowedControllers = Collections.emptySet();
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            List<SystemNotificationItem> notifications = new ArrayList<>();
            ScrollableResults sr = null;
            try {
                if (in.getNotificationIds() == null || in.getNotificationIds().size() == 0) {
                    sr = dbLayer.getSystemNotifications(JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()), types, in.getLimit());
                } else {
                    sr = dbLayer.getSystemNotifications(in.getNotificationIds(), types);
                }
                while (sr.next()) {
                    notifications.add(convert((SystemNotificationDBItemEntity) sr.get(0)));
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (sr != null) {
                    sr.close();
                }
            }

            SystemNotificationsAnswer answer = new SystemNotificationsAnswer();
            answer.setDeliveryDate(new Date());
            answer.setNotifications(notifications);
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

    private List<Integer> getTypes(NotificationsFilter in) {
        if (in.getTypes() == null || in.getTypes().size() == 0) {
            return null;
        }
        return in.getTypes().stream().map(e -> {
            try {
                return NotificationType.fromValue(e.value()).intValue();
            } catch (Throwable ex) {
                return NotificationType.ERROR.intValue();
            }
        }).collect(Collectors.toList());
    }

    private SystemNotificationItem convert(SystemNotificationDBItemEntity entity) {
        SystemNotificationItem item = new SystemNotificationItem();
        item.setNotificationId(entity.getId());
        item.setType(getType(entity.getType()));
        item.setCategory(getCategory(entity.getCategory()));
        item.setHasMonitors(entity.getHasMonitors());
        item.setSection(entity.getSection());
        item.setNotifier(entity.getNotifier());
        item.setTime(entity.getTime());
        item.setMessage(entity.getMessage());
        item.setException(entity.getException());
        item.setCreated(entity.getCreated());

        if (!SOSString.isEmpty(entity.getAcknowledgementAccount())) {
            NotificationItemAcknowledgementItem ac = new NotificationItemAcknowledgementItem();
            ac.setAccount(entity.getAcknowledgementAccount());
            ac.setComment(entity.getAcknowledgementComment());
            ac.setCreated(entity.getAcknowledgementCreated());
            item.setAcknowledgement(ac);
        }
        return item;
    }

    private NotificationType getType(Integer type) {
        try {
            return NotificationType.fromValue(type);
        } catch (Throwable e) {
            return NotificationType.ERROR;
        }
    }

    private SystemNotificationCategory getCategory(Integer type) {
        try {
            return SystemNotificationCategory.fromValue(type);
        } catch (Throwable e) {
            return SystemNotificationCategory.JOC;
        }
    }
}
