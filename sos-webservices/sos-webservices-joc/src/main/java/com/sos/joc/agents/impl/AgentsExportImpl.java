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
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.transfer.Agent;
import com.sos.joc.model.agent.transfer.AgentExportFilter;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.publish.util.ExportUtils;
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
            
            Set<Agent> agents = new HashSet<Agent>();
            for (String agentId : filter.getAgentIds()) {
                DBItemInventoryAgentInstance agentInstance = agentInstanceDbLayer.getAgentInstance(agentId);
                List<DBItemInventorySubAgentInstance> subagents = agentInstanceDbLayer.getSubAgentInstancesByAgentId(agentId);
                if(agentInstance != null) {
                    Agent agent = new Agent();
                    if (subagents != null && !subagents.isEmpty()) {
                        // agent cluster with subagents
                        ClusterAgent clAgent = new ClusterAgent();
                        clAgent.setAgentId(agentId);
                        clAgent.setAgentName(agentInstance.getAgentName());
                        clAgent.setAgentNameAliases(agentInstanceDbLayer.getAgentNamesByAgentIds(agentId));
                        clAgent.setControllerId(agentInstance.getControllerId());
                        clAgent.setDeployed(agentInstance.getDeployed());
                        clAgent.setDisabled(agentInstance.getDisabled());
                        clAgent.setHidden(agentInstance.getHidden());
                        clAgent.setIsClusterWatcher(agentInstance.getIsWatcher());
                        clAgent.setOrdering(agentInstance.getOrdering());
                        clAgent.setSyncState(null);
                        clAgent.setTitle(agentInstance.getTitle());
                        clAgent.setUrl(agentInstance.getUri());
                        subagents.stream().forEach(subagent -> {
                            SubAgent subAgent = new SubAgent();
                            subAgent.setAgentId(agentId);
                            subAgent.setDeployed(subagent.getDeployed());
                            subAgent.setDisabled(subagent.getDisabled());
                            subAgent.setIsClusterWatcher(subagent.getIsWatcher());
                            subAgent.setIsDirector(subagent.getDirectorAsEnum());
                            subAgent.setOrdering(subagent.getOrdering());
                            subAgent.setSubagentId(subagent.getSubAgentId());
                            subAgent.setSyncState(null);
                            subAgent.setTitle(subagent.getTitle());
                            subAgent.setUrl(subagent.getUri());
                            // Ask Olli
                            subAgent.setWithGenerateSubagentCluster(null);
                            clAgent.getSubagents().add(subAgent); 
                        });
                        agent.setAgentCluster(clAgent);
                    } else {
                        com.sos.joc.model.agent.Agent standalone = new com.sos.joc.model.agent.Agent();
                        standalone.setAgentId(agentId);
                        standalone.setAgentName(agentInstance.getAgentName());
                        standalone.setAgentNameAliases(agentInstanceDbLayer.getAgentNamesByAgentIds(agentId));
                        standalone.setControllerId(agentInstance.getControllerId());
                        standalone.setDeployed(agentInstance.getDeployed());
                        standalone.setDisabled(agentInstance.getDisabled());
                        standalone.setHidden(agentInstance.getHidden());
                        standalone.setIsClusterWatcher(agentInstance.getIsWatcher());
                        standalone.setOrdering(agentInstance.getOrdering());
                        standalone.setSyncState(null);
                        standalone.setTitle(agentInstance.getTitle());
                        standalone.setUrl(agentInstance.getUri());
                        agent.setStandaloneAgent(standalone);
                    }
                    agents.add(agent);
                }
            }
            if (ArchiveFormat.ZIP.equals(filter.getExportFile().getFormat())) {
                stream = ExportUtils.writeAgentExportZipFile(agents);
            } else {
                stream = ExportUtils.writeAgentExportTarGzipFile(agents);
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
