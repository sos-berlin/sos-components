package com.sos.joc.monitoring.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.monitoring.DBItemNotificationMonitor;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.MonitoringMonitorTypeText;
import com.sos.joc.model.monitoring.MonitorItem;
import com.sos.joc.model.monitoring.NotificationAnswer;
import com.sos.joc.model.monitoring.NotificationFilter;
import com.sos.joc.monitoring.resource.INotification;
import com.sos.monitoring.MonitorType;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.MONITORING)
public class NotificationImpl extends JOCResourceImpl implements INotification {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, NotificationFilter.class);
            NotificationFilter in = Globals.objectMapper.readValue(inBytes, NotificationFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermitted(accessToken, in));
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);

            List<MonitorItem> monitors = new ArrayList<MonitorItem>();
            List<DBItemNotificationMonitor> result = dbLayer.getNotificationMonitors(in.getNotificationId());
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

    private boolean getPermitted(String accessToken, NotificationFilter in) {
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
