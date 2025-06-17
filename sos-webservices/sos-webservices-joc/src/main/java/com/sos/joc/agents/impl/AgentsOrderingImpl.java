package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsOrdering;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.model.agent.OrderingAgents;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class AgentsOrderingImpl extends JOCResourceImpl implements IAgentsOrdering {

    private static final String API_CALL_STANDALONE = "./agents/inventory/ordering";
    private static final String API_CALL_CLUSTER = "./agents/inventory/cluster/ordering";

    @Override
    public JOCDefaultResponse standaloneOrdering(String accessToken, byte[] filterBytes) {
        return ordering(accessToken, filterBytes, true);
    }
    
    @Override
    public JOCDefaultResponse clusterOrdering(String accessToken, byte[] filterBytes) {
        return ordering(accessToken, filterBytes, false);
    }
    
    private JOCDefaultResponse ordering(String accessToken, byte[] filterBytes, boolean forStandaloneAgents) {
        SOSHibernateSession connection = null;
        try {
            String apiCall = forStandaloneAgents ? API_CALL_STANDALONE : API_CALL_CLUSTER;
            filterBytes = initLogging(apiCall, filterBytes, accessToken, CategoryType.CONTROLLER);

            // AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, OrderingAgents.class);
            OrderingAgents orderingParam = Globals.objectMapper.readValue(filterBytes, OrderingAgents.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getControllers()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(apiCall);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            agentDBLayer.cleanupAgentsOrdering(false);
            agentDBLayer.setAgentsOrdering(orderingParam.getAgentId(), orderingParam.getPredecessorAgentId(), forStandaloneAgents);
            Globals.commit(connection);

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            Globals.rollback(connection);
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
}
