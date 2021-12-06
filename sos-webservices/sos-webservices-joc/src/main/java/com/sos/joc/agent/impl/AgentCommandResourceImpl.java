package com.sos.joc.agent.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agent.resource.IAgentCommandResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.AgentCommand;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.agent.AgentPath;
import js7.data.controller.ControllerCommand;
import js7.data.subagent.SubagentId;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;

@Path("agent")
public class AgentCommandResourceImpl extends JOCResourceImpl implements IAgentCommandResource {

    private static String API_CALL_RESET = "./agent/reset";
    private static String API_CALL_REMOVE = "./agent/remove";
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentCommandResourceImpl.class);

    @Override
    public JOCDefaultResponse reset(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RESET, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, AgentCommand.class);
            AgentCommand agentCommand = Globals.objectMapper.readValue(filterBytes, AgentCommand.class);
            
            String controllerId = agentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(agentCommand.getAuditLog(), agentCommand.getControllerId(), CategoryType.CONTROLLER);
            JControllerCommand resetAgentCommand = JControllerCommand.apply(new ControllerCommand.ResetAgent(AgentPath.of(agentCommand
                    .getAgentId()), agentCommand.getForce() == Boolean.TRUE));
            LOGGER.debug("Reset Agent: " + resetAgentCommand.toJson());
            ControllerApi.of(controllerId).executeCommand(resetAgentCommand).thenAccept(e -> {
                ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
            });

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse remove(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REMOVE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, AgentCommand.class);
            AgentCommand agentCommand = Globals.objectMapper.readValue(filterBytes, AgentCommand.class);
            
            String controllerId = agentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(agentCommand.getAuditLog(), agentCommand.getControllerId(), CategoryType.CONTROLLER);
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            
            Stream<JUpdateItemOperation> subAgents = dbLayer.getSubAgentIdsByAgentId(agentCommand.getAgentId()).stream().map(SubagentId::of).map(
                    JUpdateItemOperation::deleteSimple);
            JUpdateItemOperation agent = JUpdateItemOperation.deleteSimple(AgentPath.of(agentCommand.getAgentId()));
            
            Globals.disconnect(connection);
            connection = null;

            ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.just(agent), Flux.fromStream(subAgents))).thenAccept(e -> {
                ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                if (e.isRight()) {
                    SOSHibernateSession connection1 = null;
                    try {
                        connection1 = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
                        connection1.setAutoCommit(false);
                        Globals.beginTransaction(connection1);
                        InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                        List<DBItemInventoryAgentInstance> dbAgents = dbLayer1.getAgentsByControllerIdAndAgentIds(Collections.singleton(
                                controllerId), Collections.singleton(agentCommand.getAgentId()), false, false);
                        if (dbAgents != null && !dbAgents.isEmpty()) {
                            dbLayer1.deleteInstance(dbAgents.get(0));
                        }
                        Globals.commit(connection1);
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
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
}
