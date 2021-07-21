package com.sos.joc.monitoring.impl;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.hibernate.ScrollableResults;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.monitoring.ControllerItem;
import com.sos.joc.model.monitoring.ControllerItemEntryItem;
import com.sos.joc.model.monitoring.ControllersAnswer;
import com.sos.joc.model.monitoring.ControllersFilter;
import com.sos.joc.model.monitoring.NotificationsFilter;
import com.sos.joc.monitoring.resource.IControllers;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.MONITORING)
public class ControllersImpl extends JOCResourceImpl implements IControllers {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, NotificationsFilter.class);
            ControllersFilter in = Globals.objectMapper.readValue(inBytes, ControllersFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermitted(accessToken, in));
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            Map<String, List<DBItemHistoryController>> map = new HashMap<>();
            ScrollableResults sr = null;
            try {
                sr = dbLayer.getControllers();
                while (sr.next()) {
                    DBItemHistoryController item = (DBItemHistoryController) sr.get(0);
                    List<DBItemHistoryController> l = map.containsKey(item.getControllerId()) ? map.get(item.getControllerId()) : new ArrayList<>();

                    l.add(item);
                    map.put(item.getControllerId(), l);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (sr != null) {
                    sr.close();
                }
            }

            ControllersAnswer answer = new ControllersAnswer();
            answer.setDeliveryDate(new Date());
            answer.setControllers(getControllers(map));
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

    private List<ControllerItem> getControllers(Map<String, List<DBItemHistoryController>> map) {
        final List<ControllerItem> result = new ArrayList<>();
        map.entrySet().stream().forEach(e -> {
            ControllerItem controller = new ControllerItem();
            controller.setControllerId(e.getKey());

            int size = e.getValue().size();
            for (int i = 0; i < size; i++) {
                boolean isLast = i == size - 1;
                DBItemHistoryController item = e.getValue().get(i);

                ControllerItemEntryItem entry = new ControllerItemEntryItem();
                entry.setReadyTime(item.getReadyTime());

                if (item.getShutdownTime() == null) {
                    if (isLast) {
                        entry.setTotalRunningTime(item.getTotalRunningTime());
                    } else {
                        DBItemHistoryController nextItem = e.getValue().get(i + 1);
                        Long trt = nextItem.getTotalRunningTime() - item.getTotalRunningTime();
                        entry.setTotalRunningTime(trt);
                        entry.setShutdownTime(SOSDate.add(item.getReadyTime(), trt, ChronoUnit.MILLIS));
                    }

                } else {
                    entry.setShutdownTime(item.getShutdownTime());
                    long diff = item.getShutdownTime().getTime() - item.getReadyTime().getTime();
                    entry.setTotalRunningTime(item.getTotalRunningTime() + diff);
                }

                controller.getEntries().add(entry);
            }
            result.add(controller);
        });
        return result;
    }

    private boolean getPermitted(String accessToken, ControllersFilter in) {
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
