package com.sos.joc.agents.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.apache.shiro.session.InvalidSessionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentCommand;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.items.SubAgentItem;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.SubAgentCommand;
import com.sos.joc.model.agent.SubAgentsCommand;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.controller.ControllerCommand;
import js7.data.subagent.SubagentId;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agents")
public class SubAgentCommandImpl extends JOCResourceImpl implements ISubAgentCommand {

    private static final String API_CALL_REMOVE = "./agents/inventory/cluster/subagents/delete";
    private static final String API_CALL_ENABLE = "./agents/inventory/cluster/subagents/enable";
    private static final String API_CALL_DISABLE = "./agents/inventory/cluster/subagents/disable";
    private static final String API_CALL_REVOKE = "./agents/inventory/cluster/subagents/revoke";
    private static final String API_CALL_RESET = "./agents/inventory/cluster/subagent/reset";
    private static final Logger LOGGER = LoggerFactory.getLogger(SubAgentCommandImpl.class);

    @Override
    public JOCDefaultResponse delete(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_REMOVE, filterBytes, accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SubAgentsCommand subAgentCommand = getSubAgentsCommand(filterBytes);

            String controllerId = subAgentCommand.getControllerId();

            storeAuditLog(subAgentCommand.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REMOVE);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventorySubAgentInstance> dbSubAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(subAgentCommand
                    .getControllerId()));

            checkSubAgentsBelongToController(subAgentCommand, dbSubAgents);

            JControllerProxy proxy = Proxy.of(controllerId);
            final Map<Boolean, List<String>> subAgentsMap = getKnownSubAgentsOnController(subAgentCommand, proxy.currentState());

