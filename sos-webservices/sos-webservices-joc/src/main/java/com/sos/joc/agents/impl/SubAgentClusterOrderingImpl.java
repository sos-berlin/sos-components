package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.ISubAgentClusterOrdering;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.OrderingSubagentClusters;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class SubAgentClusterOrderingImpl extends JOCResourceImpl implements ISubAgentClusterOrdering {

    private static final String API_CALL = "./agents/cluster/ordering";

    @Override
    public JOCDefaultResponse ordering(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);

            // AgentHelper.throwJocMissingLicenseException();

            JsonValidator.validateFailFast(filterBytes, OrderingSubagentClusters.class);
            OrderingSubagentClusters orderingParam = Globals.objectMapper.readValue(filterBytes, OrderingSubagentClusters.class);
            
            // TODO Request with controllerId
            Set<String> allowedControllers = Proxies.getControllerDbInstances().keySet();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            connection.setAutoCommit(false);
            Globals.beginTransaction(connection);
            InventorySubagentClustersDBLayer agentClusterDBLayer = new InventorySubagentClustersDBLayer(connection);
            agentClusterDBLayer.cleanupSubAgentClusterOrdering(false);
            Globals.commit(connection);
            
            List<Exception> exceptions = new ArrayList<>();
            for (String controllerId : allowedControllers) {
                Globals.beginTransaction(connection);
                try {
                    agentClusterDBLayer.setSubAgentClusterOrdering(controllerId, orderingParam.getSubagentClusterId(), orderingParam
                            .getPredecessorSubagentClusterId());
                    Globals.commit(connection);
                } catch (DBMissingDataException e) {
                    Globals.rollback(connection);
                    exceptions.add(e);
                }
            }
            if (!exceptions.isEmpty() && exceptions.size() == allowedControllers.size()) {
                throw exceptions.get(0);
            }
            
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
