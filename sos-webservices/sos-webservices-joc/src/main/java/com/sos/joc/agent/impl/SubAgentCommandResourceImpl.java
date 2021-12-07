package com.sos.joc.agent.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agent.resource.ISubAgentCommandResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.AgentCommand;
import com.sos.joc.model.agent.SubAgentCommand;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.subagent.SubagentId;
import js7.data_for_java.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;

@Path("subagent")
public class SubAgentCommandResourceImpl extends JOCResourceImpl implements ISubAgentCommandResource {

    private static String API_CALL_REMOVE = "./subagent/remove";

    @Override
    public JOCDefaultResponse remove(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_REMOVE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, SubAgentCommand.class);
            SubAgentCommand subAgentCommand = Globals.objectMapper.readValue(filterBytes, SubAgentCommand.class);
            
            String controllerId = subAgentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(subAgentCommand.getAuditLog(), subAgentCommand.getControllerId(), CategoryType.CONTROLLER);
            
            JUpdateItemOperation subAgent = JUpdateItemOperation.deleteSimple(SubagentId.of(subAgentCommand.getSubagentId()));
            
            ControllerApi.of(controllerId).updateItems(Flux.just(subAgent)).thenAccept(e -> {
                ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                if (e.isRight()) {
                    SOSHibernateSession connection = null;
                    try {
                        connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
                        connection.setAutoCommit(false);
                        Globals.beginTransaction(connection);
                        InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
                        dbLayer.deleteSubAgent(subAgentCommand.getSubagentId());
                        Globals.commit(connection);
                    } catch (Exception e1) {
                        Globals.rollback(connection);
                        ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), controllerId);
                    } finally {
                        Globals.disconnect(connection);
                    }
                }
            });

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
