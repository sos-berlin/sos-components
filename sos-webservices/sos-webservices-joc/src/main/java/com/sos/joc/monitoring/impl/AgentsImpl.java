package com.sos.joc.monitoring.impl;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.hibernate.ScrollableResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
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
import com.sos.joc.model.monitoring.AgentItemEntryItemSource;
import com.sos.joc.model.monitoring.AgentsAnswer;
import com.sos.joc.model.monitoring.AgentsControllerItem;
import com.sos.joc.model.monitoring.AgentsFilter;
import com.sos.joc.model.monitoring.enums.EntryItemSource;
import com.sos.joc.model.monitoring.enums.TotalRunningTimeSource;
import com.sos.joc.monitoring.resource.IAgents;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.MONITORING)
public class AgentsImpl extends JOCResourceImpl implements IAgents {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsImpl.class);

    private static final String DEFAULT_TIMEZONE = "Etc/UTC";

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] inBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, inBytes, accessToken);
            JsonValidator.validateFailFast(inBytes, AgentsFilter.class);
            AgentsFilter in = Globals.objectMapper.readValue(inBytes, AgentsFilter.class);

            String controllerId = in.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean noControllerAvailable = Proxies.getControllerDbInstances().isEmpty();
            boolean permitted = noControllerAvailable; // no access denied if no controllers are registered
            if (controllerId == null || controllerId.isEmpty()) {
                if (!noControllerAvailable) {
                    allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                            availableController, accessToken).getAgents().getView()).collect(Collectors.toSet());
                    permitted = !allowedControllers.isEmpty();
                    if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                        allowedControllers = Collections.emptySet();
                    }
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getAgents().getView();
            }

            JOCDefaultResponse response = initPermissions(null, permitted);
            if (response != null) {
                return response;
            }

            AgentsAnswer answer = new AgentsAnswer();
            // dateFrom, dateTo - already UTC time , use UTC instead of in.getTimeZone()
            Date dateFrom = JobSchedulerDate.getDateFrom(in.getDateFrom(), DEFAULT_TIMEZONE);
            Date dateTo = JobSchedulerDate.getDateTo(in.getDateTo(), DEFAULT_TIMEZONE);
            if (dateFrom != null) {
                if (dateFrom.getTime() > new Date().getTime()) {
                    answer.setDeliveryDate(new Date());
                    answer.setControllers(new ArrayList<>());
                    return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
                }
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            Map<String, Map<String, Map<String, Date>>> inventoryAgents = dbLayer.getActiveInventoryAgents();
            Map<String, Set<String>> historyTimeZones = new HashMap<>();
            Map<String, Map<String, List<DBItemHistoryAgent>>> historyAgents = getHistoryAgents(dbLayer, historyTimeZones, allowedControllers,
                    dateFrom, dateTo);
            Globals.disconnect(session);
            session = null;

            mergeInventoryAgentsIfNotInHistory(historyTimeZones, historyAgents, inventoryAgents);
            answer.setControllers(getItems(historyAgents, inventoryAgents, dateFrom, dateTo));
            answer.setDeliveryDate(new Date());
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

    protected Map<String, Map<String, List<DBItemHistoryAgent>>> getHistoryAgents(MonitoringDBLayer dbLayer,
            Map<String, Set<String>> historyTimeZones, Set<String> allowedControllers, Date dateFrom, Date dateTo) throws Exception {
        Map<String, Map<String, List<DBItemHistoryAgent>>> map = new HashMap<>();
        ScrollableResults sr = null;
        try {
            sr = dbLayer.getAgentsWithPrevAndLast(allowedControllers, dateFrom, dateTo);
            while (sr.next()) {
                DBItemHistoryAgent item = (DBItemHistoryAgent) sr.get(0);
                if (item.getControllerId() != null && item.getTimezone() != null) {
                    Set<String> timezones = historyTimeZones.getOrDefault(item.getControllerId(), new HashSet<>());
                    timezones.add(item.getTimezone());
                    historyTimeZones.put(item.getControllerId(), timezones);
                }

                Map<String, List<DBItemHistoryAgent>> ha = map.getOrDefault(item.getControllerId(), new HashMap<>());
                List<DBItemHistoryAgent> l = ha.getOrDefault(item.getAgentId(), new ArrayList<>());
                l.add(item);
                ha.put(item.getAgentId(), l);

                map.put(item.getControllerId(), ha);
            }
        } catch (Exception e) {
            throw e;
        } finally {
            if (sr != null) {
                sr.close();
            }
        }
        return map;
    }

    private String getDefaultTimeZone(Map<String, Set<String>> historyTimeZones, String controllerId) {
        Set<String> timeZones = historyTimeZones.get(controllerId);
        if (timeZones == null || timeZones.size() == 0) {
            return DEFAULT_TIMEZONE;
        } else {
            return timeZones.iterator().next();
        }
    }

    protected void mergeInventoryAgentsIfNotInHistory(Map<String, Set<String>> historyTimeZones,
            Map<String, Map<String, List<DBItemHistoryAgent>>> historyAgents, Map<String, Map<String, Map<String, Date>>> inventoryAgents) {
        // if inventory agent is deployed but not in the history ...
        inventoryAgents.entrySet().stream().forEach(e -> {
            String controllerId = e.getKey();

            // TODO: inventory agents missing time zone, try to set from history entries....
            final String defaultTimeZone = getDefaultTimeZone(historyTimeZones, controllerId);
            Map<String, List<DBItemHistoryAgent>> ha = historyAgents.getOrDefault(controllerId, new HashMap<>());
            e.getValue().entrySet().stream().forEach(a -> {
                String agentId = a.getKey();
                if (!ha.containsKey(agentId)) {
                    DBItemHistoryAgent item = new DBItemHistoryAgent();
                    Map.Entry<String, Date> inventoryAgentInfo = getAgentUrlAndModified(a.getValue());
                    item.setReadyTime(inventoryAgentInfo.getValue());
                    item.setReadyEventId(item.getReadyTime().getTime() * 1_000);

                    item.setControllerId(controllerId);
                    item.setAgentId(agentId);
                    item.setUri(inventoryAgentInfo.getKey());
                    item.setTimezone(defaultTimeZone);
                    item.setCreated(null);// used to identify inventory item

                    List<DBItemHistoryAgent> l = ha.getOrDefault(item.getAgentId(), new ArrayList<>());
                    l.add(item);
                    ha.put(item.getAgentId(), l);

                    historyAgents.put(item.getControllerId(), ha);
                }
            });
        });
    }

    protected List<AgentsControllerItem> getItems(Map<String, Map<String, List<DBItemHistoryAgent>>> historyAgents,
            Map<String, Map<String, Map<String, Date>>> inventoryAgents, Date filterDateFrom, Date filterDateTo) {

        Date now = new Date();
        Date dateFrom = filterDateFrom == null ? new Date(0) : filterDateFrom;
        Date dateTo = filterDateTo == null ? now : filterDateTo;

        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        final List<AgentsControllerItem> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, List<DBItemHistoryAgent>>> e : historyAgents.entrySet()) {
            AgentsControllerItem controller = new AgentsControllerItem();
            controller.setControllerId(e.getKey());

            Map<String, Map<String, Date>> inventoryAgent = inventoryAgents.getOrDefault(controller.getControllerId(), new HashMap<>());

            agentList: for (Map.Entry<String, List<DBItemHistoryAgent>> a : e.getValue().entrySet()) {
                int size = a.getValue().size();
                int lastIndex = size - 1;

                AgentItem agent = new AgentItem();
                agent.setAgentId(a.getKey());

                Map.Entry<String, Date> inventoryAgentInfo = getAgentUrlAndModified(inventoryAgent.get(agent.getAgentId()));
                if (inventoryAgentInfo == null) {// no more the in inventory
                    if (size == 1) {// only prev
                        DBItemHistoryAgent item = a.getValue().get(0);
                        if (item.getReadyTime().getTime() < dateFrom.getTime()) {
                            continue agentList;
                        }
                    }
                }

                long totalRunningTime = 0;
                agentTimes: for (int i = 0; i < size; i++) {
                    DBItemHistoryAgent item = a.getValue().get(i);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][%s][totalRunningTime=%s]%s", i, item.getAgentId(), SOSDate.tryGetDateTimeAsString(item
                                .getReadyTime()), totalRunningTime, SOSHibernate.toString(item)));
                    }
                    if (i == 0) {
                        agent.setUrl(item.getUri());

                        // Previous item - before dateFrom
                        if (item.getReadyTime().getTime() < dateFrom.getTime()) {
                            AgentItemEntryItem prev = new AgentItemEntryItem();
                            AgentItemEntryItemSource prevSource = new AgentItemEntryItemSource();

                            prev.setReadyTime(item.getReadyTime());
                            prevSource.setItem(EntryItemSource.history);

                            if (item.getShutdownTime() == null) {
                                prev.setTotalRunningTime(dateFrom.getTime() - prev.getReadyTime().getTime());
                                prevSource.setTotalRunningTime(TotalRunningTimeSource.dateFrom);

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("   [%s][1.1]totalRunningTime=%s", i, prev.getTotalRunningTime()));
                                }
                            } else {
                                prev.setLastKnownTime(item.getShutdownTime());
                                prev.setTotalRunningTime(prev.getLastKnownTime().getTime() - prev.getReadyTime().getTime());
                                prevSource.setTotalRunningTime(TotalRunningTimeSource.shutdown);

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("   [%s][1.2]totalRunningTime=%s", i, prev.getTotalRunningTime()));
                                }
                            }
                            prev.setSource(prevSource);
                            agent.setPreviousEntry(prev);

                            if (prev.getLastKnownTime() == null) {
                                int nextIndex = i + 1;
                                Date nextReady = dateFrom;
                                boolean nextReadyExists = false;
                                if (nextIndex < size) {
                                    nextReady = a.getValue().get(nextIndex).getReadyTime();
                                    nextReadyExists = true;
                                }

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("   [%s][2][nextReadyExists=%s][nextReady=%s]totalRunningTime=%s", i, nextReadyExists,
                                            SOSDate.tryGetDateTimeAsString(nextReady), totalRunningTime));
                                }

                                AgentItemEntryItemSource source = new AgentItemEntryItemSource();
                                source.setItem(EntryItemSource.webservice);
                                Date tmpLastKnownTime = null;
                                if (nextReady.before(dateTo)) {
                                    if (nextReadyExists) {
                                        tmpLastKnownTime = nextReady;
                                        source.setTotalRunningTime(TotalRunningTimeSource.nextReadyTime);
                                    } else {
                                        tmpLastKnownTime = dateTo;
                                        source.setTotalRunningTime(TotalRunningTimeSource.dateFrom);
                                    }

                                } else {
                                    tmpLastKnownTime = dateTo;
                                    source.setTotalRunningTime(TotalRunningTimeSource.dateTo);
                                }

                                totalRunningTime = tmpLastKnownTime.getTime() - dateFrom.getTime();

                                AgentItemEntryItem entry = new AgentItemEntryItem();
                                entry.setReadyTime(dateFrom);
                                entry.setLastKnownTime(tmpLastKnownTime);
                                entry.setTotalRunningTime(totalRunningTime);
                                entry.setSource(source);
                                agent.getEntries().add(entry);

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("   [%s][2.1][addEntry]%s", i, SOSString.toString(entry)));
                                }
                            }
                            continue agentTimes;
                        }
                    }

                    AgentItemEntryItemSource source = new AgentItemEntryItemSource();
                    // see mergeInventoryAgents...
                    source.setItem(item.getCreated() == null ? EntryItemSource.inventory : EntryItemSource.history);

                    Date lastKnownTime = item.getShutdownTime();
                    if (lastKnownTime == null) {
                        int nextIndex = i + 1;
                        if (nextIndex < size) {
                            lastKnownTime = a.getValue().get(nextIndex).getReadyTime();
                            if (lastKnownTime.after(dateTo)) {
                                lastKnownTime = SOSDate.add(dateTo, -1, ChronoUnit.SECONDS);
                                source.setTotalRunningTime(TotalRunningTimeSource.nextReadyTime);

                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("   [%s][4]lastKnownTime=%s", i, SOSDate.tryGetDateTimeAsString(lastKnownTime)));
                                }
                            }
                        }
                    } else {
                        source.setTotalRunningTime(TotalRunningTimeSource.shutdown);
                    }

                    if (i == lastIndex) {
                        // see getAgentsWithPrevAndLast 3) max/last
                        if (item.getReadyTime().getTime() >= dateTo.getTime()) {
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("   [%s][5.1][readyTime >= dateTo][%s >= %s]break", i, SOSDate.tryGetDateTimeAsString(item
                                        .getReadyTime()), SOSDate.tryGetDateTimeAsString(dateTo)));
                            }
                            break agentTimes;
                        }
                        if (inventoryAgentInfo == null) {// no more in inventory
                            if (item.getShutdownTime() == null) {
                                lastKnownTime = tryToEstimateIfEndDateUnknown(item, dateTo);
                                source.setItem(EntryItemSource.historyNotInInventory);
                                source.setTotalRunningTime(TotalRunningTimeSource.estimated);
                                if (isDebugEnabled) {
                                    LOGGER.debug(String.format("   [%s][5.2][inventoryAgentInfo=null]lastKnownTime=%s", i, SOSDate
                                            .tryGetDateTimeAsString(lastKnownTime)));
                                }
                            }
                        }
                    }

                    if (lastKnownTime == null) {
                        if (now.after(dateTo)) {
                            long diff = dateTo.getTime() - item.getReadyTime().getTime();
                            totalRunningTime += diff;
                            source.setTotalRunningTime(TotalRunningTimeSource.dateTo);

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("   [%s][6.1]totalRunningTime=%s", i, totalRunningTime));
                            }
                        } else {
                            long diff = now.getTime() - item.getReadyTime().getTime();
                            totalRunningTime += diff;
                            source.setTotalRunningTime(TotalRunningTimeSource.now);

                            if (isDebugEnabled) {
                                LOGGER.debug(String.format("   [%s][6.2]totalRunningTime=%s", i, totalRunningTime));
                            }
                        }
                    } else {
                        long diff = lastKnownTime.getTime() - item.getReadyTime().getTime();
                        totalRunningTime += diff;
                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("   [%s][6.3]totalRunningTime=%s", i, totalRunningTime));
                        }
                    }

                    AgentItemEntryItem entry = new AgentItemEntryItem();
                    entry.setReadyTime(item.getReadyTime());
                    entry.setLastKnownTime(lastKnownTime);
                    entry.setTotalRunningTime(totalRunningTime);
                    entry.setSource(source);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][%s][addEntry]%s", i, item.getAgentId(), SOSString.toString(entry)));
                    }

                    agent.getEntries().add(entry);
                }
                controller.getAgents().add(agent);
            }
            result.add(controller);
        }
        return result;
    }

    private Date tryToEstimateIfEndDateUnknown(DBItemHistoryAgent item, Date dateTo) {
        Date date = item.getReadyTime();
        try {
            if (item.getLastKnownTime().before(dateTo) && item.getLastKnownTime().after(item.getReadyTime())) {
                date = item.getLastKnownTime();
            }

            Date dateDayMax = SOSDate.getDateTime(SOSDate.getDateAsString(date) + " " + SOSDate.getTimeAsString(dateTo));
            Date datePlus = SOSDate.add(date, 1, ChronoUnit.HOURS);
            if (datePlus.before(dateDayMax)) {
                return datePlus;
            }
        } catch (Throwable e) {
        }
        return date;
    }

    private Map.Entry<String, Date> getAgentUrlAndModified(Map<String, Date> m) {
        if (m == null || m.size() == 0) {
            return null;
        }
        return m.entrySet().iterator().next();
    }

}
