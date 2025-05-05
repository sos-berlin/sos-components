package com.sos.joc.agents.impl;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.StreamingOutput;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsExport;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentClusterMember;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubAgentId;
import com.sos.joc.model.agent.SubagentCluster;
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
            JsonValidator.validateFailFast(agentsExportFilter, AgentExportFilter.class);
            AgentExportFilter filter = Globals.objectMapper.readValue(agentsExportFilter, AgentExportFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getControllers().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            InventoryAgentInstancesDBLayer agentInstanceDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            InventorySubagentClustersDBLayer subagentClusterDbLayer = new InventorySubagentClustersDBLayer(hibernateSession);
            
            Set<Agent> agents = new HashSet<Agent>();
            agentInstanceDbLayer.getAgentInstances(filter.getAgentIds()).stream().sorted(Comparator.comparingInt(
                    DBItemInventoryAgentInstance::getOrdering)).forEachOrdered(agentInstance -> {
                        List<DBItemInventorySubAgentInstance> subagents = agentInstanceDbLayer.getSubAgentInstancesByAgentId(agentInstance
                                .getAgentId());
                        Agent agent = new Agent();
                        if (subagents != null && !subagents.isEmpty()) {
                            // agent cluster with subagents
                            ClusterAgent clAgent = createClusterAgent(agentInstance, subagents, agentInstanceDbLayer);
                            agent.setAgentCluster(clAgent);

                            List<DBItemInventorySubAgentCluster> subagentClusterInstances = subagentClusterDbLayer.getSubagentClustersByAgentId(
                                    agentInstance.getAgentId());
                            if (!subagentClusterInstances.isEmpty()) {
                                List<DBItemInventorySubAgentClusterMember> subagentClusterMemberInstances = subagentClusterDbLayer
                                        .getSubagentClusterMembers(subagentClusterInstances.stream().map(
                                                DBItemInventorySubAgentCluster::getSubAgentClusterId).collect(Collectors.toList()), agentInstance
                                                        .getControllerId());
                                subagentClusterInstances.stream().sorted(Comparator.comparingInt(DBItemInventorySubAgentCluster::getOrdering))
                                        .forEachOrdered(sac -> agent.getSubagentClusters().add(createSubagentCluster(sac,
                                                subagentClusterMemberInstances, agentInstance.getControllerId())));
                            }
                        } else {
                            com.sos.joc.model.agent.Agent standalone = createStandaloneAgent(agentInstance, agentInstanceDbLayer);
                            agent.setStandaloneAgent(standalone);
                        }
                        agents.add(agent);
                    });
            if (ArchiveFormat.ZIP.equals(filter.getExportFile().getFormat())) {
                stream = ExportUtils.writeAgentExportZipFile(agents);
            } else {
                stream = ExportUtils.writeAgentExportTarGzipFile(agents);
            }
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

    private static ClusterAgent createClusterAgent (DBItemInventoryAgentInstance agentInstance, List<DBItemInventorySubAgentInstance> subagents, 
            InventoryAgentInstancesDBLayer agentInstanceDbLayer) {
        ClusterAgent clAgent = new ClusterAgent();
        clAgent.setAgentId(agentInstance.getAgentId());
        clAgent.setAgentName(agentInstance.getAgentName());
        clAgent.setAgentNameAliases(agentInstanceDbLayer.getAgentNamesByAgentIds(agentInstance.getAgentId()));
        clAgent.setControllerId(agentInstance.getControllerId());
        clAgent.setProcessLimit(agentInstance.getProcessLimit());
        clAgent.setDeployed(agentInstance.getDeployed());
        clAgent.setDisabled(agentInstance.getDisabled());
        clAgent.setHidden(agentInstance.getHidden());
        clAgent.setOrdering(null);
        clAgent.setSyncState(null);
        clAgent.setTitle(agentInstance.getTitle());
        clAgent.setUrl(agentInstance.getUri());
        clAgent.setVersion(agentInstance.getVersion());
        subagents.stream().sorted(Comparator.comparingInt(DBItemInventorySubAgentInstance::getOrdering)).forEachOrdered(subagent -> {
            SubAgent subAgent = new SubAgent();
            subAgent.setAgentId(agentInstance.getAgentId());
            subAgent.setDeployed(subagent.getDeployed());
            subAgent.setDisabled(subagent.getDisabled());
            subAgent.setIsDirector(subagent.getDirectorAsEnum());
            subAgent.setOrdering(null);
            subAgent.setSubagentId(subagent.getSubAgentId());
            subAgent.setSyncState(null);
            subAgent.setTitle(subagent.getTitle());
            subAgent.setUrl(subagent.getUri());
            subAgent.setVersion(subagent.getVersion());
            // Ask Olli
            subAgent.setWithGenerateSubagentCluster(null);
            clAgent.getSubagents().add(subAgent);
        });
        return clAgent;
    }
    
    private static com.sos.joc.model.agent.Agent createStandaloneAgent(DBItemInventoryAgentInstance agentInstance,
            InventoryAgentInstancesDBLayer agentInstanceDbLayer) {
        com.sos.joc.model.agent.Agent standalone = new com.sos.joc.model.agent.Agent();
        standalone.setAgentId(agentInstance.getAgentId());
        standalone.setAgentName(agentInstance.getAgentName());
        standalone.setAgentNameAliases(agentInstanceDbLayer.getAgentNamesByAgentIds(agentInstance.getAgentId()));
        standalone.setControllerId(agentInstance.getControllerId());
        standalone.setProcessLimit(agentInstance.getProcessLimit());
        standalone.setDeployed(agentInstance.getDeployed());
        standalone.setDisabled(agentInstance.getDisabled());
        standalone.setHidden(agentInstance.getHidden());
        standalone.setOrdering(null);
        standalone.setSyncState(null);
        standalone.setTitle(agentInstance.getTitle());
        standalone.setVersion(agentInstance.getVersion());
        standalone.setUrl(agentInstance.getUri());
        return standalone;
    }
    
    private static SubagentCluster createSubagentCluster (DBItemInventorySubAgentCluster subagentClusterInstance,
            List<DBItemInventorySubAgentClusterMember> subagentClusterMemberInstances, String controllerId) {
        SubagentCluster subagentCluster = new SubagentCluster();
        subagentCluster.setAgentId(subagentClusterInstance.getAgentId());
        subagentCluster.setControllerId(controllerId);
        subagentCluster.setDeployed(subagentClusterInstance.getDeployed());
        subagentCluster.setOrdering(null);
        subagentCluster.setSubagentClusterId(subagentClusterInstance.getSubAgentClusterId());
        subagentCluster.setSubagentIds(subagentClusterMemberInstances.stream()
                .filter(member -> member.getSubAgentClusterId().equals(subagentClusterInstance.getSubAgentClusterId()))
                .map(item -> createSubagentId(item))
                .collect(Collectors.toList()));
        subagentCluster.setSyncState(null);
        subagentCluster.setTitle(subagentClusterInstance.getTitle());
        return subagentCluster;
    }
    
    private static SubAgentId createSubagentId (DBItemInventorySubAgentClusterMember subagentClusterMemberInstance) {
        SubAgentId subagentId = new SubAgentId();
        subagentId.setSubagentId(subagentClusterMemberInstance.getSubAgentId());
        subagentId.setPriority(subagentClusterMemberInstance.getPriority());
        return subagentId;
    }
}
