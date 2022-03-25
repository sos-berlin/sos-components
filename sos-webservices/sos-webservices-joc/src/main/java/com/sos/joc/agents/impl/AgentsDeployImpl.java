package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsDeploy;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.DeployAgents;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agents")
public class AgentsDeployImpl extends JOCResourceImpl implements IAgentsDeploy {

    private static final String API_CALL = "./agents/inventory/deploy";

    @Override
    public JOCDefaultResponse postDeploy(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeployAgents.class);
            DeployAgents agentDeployParameter = Globals.objectMapper.readValue(filterBytes, DeployAgents.class);
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            String controllerId = agentDeployParameter.getControllerId();
            List<String> agentIds = agentDeployParameter.getAgentIds();

            storeAuditLog(agentDeployParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = agentDBLayer.getAgentsByControllerIdAndAgentIds(Collections.singleton(controllerId),
                    agentIds, false, false);
            
            if (dbAgents != null) {
                agentIds.removeAll(dbAgents.stream().map(DBItemInventoryAgentInstance::getAgentId).collect(Collectors.toList()));
            }
            if (agentIds.size() > 0) {
                throw new JocBadRequestException(String.format("The Agents %s are not assigned to Controller '%s'", agentIds.toString(),
                        controllerId));
            }
            
            List<JUpdateItemOperation> agentRefs = new ArrayList<>();
            List<String> updateAgentIds = new ArrayList<>();
            
            JControllerProxy proxy = Proxy.of(controllerId);
            JControllerState currentState = proxy.currentState();

            if (dbAgents != null) {
                Map<AgentPath, JAgentRef> knownAgents = currentState.pathToAgentRef();
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    JAgentRef agentRef = knownAgents.get(AgentPath.of(dbAgent.getAgentId()));
                    if (agentRef != null && (!agentRef.director().isPresent() || agentRef.directors().isEmpty())) {
                        agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createOldAgent(dbAgent)));
                    } else {
                        agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createNewAgent(dbAgent)));
                        agentRefs.add(JUpdateItemOperation.addOrChangeSimple(createSubagentDirector(dbAgent)));
                    }
                    updateAgentIds.add(dbAgent.getAgentId());
                }
            }

            if (!agentRefs.isEmpty()) {
                proxy.api().updateItems(Flux.fromIterable(agentRefs)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setAgentsDeployed(updateAgentIds);
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
    
    private static JAgentRef createOldAgent(DBItemInventoryAgentInstance a) {
        return JAgentRef.of(AgentPath.of(a.getAgentId()), Uri.of(a.getUri()));
    }
    
    private static JAgentRef createNewAgent(DBItemInventoryAgentInstance a) {
        return JAgentRef.of(AgentPath.of(a.getAgentId()), SubagentId.of((a.getAgentId())));
    }
    
    private static JSubagentItem createSubagentDirector(DBItemInventoryAgentInstance a) {
        return JSubagentItem.of(SubagentId.of(a.getAgentId()), AgentPath.of(a.getAgentId()), Uri.of(a.getUri()), true);
    }
}
