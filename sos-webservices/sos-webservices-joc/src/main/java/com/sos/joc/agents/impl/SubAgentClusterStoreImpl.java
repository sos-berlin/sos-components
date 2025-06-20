package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterStore;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.agent.AgentStoreUtils;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.model.agent.StoreSubagentClusters;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class SubAgentClusterStoreImpl extends JOCResourceImpl implements ISubAgentClusterStore {

    private static final String API_STORE = "./agents/cluster/store";
    private static final String API_ADD = "./agents/cluster/add";

    @Override
    public JOCDefaultResponse store(String accessToken, byte[] filterBytes) {
        return storeOrAdd(API_STORE, accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse add(String accessToken, byte[] filterBytes) {
        return storeOrAdd(API_ADD, accessToken, filterBytes);
    }
    
    private JOCDefaultResponse storeOrAdd(String action, String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(action, filterBytes, accessToken, CategoryType.CONTROLLER);
            
            AgentHelper.throwJocMissingLicenseException();
            
            JsonValidator.validate(filterBytes, StoreSubagentClusters.class);
            StoreSubagentClusters agentStoreParameter = Globals.objectMapper.readValue(filterBytes, StoreSubagentClusters.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getControllers()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(agentStoreParameter.getAuditLog());

            connection = Globals.createSosHibernateStatelessConnection(action);
            connection.setAutoCommit(false);
            connection.beginTransaction();
            InventorySubagentClustersDBLayer agentClusterDBLayer = new InventorySubagentClustersDBLayer(connection);
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(connection);
            Set<String> controllerIds = Collections.emptySet();
            
            if (API_ADD.equals(action)) {
                controllerIds = AgentStoreUtils.storeSubagentCluster(agentStoreParameter.getSubagentClusters(), agentDbLayer, agentClusterDBLayer,
                        true, true, false);
            } else {
                controllerIds = AgentStoreUtils.storeSubagentCluster(agentStoreParameter.getSubagentClusters(), agentDbLayer, agentClusterDBLayer,
                        true, false, agentStoreParameter.getUpdate() == Boolean.TRUE);
            }

            Globals.commit(connection);
            
            controllerIds.forEach(controllerId -> EventBus.getInstance().post(new AgentInventoryEvent(controllerId)));
            
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(connection);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

}
