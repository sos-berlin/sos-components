package com.sos.joc.agent.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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
                    .getAgentId())));
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
            
            //JControllerProxy proxy = Proxy.of(controllerId);
            AgentPath agent = AgentPath.of(agentCommand.getAgentId());
            
            //Either<Problem, JAgentRefState> either = proxy.currentState().pathToAgentRefState(agent);
            //ProblemHelper.throwProblemIfExist(either);
            //AgentRefState.CouplingState couplingState = either.get().asScala().couplingState();
            
            //if (couplingState instanceof AgentRefState.Coupled$) {
                
                JUpdateItemOperation op = JUpdateItemOperation.deleteSimple(agent);
                ControllerApi.of(controllerId).updateItems(Flux.just(op)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection = null;
                        try {
                            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
                            connection.setAutoCommit(false);
                            Globals.beginTransaction(connection);
                            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
                            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIdsAndUrls(Collections.singleton(
                                    controllerId), Collections.singleton(agentCommand.getAgentId()), null, false, false);
                            if (dbAgents != null && !dbAgents.isEmpty()) {
                                dbLayer.deleteInstance(dbAgents.get(0));
                            }
                            Globals.commit(connection);
                        } catch (Exception e1) {
                            Globals.rollback(connection);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), controllerId);
                        } finally {
                            Globals.disconnect(connection);
                        }
                    }
                });
//            } else {
//                throw new ControllerConflictException("Agent has to be coupled");
//            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
