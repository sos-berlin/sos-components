package com.sos.joc.monitoring.impl;

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
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.history.DBItemHistoryController;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.monitoring.ControllerItem;
import com.sos.joc.model.monitoring.ControllerItemEntryItem;
import com.sos.joc.model.monitoring.ControllersAnswer;
import com.sos.joc.model.monitoring.ControllersFilter;
import com.sos.joc.monitoring.resource.IControllers;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.MONITORING)
public class ControllersImpl extends JOCResourceImpl implements IControllers {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, ControllersFilter.class);
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
                sr = dbLayer.getControllers(in.getControllerId(), JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()), JobSchedulerDate
                        .getDateTo(in.getDateTo(), in.getTimeZone()));

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
            answer.setControllers(getControllers(dbLayer, map, in.getDateFrom() != null, in.getDateTo() != null));
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

    private List<ControllerItem> getControllers(MonitoringDBLayer dbLayer, Map<String, List<DBItemHistoryController>> map, boolean getPrevious,
            boolean getLast) throws SOSHibernateException {

        Map<String, Long> lastControllers = null;
        if (getLast) {
            lastControllers = getLastControllers(dbLayer);
        }

        final List<ControllerItem> result = new ArrayList<>();
        for (Map.Entry<String, List<DBItemHistoryController>> e : map.entrySet()) {
            ControllerItem controller = new ControllerItem();
            controller.setControllerId(e.getKey());

            int size = e.getValue().size();
            long totalRunningTime = 0;
            int lastIndex = size - 1;
            for (int i = 0; i < size; i++) {
                DBItemHistoryController item = e.getValue().get(i);
                if (i == 0) {
                    controller.setUrl(item.getUri());
                    if (getPrevious) {
                        setPreviousEntry(dbLayer, item, controller);
                    }
                }
                Date lastKnownTime = getLastKnownTime(item);
                if (i == lastIndex) {
                    if (item.getShutdownTime() == null) {
                        if (lastControllers == null) {
                            lastKnownTime = null;
                        } else {
                            Long last = lastControllers.get(item.getControllerId());
                            if (last != null && last.equals(item.getReadyEventId())) {
                                lastKnownTime = null;
                            }
                        }
                    }
                }
                if (lastKnownTime != null) {
                    long diff = lastKnownTime.getTime() - item.getReadyTime().getTime();
                    totalRunningTime += diff;
                }

                ControllerItemEntryItem entry = new ControllerItemEntryItem();
                entry.setReadyTime(item.getReadyTime());
                entry.setLastKnownTime(lastKnownTime);
                entry.setTotalRunningTime(totalRunningTime);
                controller.getEntries().add(entry);
            }
            result.add(controller);
        }
        ;
        return result;
    }

    private Map<String, Long> getLastControllers(MonitoringDBLayer dbLayer) throws SOSHibernateException {
        Map<String, Long> result = new HashMap<String, Long>();
        List<Object[]> l = dbLayer.getLastControllers();
        for (Object[] o : l) {
            result.put(o[1].toString(), (Long) o[0]);
        }
        return result;
    }

    private void setPreviousEntry(MonitoringDBLayer dbLayer, DBItemHistoryController item, ControllerItem controller) {
        try {
            DBItemHistoryController previousItem = dbLayer.getPreviousController(item.getControllerId(), item.getReadyEventId());
            if (previousItem != null) {
                ControllerItemEntryItem previousEntry = new ControllerItemEntryItem();
                previousEntry.setReadyTime(previousItem.getReadyTime());
                previousEntry.setLastKnownTime(getLastKnownTime(previousItem));
                previousEntry.setTotalRunningTime(previousEntry.getLastKnownTime().getTime() - previousEntry.getReadyTime().getTime());
                controller.setPreviousEntry(previousEntry);
            }
        } catch (SOSHibernateException e1) {

        }
    }

    private Date getLastKnownTime(DBItemHistoryController item) {
        if (item.getShutdownTime() != null) {
            return item.getShutdownTime();
        } else if (item.getLastKnownTime() != null) {
            return item.getLastKnownTime();
        } else {
            return item.getReadyTime();
        }
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
