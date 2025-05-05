package com.sos.joc.agents.impl;


import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;

import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsClusterDeploy;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.DeployClusterAgents;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;

import reactor.core.publisher.Flux;

@Path("agents")
public class AgentsClusterDeployImpl extends JOCResourceImpl implements IAgentsClusterDeploy {

    private static final String API_CALL = "./agents/inventory/cluster/deploy";

    @Override
    public JOCDefaultResponse postDeploy(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            
            AgentHelper.throwJocMissingLicenseException();
            
            JsonValidator.validateFailFast(filterBytes, DeployClusterAgents.class);
            DeployClusterAgents agentParameter = Globals.objectMapper.readValue(filterBytes, DeployClusterAgents.class);
            
            String controllerId = agentParameter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getControllers()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(agentParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<SubagentDirectorType> directorTypes = Arrays.asList(SubagentDirectorType.PRIMARY_DIRECTOR, SubagentDirectorType.SECONDARY_DIRECTOR);
            List<JUpdateItemOperation> updateItems = new ArrayList<>();
            List<String> updateAgentIds = new ArrayList<>();
            List<String> updateSubagentIds = new ArrayList<>();

            
            Set<String> agentIds = agentParameter.getClusterAgentIds();
            if (agentIds != null) {
                for (String agentId : agentIds) {
                    DBItemInventoryAgentInstance dbAgent = dbLayer.getAgentInstance(agentId);
                    List<DBItemInventorySubAgentInstance> subAgents = dbLayer.getSubAgentInstancesByAgentId(agentId);
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
                    updateAgentIds.add(agentId);

                    updateItems.addAll(subAgents.stream().map(s -> JSubagentItem.of(SubagentId.of(s.getSubAgentId()), agentPath, Uri.of(s.getUri()), s
                            .getDisabled())).map(JUpdateItemOperation::addOrChangeSimple).collect(Collectors.toList()));
                    updateSubagentIds.addAll(subAgents.stream().map(DBItemInventorySubAgentInstance::getSubAgentId).collect(Collectors.toList()));



                }

            }
            
            if (!updateItems.isEmpty()) {
                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(updateItems)).thenAccept(e -> {
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