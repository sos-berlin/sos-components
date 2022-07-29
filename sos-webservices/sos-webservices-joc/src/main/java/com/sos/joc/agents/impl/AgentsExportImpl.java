package com.sos.joc.agents.impl;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsExport;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.transfer.AgentExportFilter;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsExportImpl extends JOCResourceImpl implements IAgentsExport {

    private static final String API_CALL = "./agents/export";

    @Override
    public JOCDefaultResponse postExport(String xAccessToken, byte[] agentsExportFilter) {
        SOSHibernateSession hibernateSession = null;
        StreamingOutput stream = null;
        try {
            initLogging(API_CALL, agentsExportFilter, xAccessToken);
            JsonValidator.validate(agentsExportFilter, AgentExportFilter.class);
            AgentExportFilter filter = Globals.objectMapper.readValue(agentsExportFilter, AgentExportFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            InventoryAgentInstancesDBLayer agentInstanceDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            
            Set<DBItemInventoryAgentInstance> agents = new HashSet<DBItemInventoryAgentInstance>();
            for (String agentId : filter.getAgentIds()) {
                DBItemInventoryAgentInstance agentInstance = agentInstanceDbLayer.getAgentInstance(agentId);
                List<String> subagentIds = agentInstanceDbLayer.getSubAgentIdsByAgentId(agentId);
                if(agentInstance != null) {
                    if (subagentIds != null && !subagentIds.isEmpty()) {
                        // agent cluster with subagents
                    }
                }
            }
        // TODO Auto-generated method stub
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(stream, filter.getExportFile().getFilename());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
