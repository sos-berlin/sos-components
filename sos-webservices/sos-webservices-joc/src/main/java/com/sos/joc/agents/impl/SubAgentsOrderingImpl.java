package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentsOrdering;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.OrderingSubagentClusters;
import com.sos.joc.model.agent.OrderingSubagents;
import com.sos.schema.JsonValidator;

@Path("agents")
public class SubAgentsOrderingImpl extends JOCResourceImpl implements ISubAgentsOrdering {

    private static final String API_CALL = "./agents/inventory/cluster/subagents/ordering";

    @Override
    public JOCDefaultResponse ordering(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);

            // AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, OrderingSubagents.class);
            OrderingSubagents orderingParam = Globals.objectMapper.readValue(filterBytes, OrderingSubagents.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventoryAgentInstancesDBLayer agentDBLayer = new InventoryAgentInstancesDBLayer(connection);
            agentDBLayer.cleanupSubAgentsOrdering(false);
            agentDBLayer.setSubAgentsOrdering(orderingParam.getSubagentId(), orderingParam.getPredecessorSubagentId());
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
