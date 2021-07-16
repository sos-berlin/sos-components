package com.sos.joc.monitoring.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

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
import com.sos.joc.model.common.MonitoringNotificationTypeText;
import com.sos.joc.model.monitoring.NotificationItem;
import com.sos.joc.model.monitoring.NotificationItemJobItem;
import com.sos.joc.model.monitoring.NotificationsAnswer;
import com.sos.joc.model.monitoring.NotificationsFilter;
import com.sos.joc.monitoring.resource.INotifications;
import com.sos.monitoring.notification.NotificationType;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.MONITORING)
public class NotificationsImpl extends JOCResourceImpl implements INotifications {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, NotificationsFilter.class);
            NotificationsFilter in = Globals.objectMapper.readValue(inBytes, NotificationsFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermitted(accessToken, in));
            if (response != null) {
                return response;
            }
            if (in.getLimit() == null) {
                in.setLimit(WebserviceConstants.HISTORY_RESULTSET_LIMIT);
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            List<NotificationItem> notifications = new ArrayList<NotificationItem>();

            ScrollableResults sr = null;
            try {
                sr = dbLayer.getNotifications(JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()), in.getControllerId(), in.getLimit());
                while (sr.next()) {
                    notifications.add(convert((NotificationDBItemEntity) sr.get(0)));
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (sr != null) {
                    sr.close();
                }
            }

            NotificationsAnswer answer = new NotificationsAnswer();
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

    private NotificationItem convert(NotificationDBItemEntity entity) {
        NotificationItem item = new NotificationItem();
        item.setNotificationId(entity.getNotificationId());
        item.setType(getType(entity.getType()));
        item.setCreated(entity.getCreated());
        item.setHasMonitors(entity.getHasMonitors());

        item.setControllerId(entity.getControllerId());
        item.setOrderId(entity.getOrderId());
        item.setWorkflow(entity.getWorkflowPath());
        item.setMessage(getMessage(entity));

        if (!SOSString.isEmpty(entity.getOrderStepJobName())) {
            NotificationItemJobItem job = new NotificationItemJobItem();
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
        return item;
    }

    private MonitoringNotificationTypeText getType(Integer type) {
        try {
            return MonitoringNotificationTypeText.fromValue(NotificationType.fromValue(type).value());
        } catch (Throwable e) {
            return MonitoringNotificationTypeText.ERROR;
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

    private boolean getPermitted(String accessToken, NotificationsFilter in) {
        String controllerId = in.getControllerId();
        Set<String> allowedControllers = Collections.emptySet();
        boolean permitted = false;
        if (controllerId == null || controllerId.isEmpty()) {
            controllerId = "";
            allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                    availableController, accessToken).getOrders().getView()).collect(Collectors.toSet());
            permitted = !allowedControllers.isEmpty();
            if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                allowedControllers = Collections.emptySet();
            }
        } else {
            allowedControllers = Collections.singleton(controllerId);
            permitted = getControllerPermissions(controllerId, accessToken).getOrders().getView();
        }
        return permitted;
    }
}
