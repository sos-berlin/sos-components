package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterCommand;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.DeployClusterAgents;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.subagent.SubagentSelectionId;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agents")
public class SubAgentClusterCommandImpl extends JOCResourceImpl implements ISubAgentClusterCommand {

    private static final String API_CALL_DELETE = "./agents/cluster/delete";
    private static final String API_CALL_REVOKE = "./agents/cluster/revoke";
    
    @Override
    public JOCDefaultResponse revoke(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REVOKE, filterBytes, accessToken);

            AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, DeployClusterAgents.class);
            DeployClusterAgents agentParameter = Globals.objectMapper.readValue(filterBytes, DeployClusterAgents.class);

            String controllerId = agentParameter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(agentParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REVOKE);
            InventorySubagentClustersDBLayer dbLayer = new InventorySubagentClustersDBLayer(connection);
            Set<String> subagentClusterIds = agentParameter.getSubagentClusterIds();
            List<DBItemInventorySubAgentCluster> dbSubAgentClusters = dbLayer.getSubagentClusters(Collections.singleton(controllerId),
                    subagentClusterIds.stream().collect(Collectors.toList()));

            final List<String> dbSubagentClusterIds = dbSubAgentClusters.stream().map(DBItemInventorySubAgentCluster::getSubAgentClusterId).distinct()
                    .collect(Collectors.toList());
            
            final List<String> dbAgentIds = dbSubAgentClusters.stream().map(DBItemInventorySubAgentCluster::getAgentId).distinct().collect(Collectors
                    .toList());

            // check that controllerId corresponds to subagentClusterIds
            if (dbSubAgentClusters.size() != subagentClusterIds.size()) {
                subagentClusterIds.removeAll(dbSubagentClusterIds);
                throw new JocBadRequestException(String.format("The Subagent Clusters %s are not assigned to Controller '%s'", subagentClusterIds
                        .toString(), controllerId));
            }
            
            JControllerProxy proxy = Proxy.of(controllerId);
            final List<String> knownSubagentSelectionIds = proxy.currentState().idToSubagentSelection().keySet().stream().map(
                    SubagentSelectionId::string).collect(Collectors.toList());
            Predicate<String> knownInController = s -> knownSubagentSelectionIds.contains(s);
            List<JUpdateItemOperation> s = subagentClusterIds.stream().filter(knownInController).map(SubagentSelectionId::of).map(
                    JUpdateItemOperation::deleteSimple).collect(Collectors.toList());
            
            if (!s.isEmpty()) {
                proxy.api().updateItems(Flux.fromIterable(s)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL_REVOKE);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setSubAgentClustersDeployed(subagentClusterIds.stream().collect(Collectors.toList()), false);
                            Globals.commit(connection1);
                            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, dbAgentIds));
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

    @Override
    public JOCDefaultResponse delete(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_DELETE, filterBytes, accessToken);

            AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, DeployClusterAgents.class);
            DeployClusterAgents agentParameter = Globals.objectMapper.readValue(filterBytes, DeployClusterAgents.class);

            String controllerId = agentParameter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(agentParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventorySubagentClustersDBLayer dbLayer = new InventorySubagentClustersDBLayer(connection);
            Set<String> subagentClusterIds = agentParameter.getSubagentClusterIds();
            List<DBItemInventorySubAgentCluster> subAgentClusters = dbLayer.getSubagentClusters(Collections.singleton(controllerId),
                    subagentClusterIds.stream().collect(Collectors.toList()));

            final List<String> dbSubagentClusterIds = subAgentClusters.stream().map(DBItemInventorySubAgentCluster::getSubAgentClusterId).distinct()
                    .collect(Collectors.toList());
            final List<String> dbAgentIds = subAgentClusters.stream().map(DBItemInventorySubAgentCluster::getAgentId).distinct().collect(Collectors
                    .toList());

            // check that controllerId corresponds to subagentClusterIds
            if (subAgentClusters.size() != subagentClusterIds.size()) {
                subagentClusterIds.removeAll(dbSubagentClusterIds);
                throw new JocBadRequestException(String.format("The Subagent Clusters %s are not assigned to Controller '%s'", subagentClusterIds
                        .toString(), controllerId));
            }

            JControllerProxy proxy = Proxy.of(controllerId);
            final List<String> knownSubagentSelectionIds = proxy.currentState().idToSubagentSelection().keySet().stream().map(SubagentSelectionId::string).collect(Collectors.toList());
            Predicate<String> knownInController = s -> knownSubagentSelectionIds.contains(s);
            
            List<String> unknownSubagentSelectionIds = subagentClusterIds.stream().filter(knownInController.negate()).collect(Collectors.toList());
            if (!unknownSubagentSelectionIds.isEmpty()) {
                dbLayer.deleteSubAgentClusters(unknownSubagentSelectionIds);
                EventBus.getInstance().post(new AgentInventoryEvent(controllerId, dbAgentIds));
                Globals.commit(connection);
            }

            proxy.api().updateItems(Flux.fromStream(subagentClusterIds.stream().filter(knownInController).map(SubagentSelectionId::of).map(
                    JUpdateItemOperation::deleteSimple))).thenAccept(e -> {
                        ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                        if (e.isRight()) {
                            SOSHibernateSession connection1 = null;
                            try {
                                connection1 = Globals.createSosHibernateStatelessConnection(API_CALL_DELETE);
                                connection1.setAutoCommit(false);
                                Globals.beginTransaction(connection1);
                                InventorySubagentClustersDBLayer dbLayer1 = new InventorySubagentClustersDBLayer(connection1);
                                dbLayer1.deleteSubAgentClusters(dbSubagentClusterIds);
                                Globals.commit(connection1);
                                EventBus.getInstance().post(new AgentInventoryEvent(controllerId, dbAgentIds));
                            } catch (Exception e1) {
                                Globals.rollback(connection1);
                                ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), controllerId);
                            } finally {
                                Globals.disconnect(connection1);
                            }
                        }
                    });

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            Globals.rollback(connection);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(connection);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}
