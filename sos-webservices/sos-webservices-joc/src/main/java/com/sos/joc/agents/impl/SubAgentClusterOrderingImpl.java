package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterOrdering;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.model.agent.OrderingSubagentClusters;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class SubAgentClusterOrderingImpl extends JOCResourceImpl implements ISubAgentClusterOrdering {

    private static final String API_CALL = "./agents/cluster/ordering";

    @Override
    public JOCDefaultResponse ordering(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);

            // AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, OrderingSubagentClusters.class);
            OrderingSubagentClusters orderingParam = Globals.objectMapper.readValue(filterBytes, OrderingSubagentClusters.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getControllers()
                    .getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventorySubagentClustersDBLayer agentClusterDBLayer = new InventorySubagentClustersDBLayer(connection);
            agentClusterDBLayer.cleanupSubAgentClusterOrdering(false);
            agentClusterDBLayer.setSubAgentClusterOrdering(orderingParam.getControllerId(), orderingParam.getSubagentClusterId(), orderingParam
                    .getPredecessorSubagentClusterId());
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
