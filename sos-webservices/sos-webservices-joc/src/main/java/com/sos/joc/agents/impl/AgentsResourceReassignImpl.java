package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceReassign;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

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
            
            // TODO consider old Agent cannot convert to new Agents
            //Map<JAgentRef, List<JSubagentRef>> agents = Proxies.getAgents(controllerId, null);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(sosHibernateSession);
            List<DBItemInventoryAgentInstance> dbAvailableAgents = dbLayer.getAgentsByControllerIds(Collections.singleton(controllerId), false, true);
           
            if (dbAvailableAgents != null) {
                JControllerProxy proxy = Proxy.of(controllerId);
                JControllerState currentState = proxy.currentState();
                Map<JAgentRef, List<JSubagentItem>> agents = new LinkedHashMap<>(dbAvailableAgents.size());
                
                Map<String, List<DBItemInventorySubAgentInstance>> subAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(
                        controllerId), false, true);
                Map<AgentPath, JAgentRef> knownAgents = currentState.pathToAgentRef();
                
                for (DBItemInventoryAgentInstance agent : dbAvailableAgents) {
                    boolean versionBefore220beta20211201 = false;
                    List<DBItemInventorySubAgentInstance> subs = subAgents.get(agent.getAgentId());
                    if (subs == null || subs.isEmpty()) { // single agent
                        JAgentRef agentE = knownAgents.get(AgentPath.of(agent.getAgentId()));
                        if (agentE != null && (!agentE.director().isPresent() || agentE.directors().isEmpty())) {
                            versionBefore220beta20211201 = true;
                        }
                        subs = Collections.singletonList(dbLayer.solveAgentWithoutSubAgent(agent));
                    }
                    List<JSubagentItem> subRefs = subs.stream().map(s -> JSubagentItem.of(SubagentId.of(s.getSubAgentId()), AgentPath.of(s
                            .getAgentId()), Uri.of(s.getUri()), s.getDisabled())).collect(Collectors.toList());
                    Set<SubagentId> directors = subs.stream().filter(s -> s.getIsDirector() > SubagentDirectorType.NO_DIRECTOR.intValue()).sorted()
                            .map(s -> SubagentId.of(s.getSubAgentId())).collect(Collectors.toSet());
                    if (versionBefore220beta20211201) {
                        agents.put(JAgentRef.of(AgentPath.of(agent.getAgentId()), Uri.of(agent.getUri())), Collections.emptyList());
                    } else {
                        agents.put(JAgentRef.of(AgentPath.of(agent.getAgentId()), directors), subRefs);
                    }
                }
                
                if (!agents.isEmpty()) {
                    Stream<JUpdateItemOperation> a = agents.keySet().stream().map(JUpdateItemOperation::addOrChangeSimple);
                    Stream<JUpdateItemOperation> s = agents.values().stream().flatMap(l -> l.stream().map(JUpdateItemOperation::addOrChangeSimple));
                    proxy.api().updateItems(Flux.concat(Flux.fromStream(a), Flux.fromStream(s)))
                        .thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, accessToken, getJocError(), controllerId));
                }
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