            if (subAgentsMap.containsKey(false)) {
                dbLayer.deleteSubAgents(controllerId, subAgentsMap.get(false));
                EventBus.getInstance().post(new AgentInventoryEvent(controllerId));
            }
            if (subAgentsMap.containsKey(true)) {
                List<SubAgentItem> directors = dbLayer.getDirectorSubAgentIdsByControllerId(controllerId, subAgentsMap.get(true));

                directors.parallelStream().filter(SubAgentItem::isPrimaryDirector).findAny().ifPresent(s -> {
                    throw new JocBadRequestException("A primary director ('" + s.getSubAgentId()
                            + "') cannot be deleted. Change the primary director or delete the whole Agent cluster.");
                });

                final Stream<JUpdateItemOperation> subAgents = subAgentsMap.get(true).stream().map(SubagentId::of).map(
                        JUpdateItemOperation::deleteSimple);
                
                // if secondary director will be deleted then change the AgentRef settings in that way that only the primary is specified.
                // and add to delete command
//                subAgents.addAll(directors.stream().collect(Collectors.groupingBy(SubAgentItem::getAgentId))
//                        .values().stream().filter(l -> l.size() == 2).flatMap(List::stream).filter(SubAgentItem::isPrimaryDirector).map(
//                                s -> JAgentRef.of(AgentPath.of(s.getAgentId()), SubagentId.of(s.getSubAgentId()))).map(
//                                        JUpdateItemOperation::addOrChangeSimple).collect(Collectors.toList()));

                proxy.api().updateItems(Flux.fromStream(subAgents)).thenAccept(e -> {
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
                            EventBus.getInstance().post(new AgentInventoryEvent(controllerId));
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
    
    @Override
    public JOCDefaultResponse revoke(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_REVOKE, filterBytes, accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SubAgentsCommand subAgentCommand = getSubAgentsCommand(filterBytes);

            String controllerId = subAgentCommand.getControllerId();

            storeAuditLog(subAgentCommand.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_REVOKE);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventorySubAgentInstance> dbSubAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(subAgentCommand
                    .getControllerId()));

            checkSubAgentsBelongToController(subAgentCommand, dbSubAgents);

            JControllerProxy proxy = Proxy.of(controllerId);
            final Map<Boolean, List<String>> subAgentsMap = getKnownSubAgentsOnController(subAgentCommand, proxy.currentState());

            if (subAgentsMap.containsKey(false)) {
                dbLayer.deleteSubAgents(controllerId, subAgentsMap.get(false));
                EventBus.getInstance().post(new AgentInventoryEvent(controllerId));
            }
            if (subAgentsMap.containsKey(true)) {
                List<SubAgentItem> directors = dbLayer.getDirectorSubAgentIdsByControllerId(controllerId, subAgentsMap.get(true));

                directors.parallelStream().filter(SubAgentItem::isPrimaryDirector).findAny().ifPresent(s -> {
                    throw new JocBadRequestException("A primary director ('" + s.getSubAgentId()
                            + "') cannot be revoked. Change the primary director or revoke the whole Agent cluster.");
                });

                final Stream<JUpdateItemOperation> subAgents = subAgentsMap.get(true).stream().map(SubagentId::of).map(
                        JUpdateItemOperation::deleteSimple);
                
                proxy.api().updateItems(Flux.fromStream(subAgents)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL_REVOKE);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setSubAgentsDeployed(subAgentsMap.get(true), false);
                            Globals.commit(connection1);
                            EventBus.getInstance().post(new AgentInventoryEvent(controllerId));
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
    
    @Override
    public JOCDefaultResponse reset(String accessToken, byte[] filterBytes) {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL_RESET, filterBytes, accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            JsonValidator.validateFailFast(filterBytes, SubAgentCommand.class);
            SubAgentCommand subAgentCommand = Globals.objectMapper.readValue(filterBytes, SubAgentCommand.class);

            String controllerId = subAgentCommand.getControllerId();

            storeAuditLog(subAgentCommand.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            JControllerCommand resetSubagentCommand = JControllerCommand.apply(new ControllerCommand.ResetSubagent(SubagentId.of(subAgentCommand
                    .getSubagentId()), subAgentCommand.getForce() == Boolean.TRUE));
            LOGGER.debug("Reset Subagent: " + resetSubagentCommand.toJson());
            ControllerApi.of(controllerId).executeCommand(resetSubagentCommand).thenAccept(e -> {
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
    public JOCDefaultResponse enable(String accessToken, byte[] filterBytes) {
        return disOrEnable(accessToken, filterBytes, false);
    }
    
    @Override
    public JOCDefaultResponse disable(String accessToken, byte[] filterBytes) {
        return disOrEnable(accessToken, filterBytes, true);
    }
    
    public JOCDefaultResponse disOrEnable(String accessToken, byte[] filterBytes, boolean disabled) {
        SOSHibernateSession connection = null;
        try {
            String apiCall = disabled ? API_CALL_DISABLE : API_CALL_ENABLE;
            JOCDefaultResponse jocDefaultResponse = init(apiCall, filterBytes, accessToken);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SubAgentsCommand subAgentCommand = getSubAgentsCommand(filterBytes);

            String controllerId = subAgentCommand.getControllerId();

            storeAuditLog(subAgentCommand.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(apiCall);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventorySubAgentInstance> dbSubAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(subAgentCommand
                    .getControllerId()));

            checkSubAgentsBelongToController(subAgentCommand, dbSubAgents);

            JControllerProxy proxy = Proxy.of(controllerId);
            final Map<Boolean, List<String>> subAgentsMap = getKnownSubAgentsOnController(subAgentCommand, proxy.currentState());

            if (subAgentsMap.containsKey(false)) {
                // dbLayer.setSubAgentsDisabled(subAgentsMap.get(false), disabled);
                // EventBus.getInstance().post(new AgentInventoryEvent(controllerId));
                ProblemHelper.postExceptionEventAsHintIfExist(Either.left(new ControllerObjectNotExistException("Subagents " + subAgentsMap.get(false)
                        .toString() + "not exist.")), accessToken, getJocError(), null);
            }
            if (subAgentsMap.containsKey(true)) {
                
                final List<String> subagentIds = subAgentsMap.get(true);
                final Stream<JUpdateItemOperation> subAgents = dbSubAgents.stream().filter(s -> subagentIds.contains(s.getSubAgentId())).map(s -> JSubagentItem.of(
                        SubagentId.of(s.getSubAgentId()), AgentPath.of(s.getAgentId()), Uri.of(s.getUri()), disabled)).map(
                                JUpdateItemOperation::addOrChangeSimple);

                proxy.api().updateItems(Flux.fromStream(subAgents)).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(apiCall);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setSubAgentsDisabled(subagentIds, disabled);
                            Globals.commit(connection1);
                            EventBus.getInstance().post(new AgentInventoryEvent(controllerId));
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
    
    private JOCDefaultResponse init(String apiCall, byte[] filterBytes, String accessToken) throws InvalidSessionException, JsonParseException,
            JsonMappingException, JocException, IOException {
        initLogging(apiCall, filterBytes, accessToken);

        AgentHelper.throwJocMissingLicenseException();

        return initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers().getManage());
    }
    
    private static SubAgentsCommand getSubAgentsCommand(byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        JsonValidator.validateFailFast(filterBytes, SubAgentsCommand.class);
        return Globals.objectMapper.readValue(filterBytes, SubAgentsCommand.class);
    }
    
    private static void checkSubAgentsBelongToController(SubAgentsCommand subAgentCommand, List<DBItemInventorySubAgentInstance> dbSubAgents) {
        Set<String> knownDbSubagents = dbSubAgents.stream().map(DBItemInventorySubAgentInstance::getSubAgentId).collect(Collectors.toSet());
        
        Set<String> unknownSubagents = subAgentCommand.getSubagentIds().stream().filter(s -> !knownDbSubagents.contains(s)).collect(Collectors
                .toSet());
        if (!unknownSubagents.isEmpty()) {
            throw new JocBadRequestException(String.format("The Subagents %s don't belong to the Controller '%s'", unknownSubagents.toString(),
                    subAgentCommand.getControllerId()));
        }
    }
    
    private static Map<Boolean, List<String>> getKnownSubAgentsOnController(SubAgentsCommand subAgentCommand, JControllerState currentState) {
        Set<String> subAgentIdsOnController = currentState.idToSubagentItem().keySet().stream().map(SubagentId::string).collect(Collectors.toSet());
        return subAgentCommand.getSubagentIds().stream().collect(Collectors.groupingBy(s -> subAgentIdsOnController.contains(s)));
    }
}
