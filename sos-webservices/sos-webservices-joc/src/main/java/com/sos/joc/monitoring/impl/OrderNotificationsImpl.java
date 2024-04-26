package com.sos.joc.monitoring.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.ScrollableResults;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.history.HistoryMapper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.db.monitoring.NotificationDBItemEntity;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.monitoring.notification.common.AcknowledgementItem;
import com.sos.joc.model.monitoring.notification.order.OrderNotificationsAnswer;
import com.sos.joc.model.monitoring.notification.order.OrderNotificationsFilter;
import com.sos.joc.model.monitoring.notification.order.items.OrderNotificationItem;
import com.sos.joc.model.monitoring.notification.order.items.OrderNotificationJobItem;
import com.sos.joc.monitoring.resource.IOrderNotifications;
import com.sos.monitoring.notification.NotificationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.MONITORING)
public class OrderNotificationsImpl extends JOCResourceImpl implements IOrderNotifications {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, OrderNotificationsFilter.class);
            OrderNotificationsFilter in = Globals.objectMapper.readValue(inBytes, OrderNotificationsFilter.class);

            // 1) notification view permitted
            if (!getJocPermissions(accessToken).getNotification().getView()) {
                return initPermissions(in.getControllerId(), false);
            }
            // 2) controller permitted (because of controller related monitoring entries)
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
            Map<String, Set<Folder>> permittedFolders = folderPermissions.getListOfFolders(allowedControllers);
            if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                allowedControllers = Collections.emptySet();
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            List<OrderNotificationItem> notifications = new ArrayList<OrderNotificationItem>();
            ScrollableResults<NotificationDBItemEntity> sr = null;
            try {
                if (in.getNotificationIds() == null || in.getNotificationIds().size() == 0) {
                    sr = dbLayer.getOrderNotifications(JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()), allowedControllers, types, in
                            .getLimit());
                } else {
                    sr = dbLayer.getOrderNotifications(in.getNotificationIds(), allowedControllers, types);
                }
                while (sr.next()) {
                    OrderNotificationItem item = convert(sr.get());
                    if (canAdd(item.getWorkflow(), permittedFolders.get(item.getControllerId()))) {
                        notifications.add(item);
                    }
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (sr != null) {
                    sr.close();
                }
            }

            OrderNotificationsAnswer answer = new OrderNotificationsAnswer();
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

    private List<Integer> getTypes(OrderNotificationsFilter in) {
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

    private OrderNotificationItem convert(NotificationDBItemEntity entity) {
        OrderNotificationItem item = new OrderNotificationItem();
        item.setNotificationId(entity.getId());
        item.setType(getType(entity.getType()));
        item.setCreated(entity.getCreated());
        item.setHasMonitors(entity.getHasMonitors());
        if (entity.getRecoveredNotificationId() != null && entity.getRecoveredNotificationId() > 0L) {
            item.setRecoveredNotificationId(entity.getRecoveredNotificationId());
        }

        item.setControllerId(entity.getControllerId());
        item.setOrderId(entity.getOrderId());
        item.setWorkflow(entity.getWorkflowPath());
        item.setMessage(getMessage(entity));

        if (!SOSString.isEmpty(entity.getOrderStepJobName())) {
            OrderNotificationJobItem job = new OrderNotificationJobItem();
            job.setJob(entity.getOrderStepJobName());
            job.setStartTime(entity.getOrderStepStartTime());
            job.setEndTime(entity.getOrderStepEndTime());
            job.setPosition(entity.getOrderStepWorkflowPosition());
            job.setState(HistoryMapper.getState(entity.getOrderStepSeverity()));
            job.setCriticality(getCriticality(entity).value().toLowerCase());
            job.setTaskId(entity.getOrderStepHistoryId());
            job.setAgentUrl(entity.getOrderStepAgentUri());
            job.setExitCode(entity.getOrderStepReturnCode());

            item.setJob(job);
        }
        if (!SOSString.isEmpty(entity.getAcknowledgementAccount())) {
            AcknowledgementItem ac = new AcknowledgementItem();
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

    private String getMessage(NotificationDBItemEntity entity) {
        if (entity.getType().equals(NotificationType.WARNING.intValue())) {
            return entity.getOrderStepWarnText();
        }
        if (!SOSString.isEmpty(entity.getOrderStepErrorText())) {
            return entity.getOrderStepErrorText();
        }
        return entity.getOrderErrorText();
    }

    private JobCriticality getCriticality(NotificationDBItemEntity entity) {
        try {
            return JobCriticality.fromValue(entity.getOrderStepJobCriticality());
        } catch (Throwable e) {
            return JobCriticality.NORMAL;
        }
    }
}
