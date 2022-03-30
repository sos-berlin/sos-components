package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsClusterCommand;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.DeployClusterAgents;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;

@Path("agents")
public class AgentsClusterCommandImpl extends JOCResourceImpl implements IAgentsClusterCommand {

    private static final String API_CALL = "./agents/inventory/cluster/revoke";

    @Override
    public JOCDefaultResponse postRevoke(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            
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
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<JUpdateItemOperation> deleteItems = new ArrayList<>();
            List<String> deleteAgentIds = new ArrayList<>();
            List<String> deleteSubagentIds = new ArrayList<>();
            
            Set<String> agentIds = agentParameter.getClusterAgentIds();
            if (agentIds != null) {
                for (String agentId : agentIds) {
                    List<DBItemInventorySubAgentInstance> subAgents = dbLayer.getSubAgentInstancesByAgentId(agentId);

                    deleteItems.add(JUpdateItemOperation.deleteSimple(AgentPath.of(agentId)));
                    deleteAgentIds.add(agentId);

                    deleteItems.addAll(subAgents.stream().map(s -> SubagentId.of(s.getSubAgentId())).map(JUpdateItemOperation::deleteSimple).collect(
                            Collectors.toList()));
                    deleteSubagentIds.addAll(subAgents.stream().map(DBItemInventorySubAgentInstance::getSubAgentId).collect(Collectors.toList()));
                }
            }
            
            if (!deleteItems.isEmpty()) {
                ControllerApi.of(controllerId).updateItems(Flux.fromIterable(deleteItems)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setSubAgentsDeployed(deleteSubagentIds, false);
                            dbLayer1.setAgentsDeployed(deleteAgentIds, false);
                            Globals.commit(connection1);
                            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, deleteAgentIds));
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
