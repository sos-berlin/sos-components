package com.sos.joc.agent.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agent.resource.ISubAgentCommandResource;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.items.SubAgentItem;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.SubAgentsCommand;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentRef;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agent")
public class SubAgentCommandResourceImpl extends JOCResourceImpl implements ISubAgentCommandResource {

    private static String API_CALL_REMOVE = "./agent/subagents/remove";

    @Override
    public JOCDefaultResponse remove(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_REMOVE, filterBytes, accessToken);

            // if (JocClusterService.getInstance().getCluster() != null && !JocClusterService.getInstance().getCluster().getConfig().getClusterMode()) {
            // throw new JocMissingLicenseException("missing license for Agent cluster");
            // }

            JsonValidator.validateFailFast(filterBytes, SubAgentsCommand.class);
            SubAgentsCommand subAgentCommand = Globals.objectMapper.readValue(filterBytes, SubAgentsCommand.class);

            String controllerId = subAgentCommand.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(subAgentCommand.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            JControllerProxy proxy = Proxy.of(controllerId);
            Map<SubagentId, JSubagentRef> subAgentsOnController = proxy.currentState().idToSubagentRef();

            Set<String> subAgentIdsOnController = subAgentsOnController.keySet().stream().map(SubagentId::string).filter(s -> subAgentCommand
                    .getSubagentIds().contains(s)).collect(Collectors.toSet());

            final Map<Boolean, List<String>> subAgentsMap = subAgentCommand.getSubagentIds().stream().collect(Collectors.groupingBy(
                    s -> subAgentIdsOnController.contains(s)));

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            
            if (subAgentsMap.get(false) != null) {
                dbLayer.deleteSubAgents(controllerId, subAgentsMap.get(false));
            }
            if (subAgentsMap.get(true) != null) {
                List<SubAgentItem> directors = dbLayer.getDirectorSubAgentIdsByControllerId(controllerId, subAgentsMap.get(true));

                directors.parallelStream().filter(SubAgentItem::isPrimaryDirector).findAny().ifPresent(s -> {
                    throw new JocBadRequestException("A primary director ('" + s.getSubAgentId()
                            + "') cannot be deleted. Change the primary director or remove the whole Agent cluster.");
                });

                final Stream<JUpdateItemOperation> updateAgentRef = directors.stream().collect(Collectors.groupingBy(SubAgentItem::getAgentId))
                        .values().stream().filter(l -> l.size() == 2).flatMap(l -> l.stream()).filter(SubAgentItem::isPrimaryDirector).map(
                                s -> JAgentRef.of(AgentPath.of(s.getAgentId()), SubagentId.of(s.getSubAgentId()))).map(
                                        JUpdateItemOperation::addOrChangeSimple);

                final Stream<JUpdateItemOperation> subAgents = subAgentCommand.getSubagentIds().stream().distinct().map(SubagentId::of).map(
                        JUpdateItemOperation::deleteSimple);

                proxy.api().updateItems(Flux.concat(Flux.fromStream(subAgents), Flux.fromStream(updateAgentRef))).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.deleteSubAgents(controllerId, subAgentsMap.get(true));
                            Globals.commit(connection1);
                        } catch (Exception e1) {
                            Globals.rollback(connection1);
                            ProblemHelper.postExceptionEventIfExist(Either.left(e1), accessToken, getJocError(), controllerId);
                        } finally {
                            Globals.disconnect(connection1);
                        }
                    }
                });
            }
            
            Globals.commit(connection);
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
