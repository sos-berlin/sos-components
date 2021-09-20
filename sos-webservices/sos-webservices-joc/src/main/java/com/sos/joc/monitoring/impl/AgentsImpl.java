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
import com.sos.commons.util.SOSDate;
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
            // dateFrom, dateTo - already UTC time , use UTC instead of in.getTimeZone()
            Date dateFrom = JobSchedulerDate.getDateFrom(in.getDateFrom(), "UTC");
            Date dateTo = JobSchedulerDate.getDateTo(in.getDateTo(), "UTC");
            ScrollableResults sr = null;
            try {
                sr = dbLayer.getAgents(in.getControllerId(), dateFrom, dateTo);
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

    private List<AgentsControllerItem> getPreviousControllers(MonitoringDBLayer dbLayer, Date dateFrom, Date dateTo) throws SOSHibernateException {
        final List<AgentsControllerItem> result = new ArrayList<>();
        if (dateFrom != null && dateTo != null) {
            Date now = new Date();
            if (dateFrom.getTime() > now.getTime() && dateTo.getTime() > now.getTime()) {
                return result;
            }

            Map<String, Map<String, Long>> previousAgents = getPreviousAgents(dbLayer, dateFrom);
            if (previousAgents != null && previousAgents.size() > 0) {
                for (Map.Entry<String, Map<String, Long>> entry : previousAgents.entrySet()) {
                    AgentsControllerItem controller = new AgentsControllerItem();
                    controller.setControllerId(entry.getKey());
                    for (Map.Entry<String, Long> a : entry.getValue().entrySet()) {
                        AgentItem agent = new AgentItem();
                        agent.setAgentId(a.getKey());

                        DBItemHistoryAgent item = dbLayer.getAgent(entry.getKey(), a.getKey(), a.getValue());
                        if (item != null) {
                            agent.setUrl(item.getUri());

                            AgentItemEntryItem prev = new AgentItemEntryItem();
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
                            agent.setPreviousEntry(prev);

                            controller.getAgents().add(agent);
                        }
                    }
                    result.add(controller);
                }
            }
        }
        return result;
    }

    private List<AgentsControllerItem> getControllers(MonitoringDBLayer dbLayer, Map<String, Map<String, List<DBItemHistoryAgent>>> map,
            Date dateFrom, Date dateTo, boolean getPrevious, boolean getLast) throws SOSHibernateException {

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
                            AgentItemEntryItem prev = setPreviousEntry(dbLayer, item, agent);
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
            String controllerId = o[1].toString();
            Map<String, Long> m = result.get(controllerId);
            if (m == null) {
                m = new HashMap<>();
            }
            m.put(o[2].toString(), (Long) o[0]);
            result.put(controllerId, m);
        }
        return result;
    }

    private Map<String, Map<String, Long>> getPreviousAgents(MonitoringDBLayer dbLayer, Date dateFrom) throws SOSHibernateException {
        Map<String, Map<String, Long>> result = new HashMap<>();
        List<Object[]> l = dbLayer.getPreviousAgents(dateFrom);
        for (Object[] o : l) {
            String controllerId = o[1].toString();
            Map<String, Long> m = result.get(controllerId);
            if (m == null) {
                m = new HashMap<>();
            }
            m.put(o[2].toString(), (Long) o[0]);
            result.put(controllerId, m);
        }
        return result;
    }

    private AgentItemEntryItem setPreviousEntry(MonitoringDBLayer dbLayer, DBItemHistoryAgent item, AgentItem agent) {
        try {
            DBItemHistoryAgent pa = dbLayer.getPreviousAgent(item.getControllerId(), item.getAgentId(), item.getReadyEventId());
            if (pa != null) {
                AgentItemEntryItem prev = new AgentItemEntryItem();
                prev.setReadyTime(pa.getReadyTime());
                prev.setLastKnownTime(getLastKnownTime(pa));
                prev.setTotalRunningTime(prev.getLastKnownTime().getTime() - prev.getReadyTime().getTime());
                agent.setPreviousEntry(prev);
                return prev;
            }
        } catch (SOSHibernateException e1) {

        }
        return null;
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
