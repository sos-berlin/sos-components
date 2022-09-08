package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceReassign;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;

@Path("agents")
public class AgentsResourceReassignImpl extends JOCResourceImpl implements IAgentsResourceReassign {

    private static final String API_CALL = "./agents/inventory/reassign";

    @Override
    public JOCDefaultResponse reAssign2(String accessToken, byte[] filterBytes) {
        return reAssign(accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse reAssign(String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter body = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();
            String controllerId = body.getControllerId();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(body.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("GetAgents");
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(sosHibernateSession);
            JControllerProxy proxy = Proxy.of(controllerId);
            JControllerState currentState = proxy.currentState();
            Map<AgentPath, JAgentRef> controllerKnownAgents = currentState.pathToAgentRef();
            Map<SubagentId, JSubagentItem> controllerKnownSubagents = currentState.idToSubagentItem();
            Map<JAgentRef, List<JSubagentItem>> agents = Proxies.getUnknownAgents(controllerId, dbLayer, controllerKnownAgents,
                    controllerKnownSubagents, true);
            if (!agents.isEmpty()) {

                Stream<JUpdateItemOperation> a = agents.keySet().stream().map(JUpdateItemOperation::addOrChangeSimple);
                Stream<JUpdateItemOperation> s = agents.values().stream().flatMap(l -> l.stream().map(
                        JUpdateItemOperation::addOrChangeSimple));

                proxy.api().updateItems(Flux.concat(Flux.fromStream(a), Flux.fromStream(s))).thenAccept(e -> {
                    ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId);
                    if (e.isRight()) {
                        SOSHibernateSession connection1 = null;
                        try {
                            connection1 = Globals.createSosHibernateStatelessConnection(API_CALL);
                            connection1.setAutoCommit(false);
                            Globals.beginTransaction(connection1);
                            InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                            dbLayer1.setAgentsDeployed(agents.keySet().stream().map(JAgentRef::path).map(AgentPath::string).collect(Collectors
                                    .toList()));
                            dbLayer1.setSubAgentsDeployed(agents.values().stream().flatMap(l -> l.stream()).map(JSubagentItem::path).map(
                                    SubagentId::string).collect(Collectors.toList()));
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
            
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
}
