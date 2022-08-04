package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentStore;
import com.sos.joc.agents.util.AgentStoreUtils;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.StoreSubAgents;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

@Path("agents")
public class SubAgentStoreImpl extends JOCResourceImpl implements ISubAgentStore {

    private static final String API_CALL = "./agents/inventory/cluster/subagents/store";
    
    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);

            AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, StoreSubAgents.class);
            StoreSubAgents subAgentsParam = Globals.objectMapper.readValue(filterBytes, StoreSubAgents.class);

            String controllerId = subAgentsParam.getControllerId();
            String agentId = subAgentsParam.getAgentId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(subAgentsParam.getAuditLog(), controllerId, CategoryType.CONTROLLER);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            
            DBItemInventoryAgentInstance dbAgent = dbLayer.getAgentInstance(agentId);
            if (dbAgent == null) {
                throw new JocBadRequestException("Cluster Agent '" + agentId + "' doesn't exist");
            }
            
            // check uniqueness of SubagentUrl in request
            subAgentsParam.getSubagents().stream().collect(Collectors.groupingBy(SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(e -> e
                    .getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("Subagent url", e));
                    });
            
            Set<String> requestedSubagentIds = subAgentsParam.getSubagents().stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());

            // check java name rules of SubagentIds
            for (String subagentId : requestedSubagentIds) {
                SOSCheckJavaVariableName.test("Subagent ID", subagentId);
            }
            
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIds(null);
            List<DBItemInventorySubAgentInstance> dbSubAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(controllerId));
            
            // check uniqueness of SubagentUrl with DB
            Set<String> requestedSubagentUrls = subAgentsParam.getSubagents().stream().map(SubAgent::getUrl).collect(Collectors.toSet());
            dbSubAgents.stream().filter(s -> !requestedSubagentIds.contains(s.getSubAgentId())).filter(s -> requestedSubagentUrls.contains(s
                    .getUri())).findAny().ifPresent(s -> {
                        throw new JocBadRequestException(String.format("Subagent url %s is already used by Subagent %s", s.getUri(), s.getSubAgentId()));
                    });
            dbAgents.stream().filter(a -> a.getUri() != null && !a.getUri().isEmpty() && !a.getAgentId().equals(agentId)).filter(
                    a -> requestedSubagentUrls.contains(a.getUri())).findAny().ifPresent(a -> {
                        throw new JocBadRequestException(String.format("Subagent url %s is already used by Agent %s", a.getUri(), a.getAgentId()));
                    });
            
            AgentStoreUtils.saveOrUpdate(dbLayer, new InventorySubagentClustersDBLayer(connection), dbAgent, dbSubAgents, subAgentsParam.getSubagents(), true);
            Globals.commit(connection);
            Globals.disconnect(connection);
            connection = null;
            EventBus.getInstance().post(new AgentInventoryEvent(controllerId, agentId));
            
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
    
};