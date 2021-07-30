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
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.monitoring.MonitoringDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.monitoring.AgentItem;
import com.sos.joc.model.monitoring.AgentItemEntryItem;
import com.sos.joc.model.monitoring.AgentsAnswer;
import com.sos.joc.model.monitoring.AgentsControllerItem;
import com.sos.joc.model.monitoring.AgentsFilter;
import com.sos.joc.monitoring.resource.IAgents;
import com.sos.schema.JsonValidator;

@Path(WebservicePaths.MONITORING)
public class AgentsImpl extends JOCResourceImpl implements IAgents {

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, AgentsFilter.class);
            AgentsFilter in = Globals.objectMapper.readValue(inBytes, AgentsFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getPermitted(accessToken, in));
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            Map<String, Map<String, List<DBItemHistoryAgent>>> map = new HashMap<>();
            ScrollableResults sr = null;
            try {
                sr = dbLayer.getAgents(in.getControllerId(), JobSchedulerDate.getDateFrom(in.getDateFrom(), in.getTimeZone()), JobSchedulerDate
                        .getDateTo(in.getDateTo(), in.getTimeZone()));
                while (sr.next()) {
                    DBItemHistoryAgent item = (DBItemHistoryAgent) sr.get(0);

                    Map<String, List<DBItemHistoryAgent>> m = map.containsKey(item.getControllerId()) ? map.get(item.getControllerId())
                            : new HashMap<>();
                    List<DBItemHistoryAgent> l = m.containsKey(item.getAgentId()) ? m.get(item.getAgentId()) : new ArrayList<>();
                    l.add(item);
                    m.put(item.getAgentId(), l);

                    map.put(item.getControllerId(), m);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                if (sr != null) {
                    sr.close();
                }
            }

            AgentsAnswer answer = new AgentsAnswer();
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

    private List<AgentsControllerItem> getControllers(MonitoringDBLayer dbLayer, Map<String, Map<String, List<DBItemHistoryAgent>>> map,
            boolean getPrevious, boolean getLast) throws SOSHibernateException {

        Map<String, Map<String, Long>> lastAgents = null;
        if (getLast) {
            lastAgents = getLastAgents(dbLayer);
        }

        final List<AgentsControllerItem> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<DBItemHistoryAgent>>> e : map.entrySet()) {
            AgentsControllerItem controller = new AgentsControllerItem();
            controller.setControllerId(e.getKey());

            for (Map.Entry<String, List<DBItemHistoryAgent>> a : e.getValue().entrySet()) {
                AgentItem agent = new AgentItem();
                agent.setAgentId(a.getKey());

                int size = a.getValue().size();
                long totalRunningTime = 0;
                int lastIndex = size - 1;
                for (int i = 0; i < size; i++) {
                    DBItemHistoryAgent item = a.getValue().get(i);
                    if (i == 0) {
                        agent.setUrl(item.getUri());
                        if (getPrevious) {
                            setPreviousEntry(dbLayer, item, agent);
                        }
                    }
                    Date lastKnownTime = getLastKnownTime(item);
                    if (i == lastIndex) {
                        if (item.getShutdownTime() == null) {
                            if (lastAgents == null) {
                                lastKnownTime = null;
                            } else {
                                Map<String, Long> last = lastAgents.get(item.getControllerId());
                                if (last != null && last.containsKey(item.getAgentId())) {
                                    if (last.get(item.getAgentId()).equals(item.getReadyEventId())) {
                                        lastKnownTime = null;
                                    }
                                }
                            }
                        }
                    }
                    if (lastKnownTime != null) {
                        long diff = lastKnownTime.getTime() - item.getReadyTime().getTime();
                        totalRunningTime += diff;
                    }
                    AgentItemEntryItem entry = new AgentItemEntryItem();
                    entry.setReadyTime(item.getReadyTime());
                    entry.setLastKnownTime(lastKnownTime);
                    entry.setTotalRunningTime(totalRunningTime);
                    agent.getEntries().add(entry);
                }

                controller.getAgents().add(agent);
            }

            result.add(controller);
        }
        return result;
    }

    private Map<String, Map<String, Long>> getLastAgents(MonitoringDBLayer dbLayer) throws SOSHibernateException {
        Map<String, Map<String, Long>> result = new HashMap<>();
        List<Object[]> l = dbLayer.getLastAgents();
        for (Object[] o : l) {
            result.put(o[1].toString(), Collections.singletonMap(o[2].toString(), (Long) o[0]));
        }
        return result;
    }

    private void setPreviousEntry(MonitoringDBLayer dbLayer, DBItemHistoryAgent item, AgentItem agent) {
        try {
            DBItemHistoryAgent previousItem = dbLayer.getPreviousAgent(item.getControllerId(), item.getAgentId(), item.getReadyEventId());
            if (previousItem != null) {
                AgentItemEntryItem previousEntry = new AgentItemEntryItem();
                previousEntry.setReadyTime(previousItem.getReadyTime());
                previousEntry.setLastKnownTime(getLastKnownTime(previousItem));
                previousEntry.setTotalRunningTime(previousEntry.getLastKnownTime().getTime() - previousEntry.getReadyTime().getTime());
                agent.setPreviousEntry(previousEntry);
            }
        } catch (SOSHibernateException e1) {

        }
    }

    private Date getLastKnownTime(DBItemHistoryAgent item) {
        if (item.getShutdownTime() != null) {
            return item.getShutdownTime();
        } else {
            if (item.getCouplingFailedTime() != null && item.getLastKnownTime() != null) {
                return item.getCouplingFailedTime().getTime() > item.getLastKnownTime().getTime() ? item.getCouplingFailedTime() : item
                        .getLastKnownTime();
            } else if (item.getCouplingFailedTime() != null) {
                return item.getCouplingFailedTime();
            } else if (item.getLastKnownTime() != null) {
                return item.getLastKnownTime();
            } else {
                return item.getReadyTime();
            }
        }
    }

    private boolean getPermitted(String accessToken, AgentsFilter in) {
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
