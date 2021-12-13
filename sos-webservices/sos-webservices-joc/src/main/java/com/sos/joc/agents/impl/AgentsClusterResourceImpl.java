package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsClusterResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingLicenseException;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.ClusterAgents;
import com.sos.joc.model.agent.DeployClusterAgent;
import com.sos.joc.model.agent.DeployClusterAgents;
import com.sos.joc.model.agent.ReadAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.schema.JsonValidator;

import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentRef;
import reactor.core.publisher.Flux;

@Path("agents/cluster")
public class AgentsClusterResourceImpl extends JOCResourceImpl implements IAgentsClusterResource {

    private static String API_CALL_P = "./agents/cluster/p";
    private static String API_CALL_DEPLOY = "./agents/cluster/deploy";

    @Override
    public JOCDefaultResponse postCluster(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_P, filterBytes, accessToken);
            
            if (JocClusterService.getInstance().getCluster() != null && !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
                ClusterAgents agents = new ClusterAgents();
                agents.setDeliveryDate(Date.from(Instant.now()));
                
                return JOCDefaultResponse.responseStatus200(agents);
            }
            
            JsonValidator.validateFailFast(filterBytes, ReadAgents.class);
            ReadAgents agentParameter = Globals.objectMapper.readValue(filterBytes, ReadAgents.class);
            
