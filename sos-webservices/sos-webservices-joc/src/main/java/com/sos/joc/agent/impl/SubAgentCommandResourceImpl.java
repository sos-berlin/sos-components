package com.sos.joc.agent.impl;

import java.time.Instant;
import java.util.Date;
import java.util.stream.Stream;

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
import com.sos.joc.model.agent.SubAgentsCommand;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.subagent.SubagentId;
import js7.data_for_java.item.JUpdateItemOperation;
import reactor.core.publisher.Flux;

@Path("agent")
public class SubAgentCommandResourceImpl extends JOCResourceImpl implements ISubAgentCommandResource {

    private static String API_CALL_REMOVE = "./agent/subagents/remove";

    @Override
    public JOCDefaultResponse remove(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_REMOVE, filterBytes, accessToken);
            
//            if (JocClusterService.getInstance().getCluster() != null && !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
//                throw new JocMissingLicenseException("missing license for Agent cluster");
//            }
            
            JsonValidator.validateFailFast(filterBytes, SubAgentsCommand.class);
            SubAgentsCommand subAgentCommand = Globals.objectMapper.readValue(filterBytes, SubAgentsCommand.class);
            
            String controllerId = subAgentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(subAgentCommand.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            Stream<JUpdateItemOperation> subAgents = subAgentCommand.getSubagentIds().stream().distinct().map(SubagentId::of).map(
                    JUpdateItemOperation::deleteSimple);
            
            ControllerApi.of(controllerId).updateItems(Flux.fromStream(subAgents)).thenAccept(e -> {
                ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                if (e.isRight()) {
                    SOSHibernateSession connection = null;
                    try {
                        connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
                        connection.setAutoCommit(false);
                        Globals.beginTransaction(connection);
                        InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
                        dbLayer.deleteSubAgents(subAgentCommand.getSubagentIds());
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
