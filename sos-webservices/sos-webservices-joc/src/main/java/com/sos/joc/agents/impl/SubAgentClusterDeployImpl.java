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

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterDeploy;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.DeploySubagentClusters;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentBundleId;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentBundle;
import js7.data_for_java.subagent.JSubagentItem;
import js7.data_for_java.value.JExpression;
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
            
            AgentHelper.throwJocMissingLicenseException();
            
            JsonValidator.validateFailFast(filterBytes, DeploySubagentClusters.class);
            DeploySubagentClusters agentParameter = Globals.objectMapper.readValue(filterBytes, DeploySubagentClusters.class);
            
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
                subAgentClusters.keySet().stream().map(DBItemInventorySubAgentCluster::getSubAgentClusterId).forEach(a -> subagentClusterIds.remove(
                        a));
                throw new JocBadRequestException(String.format("The Subagent Clusters %s are not assigned to Controller '%s'", subagentClusterIds
                        .toString(), controllerId));
            }
            
            subAgentClusters.entrySet().stream().filter(e -> e.getValue().isEmpty()).findAny().map(Map.Entry::getKey).map(
                    DBItemInventorySubAgentCluster::getSubAgentClusterId).ifPresent(id -> {
                        throw new JocBadRequestException(String.format("The Subagent Cluster '%s' doesn't contain any Subagents", id));
                    });
            
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
                
//                JSubagentBundle selection = JSubagentBundle.of(SubagentBundleId.of(key.getSubAgentClusterId()),
//                        subAgentCluster.getValue().stream().collect(Collectors.toMap(s -> SubagentId.of(s.getSubagentId()),
//                                SubAgentId::getPriority)));
                
                JSubagentBundle selection = JSubagentBundle.of(SubagentBundleId.of(key.getSubAgentClusterId()),
                        subAgentCluster.getValue().stream().collect(Collectors.toMap(s -> SubagentId.of(s.getSubagentId()),
                                s -> JExpression.apply(s.getPriority()))));
                
                // if the cluster agent of the subagent cluster is unknown in Controller then deploy the cluster agent too
                if (!updateAgentIds.contains(key.getAgentId()) && knownAgents.get(AgentPath.of(key.getAgentId())) == null) {
                    updateAgentIds.add(key.getAgentId());
                }
                
                updateItems.add(JUpdateItemOperation.addOrChangeSimple(selection));
            }
            
            if (updateAgentIds != null && !updateAgentIds.isEmpty()) {
                InventoryAgentInstancesDBLayer dbLayer2 = new InventoryAgentInstancesDBLayer(connection);
                for (String agentId : updateAgentIds) {
                    DBItemInventoryAgentInstance dbAgent = dbLayer2.getAgentInstance(agentId);
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
                    updateItems.add(JUpdateItemOperation.addOrChangeSimple(JAgentRef.of(agentPath, directors, AgentHelper.getProcessLimit(dbAgent
                            .getProcessLimit()))));
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
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), null);
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
                            ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), null);
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
