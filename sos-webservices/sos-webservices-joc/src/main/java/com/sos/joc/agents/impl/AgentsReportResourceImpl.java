package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsReportResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.db.history.items.JobsPerAgent;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.AgentReport;
import com.sos.joc.model.agent.AgentReportFilter;
import com.sos.joc.model.agent.AgentReports;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class AgentsReportResourceImpl extends JOCResourceImpl implements IAgentsReportResource {

    private static final String API_CALL = "./agents/report";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsReportResourceImpl.class);

    @Override
    public JOCDefaultResponse post(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, AgentReportFilter.class);
            AgentReportFilter agentParameter = Globals.objectMapper.readValue(filterBytes, AgentReportFilter.class);
            
            String controllerId = agentParameter.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                if (Proxies.getControllerDbInstances().isEmpty()) {
                    permitted = getBasicControllerDefaultPermissions(accessToken).getAgents().getView();
                } else {
                    allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getBasicControllerPermissions(
                            availableController, accessToken).getAgents().getView()).collect(Collectors.toSet());
                    permitted = !allowedControllers.isEmpty();
                    if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                        allowedControllers = Collections.emptySet();
                    }
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getBasicControllerPermissions(controllerId, accessToken).getAgents().getView();
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            AgentReports agentReports = new AgentReports();
            if (Proxies.getControllerDbInstances().isEmpty()) {
                agentReports.setAgents(Collections.emptyList());
                agentReports.setTotalNumOfJobs(0L);
                agentReports.setTotalNumOfSuccessfulTasks(0L);
                agentReports.setDeliveryDate(Date.from(Instant.now()));
                JocError jocError = getJocError();
                if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                    LOGGER.info(jocError.printMetaInfo());
                    jocError.clearMetaInfo();
                }
                LOGGER.warn(InventoryInstancesDBLayer.noRegisteredControllers());
                return JOCDefaultResponse.responseStatus200(agentReports);
            }
            
            boolean withAgentFilter = (agentParameter.getAgentIds() != null && !agentParameter.getAgentIds().isEmpty()) || (agentParameter
                    .getUrls() != null && !agentParameter.getUrls().isEmpty());
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIdsAndUrls(allowedControllers, agentParameter
                    .getAgentIds(), agentParameter.getUrls(), true);
            if (dbAgents != null) {
                
                HistoryFilter dbFilter = new HistoryFilter();
                dbFilter.setControllerIds(allowedControllers);
                
                if (withAgentFilter) {
                    dbFilter.setAgentIds(dbAgents.stream().map(DBItemInventoryAgentInstance::getAgentId).collect(Collectors.toSet()));
                }
                
                if (agentParameter.getDateFrom() != null) {
                    dbFilter.setExecutedFrom(JobSchedulerDate.getDateFrom(agentParameter.getDateFrom(), agentParameter.getTimeZone()));
                }
                if (agentParameter.getDateTo() != null) {
                    dbFilter.setExecutedTo(JobSchedulerDate.getDateTo(agentParameter.getDateTo(), agentParameter.getTimeZone()));
                }
                
                JobHistoryDBLayer dbHistoryLayer = new JobHistoryDBLayer(connection, dbFilter);
                Map<String, List<JobsPerAgent>> jobsPerAgents = dbHistoryLayer.getCountJobs();
                
                List<AgentReport> agents = new ArrayList<>();
                Long totalNumOfJobs = 0L;
                Long totalNumOfSuccessfulTasks = 0L;
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    Long numOfJobs = 0L;
                    Long numOfSuccessfulTasks = 0L;
                    if (jobsPerAgents.containsKey(dbAgent.getAgentId())) {
                        List<JobsPerAgent> jobs = jobsPerAgents.get(dbAgent.getAgentId());
                        for (JobsPerAgent job : jobs) {
                            numOfJobs += job.getCount();
                            if (!job.getError()) {
                                numOfSuccessfulTasks += job.getCount();
                            }
                        }
                    }
                    AgentReport a = new AgentReport();
                    a.setControllerId(dbAgent.getControllerId());
                    a.setAgentId(dbAgent.getAgentId());
                    a.setUrl(dbAgent.getUri());
                    a.setNumOfJobs(numOfJobs);
                    a.setNumOfSuccessfulTasks(numOfSuccessfulTasks);
                    agents.add(a);
                    totalNumOfJobs += numOfJobs;
                    totalNumOfSuccessfulTasks += numOfSuccessfulTasks;
                }
                agentReports.setAgents(agents);
                agentReports.setTotalNumOfJobs(totalNumOfJobs);
                agentReports.setTotalNumOfSuccessfulTasks(totalNumOfSuccessfulTasks);
            }
            agentReports.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(agentReports);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
}
