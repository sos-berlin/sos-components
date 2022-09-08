package com.sos.joc.monitoring.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import org.hibernate.ScrollableResults;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
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
            // dateFrom, dateTo - already UTC time , use UTC instead of in.getTimeZone()
            Date dateFrom = JobSchedulerDate.getDateFrom(in.getDateFrom(), "UTC");
            Date dateTo = JobSchedulerDate.getDateTo(in.getDateTo(), "UTC");
            ScrollableResults sr = null;
            try {
                sr = dbLayer.getControllers(in.getControllerId(), dateFrom, dateTo);

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
            if (map.size() == 0) {
                answer.setControllers(getPreviousControllers(dbLayer, dateFrom, dateTo));
            } else {
                answer.setControllers(getControllers(dbLayer, map, dateFrom, dateTo, in.getDateFrom() != null, in.getDateTo() != null));
            }
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

    private List<ControllerItem> getPreviousControllers(MonitoringDBLayer dbLayer, Date dateFrom, Date dateTo) throws SOSHibernateException {
        final List<ControllerItem> result = new ArrayList<>();
        if (dateFrom != null && dateTo != null) {
            Date now = new Date();
            if (dateFrom.getTime() > now.getTime() && dateTo.getTime() > now.getTime()) {
                return result;
            }

            Map<String, Long> previousControllers = getPreviousControllers(dbLayer, dateFrom);
            if (previousControllers != null && previousControllers.size() > 0) {
                for (Map.Entry<String, Long> entry : previousControllers.entrySet()) {
                    DBItemHistoryController item = dbLayer.getController(entry.getKey(), entry.getValue());
                    if (item != null) {
                        ControllerItem controller = new ControllerItem();
                        controller.setControllerId(item.getControllerId());
                        controller.setUrl(item.getUri());

                        ControllerItemEntryItem prev = new ControllerItemEntryItem();
                        prev.setReadyTime(item.getReadyTime());
                        if (item.getShutdownTime() == null) {
                            Date lkt = getLastKnownTime(item);
                            if (lkt != null && !SOSDate.equals(lkt, item.getReadyTime())) {
                                prev.setLastKnownTime(lkt);
                            }
                        } else {
                            prev.setLastKnownTime(item.getShutdownTime());
                        }

                        if (prev.getLastKnownTime() == null) {
                            long diff = now.getTime() - dateFrom.getTime();
                            prev.setTotalRunningTime(diff);
                        } else {
                            if (prev.getLastKnownTime().getTime() > dateFrom.getTime()) {
                                long diff = prev.getLastKnownTime().getTime() - dateFrom.getTime();
                                prev.setTotalRunningTime(diff);
                            }
                        }
                        controller.setPreviousEntry(prev);
                        result.add(controller);
                    }
                }
            }
        }
        return result;
    }

    private List<ControllerItem> getControllers(MonitoringDBLayer dbLayer, Map<String, List<DBItemHistoryController>> map, Date dateFrom, Date dateTo,
            boolean getPrevious, boolean getLast) throws SOSHibernateException {

        Map<String, Long> lastControllers = null;
        if (getLast) {
            lastControllers = getLastControllers(dbLayer, dateTo);
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
                        ControllerItemEntryItem prev = setPreviousEntry(dbLayer, item, controller);
                        if (dateFrom != null && prev != null && prev.getLastKnownTime() != null) {
                            if (prev.getReadyTime().getTime() < dateFrom.getTime() && prev.getLastKnownTime().getTime() > dateFrom.getTime()) {
                                long diff = prev.getLastKnownTime().getTime() - dateFrom.getTime();
                                totalRunningTime += diff;
                            }
                        }
                    }
                }
                Date lastKnownTime = getLastKnownTime(item);
                if (i == lastIndex) {
                    if (item.getShutdownTime() == null) {
                        if (lastControllers == null || lastControllers.size() == 0) {
                            lastKnownTime = null;
                        } else {
                            Long last = lastControllers.get(item.getControllerId());
                            if (last != null && last.equals(item.getReadyEventId())) {
                                lastKnownTime = null;
                            }
                        }
                    }

                    if (lastKnownTime == null && dateTo != null) {
                        Date now = new Date();
                        if (dateTo.getTime() < now.getTime()) {
                            long diff = dateTo.getTime() - item.getReadyTime().getTime();
                            totalRunningTime += diff;
                        } else {
                            long diff = now.getTime() - item.getReadyTime().getTime();
                            totalRunningTime += diff;
                        }
                    }
                }
                if (lastKnownTime != null) {
                    long diff = 0;
                    if (dateTo != null && lastKnownTime.getTime() > dateTo.getTime()) {
                        diff = dateTo.getTime() - item.getReadyTime().getTime();
                    } else {
                        diff = lastKnownTime.getTime() - item.getReadyTime().getTime();
                    }
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
        return result;
    }

    private Map<String, Long> getLastControllers(MonitoringDBLayer dbLayer, Date dateTo) throws SOSHibernateException {
        Map<String, Long> result = new HashMap<String, Long>();
        List<Object[]> l = dbLayer.getLastControllers(dateTo);
        for (Object[] o : l) {
            result.put(o[1].toString(), (Long) o[0]);
        }
        return result;
    }

    private Map<String, Long> getPreviousControllers(MonitoringDBLayer dbLayer, Date dateFrom) throws SOSHibernateException {
        Map<String, Long> result = new HashMap<String, Long>();
        List<Object[]> l = dbLayer.getPreviousControllers(dateFrom);
        for (Object[] o : l) {
            result.put(o[1].toString(), (Long) o[0]);
        }
        return result;
    }

    private ControllerItemEntryItem setPreviousEntry(MonitoringDBLayer dbLayer, DBItemHistoryController item, ControllerItem controller) {
        try {
            DBItemHistoryController pc = dbLayer.getPreviousController(item.getControllerId(), item.getReadyEventId());
            if (pc != null) {
                ControllerItemEntryItem prev = new ControllerItemEntryItem();
                prev.setReadyTime(pc.getReadyTime());
                prev.setLastKnownTime(getLastKnownTime(pc));
                prev.setTotalRunningTime(prev.getLastKnownTime().getTime() - prev.getReadyTime().getTime());
                controller.setPreviousEntry(prev);
                return prev;
            }
        } catch (SOSHibernateException e1) {

        }
        return null;
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
                    availableController, accessToken).getView()).collect(Collectors.toSet());
            permitted = !allowedControllers.isEmpty();
            if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                allowedControllers = Collections.emptySet();
            }
        } else {
            allowedControllers = Collections.singleton(controllerId);
            permitted = getControllerPermissions(controllerId, accessToken).getView();
        }
        return permitted;
    }
}