            String controllerId = agentParameter.getControllerId();
            Set<String> allowedControllers = Collections.emptySet();
            boolean permitted = false;
            if (controllerId == null || controllerId.isEmpty()) {
                controllerId = "";
                allowedControllers = Proxies.getControllerDbInstances().keySet().stream().filter(availableController -> getControllerPermissions(
                        availableController, accessToken).getAgents().getView() || getJocPermissions(accessToken).getAdministration().getControllers()
                                .getView()).collect(Collectors.toSet());
                permitted = !allowedControllers.isEmpty();
                if (allowedControllers.size() == Proxies.getControllerDbInstances().keySet().size()) {
                    allowedControllers = Collections.emptySet();
                }
            } else {
                allowedControllers = Collections.singleton(controllerId);
                permitted = getControllerPermissions(controllerId, accessToken).getAgents().getView() || getJocPermissions(accessToken)
                        .getAdministration().getControllers().getView();
            }
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permitted);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_P);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(allowedControllers, agentParameter
                    .getAgentIds(), false, agentParameter.getOnlyEnabledAgents());
            Map<String, List<DBItemInventorySubAgentInstance>> subAgents = dbLayer.getSubAgentInstancesByControllerIds(allowedControllers, false,
                    agentParameter.getOnlyEnabledAgents());
            ClusterAgents agents = new ClusterAgents();
            if (dbAgents != null) {
                Set<String> controllerIds = dbAgents.stream().map(DBItemInventoryAgentInstance::getControllerId).collect(Collectors.toSet());
                Map<String, Set<String>> allAliases = dbLayer.getAgentNamesByAgentIds(controllerIds);
                agents.setAgents(dbAgents.stream().map(a -> {
                    if (!subAgents.containsKey(a.getAgentId())) { // solo agent
                        return null;
                    }
                    ClusterAgent agent = new ClusterAgent();
                    agent.setAgentId(a.getAgentId());
                    agent.setAgentName(a.getAgentName());
                    agent.setAgentNameAliases(allAliases.get(a.getAgentId()));
                    agent.setDisabled(a.getDisabled());
                    agent.setControllerId(a.getControllerId());
                    agent.setUrl(a.getUri());
                    agent.setSubagents(mapDBSubAgentsToSubAgents(subAgents.get(a.getAgentId())));
                    return agent;
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }
            agents.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(agents);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    @Override
    public JOCDefaultResponse postDeploy(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_DEPLOY, filterBytes, accessToken);
            
            if (JocClusterService.getInstance().getCluster() != null && !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
                throw new JocMissingLicenseException("missing license for Agent cluster");
            }
            
            JsonValidator.validateFailFast(filterBytes, DeployClusterAgents.class);
            DeployClusterAgents agentParameter = Globals.objectMapper.readValue(filterBytes, DeployClusterAgents.class);
            
            String controllerId = agentParameter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_DEPLOY);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<SubagentDirectorType> directorTypes = Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR, SubagentDirectorType.SECONDARY_DIRECTOR);
            List<JUpdateItemOperation> updateItems = new ArrayList<>();
            
            Set<DeployClusterAgent> agents = agentParameter.getClusterAgents();
            for (DeployClusterAgent agent : agents) {
                List<DBItemInventorySubAgentInstance> subAgents = dbLayer.getSubAgentInstancesByAgentId(agent.getAgentId());
                if (subAgents.isEmpty()) {
                    throw new JocBadRequestException("Agent Cluster '" + agent.getAgentId() + "' doesn't have Subagents");
                }
                if (subAgents.stream().noneMatch(s -> s.getIsDirector() == SubagentDirectorType.PRIMARY_DIRECTOR.intValue())) {
                    throw new JocBadRequestException("Agent Cluster '" + agent.getAgentId() + "' doesn't have a primary director");
                }
                
                AgentPath agentPath = AgentPath.of(agent.getAgentId());
                List<SubagentId> directors = subAgents.stream().filter(s -> directorTypes.contains(s.getDirectorAsEnum())).sorted(Comparator
                        .comparingInt(DBItemInventorySubAgentInstance::getIsDirector)).map(DBItemInventorySubAgentInstance::getSubAgentId).map(
                                SubagentId::of).collect(Collectors.toList());
                updateItems.add(JUpdateItemOperation.addOrChangeSimple(JAgentRef.of(agentPath, directors)));
                
                switch (agent.getSchedulingType()) {
                case FIXED_PRIORITY:
                    Function<DBItemInventorySubAgentInstance, JSubagentRef> mapperFix = s -> JSubagentRef.of(SubagentId.of(s.getSubAgentId()),
                            agentPath, Uri.of(s.getUri()), Optional.of(-1 * s.getOrdering()));
                    updateItems.addAll(subAgents.stream().map(mapperFix).map(JUpdateItemOperation::addOrChangeSimple).collect(Collectors.toList()));
                    break;
                case ROUND_ROBIN:
                    Function<DBItemInventorySubAgentInstance, JSubagentRef> mapperRound = s -> JSubagentRef.of(SubagentId.of(s.getSubAgentId()),
                            agentPath, Uri.of(s.getUri()), Optional.of(0));
                    updateItems.addAll(subAgents.stream().map(mapperRound).map(JUpdateItemOperation::addOrChangeSimple).collect(Collectors.toList()));
                    break;
                }
            }
            
            if (!updateItems.isEmpty()) {
                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(updateItems)).thenAccept(e -> ProblemHelper.postProblemEventIfExist(e,
                        accessToken, getJocError(), controllerId));
            }
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static List<SubAgent> mapDBSubAgentsToSubAgents(List<DBItemInventorySubAgentInstance> dbSubagents) {
        if (dbSubagents == null) {
            return null;
        }
        dbSubagents.sort(Comparator.comparingInt(DBItemInventorySubAgentInstance::getOrdering));
        int index = 0;
        for (DBItemInventorySubAgentInstance item : dbSubagents) {
            item.setOrdering(++index);
        }
        return dbSubagents.stream().map(dbSubagent -> {
            SubAgent subagent = new SubAgent();
            subagent.setSubagentId(dbSubagent.getSubAgentId());
            subagent.setUrl(dbSubagent.getUri());
            subagent.setIsDirector(dbSubagent.getDirectorAsEnum());
            subagent.setIsClusterWatcher(dbSubagent.getIsWatcher());
            subagent.setPosition(dbSubagent.getOrdering());
            return subagent;
        }).collect(Collectors.toList());
    }
}
