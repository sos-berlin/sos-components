package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterDeploy;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingLicenseException;
import com.sos.joc.model.agent.DeployClusterAgents;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data.subagent.SubagentSelectionId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;
import js7.data_for_java.subagent.JSubagentSelection;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agents")
public class SubAgentClusterDeployImpl extends JOCResourceImpl implements ISubAgentClusterDeploy {

    private static final String API_CALL = "./agents/cluster/deploy";

    @Override
    public JOCDefaultResponse postDeploy(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            
            if (JocClusterService.getInstance().getCluster() == null || !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
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
            
            storeAuditLog(agentParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventorySubagentClustersDBLayer dbLayer = new InventorySubagentClustersDBLayer(connection);
            Set<String> subagentClusterIds = agentParameter.getSubagentClusterIds();
            Map<DBItemInventorySubAgentCluster, List<SubAgentId>> subAgentClusters = dbLayer.getSubagentClusters(Collections.singleton(controllerId),
                    null, subagentClusterIds.stream().collect(Collectors.toList()));
            
            // check that controllerId corresponds to subagentClusterIds
            if (subAgentClusters.size() != subagentClusterIds.size()) {
                Set<String> a = subAgentClusters.keySet().stream().map(DBItemInventorySubAgentCluster::getSubAgentClusterId).collect(Collectors
                        .toSet());
                subagentClusterIds.removeAll(a);
                throw new JocBadRequestException(String.format("The Subagent Clusters %s are not assigned to Controller '%s'", subagentClusterIds
                        .toString(), controllerId));
            }
            
            JControllerProxy proxy = Proxy.of(controllerId);
            JControllerState currentState = proxy.currentState();
            
            List<SubagentDirectorType> directorTypes = Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR, SubagentDirectorType.SECONDARY_DIRECTOR);
            List<JUpdateItemOperation> updateItems = new ArrayList<>();
            List<String> updateAgentIds = new ArrayList<>();
            List<String> subagentIdsAtController = currentState.idToSubagentItem().keySet().stream().map(SubagentId::string).collect(Collectors.toList());
            List<String> updateSubagentIds = subAgentClusters.values().stream().flatMap(List::stream).map(SubAgentId::getSubagentId).distinct()
                    .collect(Collectors.toList());
            Map<AgentPath, JAgentRef> knownAgents = currentState.pathToAgentRef();

            for (Map.Entry<DBItemInventorySubAgentCluster, List<SubAgentId>> subAgentCluster : subAgentClusters.entrySet()) {
                DBItemInventorySubAgentCluster key = subAgentCluster.getKey();
                
                JSubagentSelection selection = JSubagentSelection.of(SubagentSelectionId.of(key.getSubAgentClusterId()),
                        subAgentCluster.getValue().stream().collect(Collectors.toMap(s -> SubagentId.of(s.getSubagentId()),
                                SubAgentId::getPriority)));
                
                if (!updateAgentIds.contains(key.getAgentId()) && knownAgents.get(AgentPath.of(key.getAgentId())) != null) {
                    updateAgentIds.add(key.getAgentId());
                }
                
                updateItems.add(JUpdateItemOperation.addOrChangeSimple(selection));
            }
            
            if (updateAgentIds != null && !updateAgentIds.isEmpty()) {
                InventoryAgentInstancesDBLayer dbLayer2 = new InventoryAgentInstancesDBLayer(connection);
                for (String agentId : updateAgentIds) {
                    List<DBItemInventorySubAgentInstance> subAgents = dbLayer2.getSubAgentInstancesByAgentId(agentId);
                    if (subAgents.isEmpty()) {
                        throw new JocBadRequestException("Agent Cluster '" + agentId + "' doesn't have Subagents");
                    }
                    if (subAgents.stream().noneMatch(s -> s.getIsDirector() == SubagentDirectorType.PRIMARY_DIRECTOR.intValue())) {
                        throw new JocBadRequestException("Agent Cluster '" + agentId + "' doesn't have a primary director");
                    }

                    AgentPath agentPath = AgentPath.of(agentId);
                    List<SubagentId> directors = subAgents.stream().filter(s -> directorTypes.contains(s.getDirectorAsEnum())).sorted(Comparator
                            .comparingInt(DBItemInventorySubAgentInstance::getIsDirector)).map(DBItemInventorySubAgentInstance::getSubAgentId).map(
                                    SubagentId::of).collect(Collectors.toList());
                    updateItems.add(JUpdateItemOperation.addOrChangeSimple(JAgentRef.of(agentPath, directors)));
                    updateSubagentIds.addAll(directors.stream().map(SubagentId::string).collect(Collectors.toList()));
                }
            }
            
            // remove all controller known subagents
            updateSubagentIds.removeAll(subagentIdsAtController);
            
            if (updateSubagentIds != null) {
                InventoryAgentInstancesDBLayer dbLayer2 = new InventoryAgentInstancesDBLayer(connection);
                List<DBItemInventorySubAgentInstance> subAgents = dbLayer2.getSubAgentInstancesByControllerIds(Collections.singleton(controllerId));
                updateItems.addAll(subAgents.stream().filter(s -> updateSubagentIds.contains(s.getSubAgentId())).map(s -> JSubagentItem.of(SubagentId
                        .of(s.getSubAgentId()), AgentPath.of(s.getAgentId()), Uri.of(s.getUri()), s.getDisabled())).map(
                                JUpdateItemOperation::addOrChangeSimple).collect(Collectors.toList()));
            }
            
            if (!updateItems.isEmpty()) {
                proxy.api().updateItems(Flux.fromIterable(updateItems)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setSubAgentsDeployed(updateSubagentIds);
                            dbLayer1.setAgentsDeployed(updateAgentIds);
                            dbLayer1.setSubAgentClustersDeployed(subagentClusterIds.stream().collect(Collectors.toList()));
                            Globals.commit(connection1);
                            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, updateAgentIds));
                        } catch (Exception e1) {
                            Globals.rollback(connection1);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), controllerId);
                        } finally {
                            Globals.disconnect(connection1);
                        }
                    }
                });
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
}
