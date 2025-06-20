package com.sos.joc.agents.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSCheckJavaVariableName;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsImport;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentStoreUtils;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentInventoryEvent;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.agent.ClusterAgent;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.agent.transfer.Agent;
import com.sos.joc.model.agent.transfer.AgentImportFilter;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.publish.util.ImportUtils;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("agents")
public class AgentsImportImpl extends JOCResourceImpl implements IAgentsImport {

    private static final String API_CALL = "./agents/import";

    @Override
    public JOCDefaultResponse postImportConfiguration(String xAccessToken, FormDataBodyPart body, String format, String controllerId, boolean overwrite, 
            String timeSpent, String ticketLink, String comment) {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
        AgentImportFilter filter = new AgentImportFilter();
        filter.setAuditLog(auditLog);
        filter.setFormat(ArchiveFormat.fromValue(format));
        filter.setControllerId(controllerId);
        filter.setOverwrite(overwrite);
        filter.setFilename(PublishUtils.getImportFilename(body));
        return postImport(xAccessToken, body, filter, auditLog);
    }

    
    public JOCDefaultResponse postImport(String xAccessToken, FormDataBodyPart body, AgentImportFilter filter, AuditParams auditLog) {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            byte[] fakeRequest = Globals.objectMapper.writeValueAsBytes(filter);
            initLogging(API_CALL, fakeRequest, xAccessToken, CategoryType.CONTROLLER); 
            JsonValidator.validateFailFast(fakeRequest, AgentImportFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getControllers()
                    .getManage(), false); //4-eyes principle cannot support uploads
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            
            
            storeAuditLog(filter.getAuditLog());
            stream = body.getEntityAs(InputStream.class);
            
            Set<Agent> agents = new HashSet<Agent>();
            // process uploaded archive
            if (ArchiveFormat.ZIP.equals(filter.getFormat())) {
                agents = ImportUtils.readAgentsFromZipFileContent(stream);
            } else if (ArchiveFormat.TAR_GZ.equals(filter.getFormat())) {
                agents = ImportUtils.readAgentsFromTarGzipFileContent(stream);
            } else {
                throw new JocUnsupportedFileTypeException(
                        String.format("The file %1$s to be uploaded must have one of the formats zip or tar.gz!", uploadFileName)); 
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            hibernateSession.setAutoCommit(false);
            hibernateSession.beginTransaction();
            
            Map<String, Long> agentIds = agents.stream().map(item -> {
                if(item.getAgentCluster() != null) {
                    return item.getAgentCluster();
                } else {
                    return item.getStandaloneAgent();
                }
            }).collect(Collectors.groupingBy(com.sos.joc.model.agent.Agent::getAgentId, Collectors.counting()));
            // check uniqueness of AgentId
            agentIds.entrySet().stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("AgentId", e));
            });
            // check uniqueness of agentName
            List<com.sos.joc.model.agent.Agent> mappedAgents = agents.stream()
                    .map(agent -> agent.getAgentCluster() != null ? agent.getAgentCluster() : agent.getStandaloneAgent())
                    .collect(Collectors.toList());
            AgentStoreUtils.checkUniquenessOfAgentNames(mappedAgents);
            // check uniqueness of AgentUrl
            mappedAgents.stream().collect(Collectors.groupingBy(com.sos.joc.model.agent.Agent::getUrl, Collectors.counting())).entrySet()
                .stream().filter(e -> e.getValue() > 1L).findAny().ifPresent(e -> {
                        throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("Agent url", e));
                    });
            // check java name rules of AgentIds
            for (String agentId : agentIds.keySet()) {
                SOSCheckJavaVariableName.test("Agent ID", agentId);
            }

            Map<String, com.sos.joc.model.agent.Agent> standaloneAgentMap = new HashMap<>();
            Map<String, ClusterAgent> clusterAgentMap = new HashMap<>();
            List<SubagentCluster> subagentClusters = new ArrayList<>();
            
            agents.forEach(agent -> {
                if(agent.getAgentCluster() != null) {
                    Set<SubAgent> requestedSubagents = agent.getAgentCluster().getSubagents().stream().collect(Collectors.toSet());
                    Set<String> requestedSubagentIds = requestedSubagents.stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());
                    // check java name rules of AgentIds
                    for (String subagentId : requestedSubagentIds) {
                        SOSCheckJavaVariableName.test("Subagent ID", subagentId);
                    }
                    clusterAgentMap.put(agent.getAgentCluster().getAgentId(), agent.getAgentCluster());
//                    try {
//                        AgentStoreUtils.storeClusterAgent(agent.getAgentCluster(), filter.getControllerId(), filter.getOverwrite(),
//                                agentInstanceDbLayer, subagentClusterDbLayer);
//                    } catch (SOSHibernateException e) {
//                        throw new JocSosHibernateException(e);
//                    }
                    if (agent.getSubagentClusters() != null) {
                        subagentClusters.addAll(agent.getSubagentClusters());
//                        for (SubagentCluster subagentCluster : agent.getSubagentClusters()) {
//                            try {
//                                AgentStoreUtils.storeSubagentCluster(filter.getControllerId(), subagentCluster, subagentClusterDbLayer, Date.from(
//                                        Instant.now()));
//                            } catch (SOSHibernateException e) {
//                                throw new JocSosHibernateException(e);
//                            }
//                        }
                    }
                } else if(agent.getStandaloneAgent() != null) {
                    standaloneAgentMap.put(agent.getStandaloneAgent().getAgentId(), agent.getStandaloneAgent());
//                    try {
//                        AgentStoreUtils.storeStandaloneAgent(agent.getStandaloneAgent(), filter.getControllerId(), filter.getOverwrite(),
//                                agentInstanceDbLayer);
//                    } catch (SOSHibernateException e) {
//                        throw new JocSosHibernateException(e);
//                    }
                }
            });
            
            InventoryAgentInstancesDBLayer agentInstanceDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            InventorySubagentClustersDBLayer subagentClusterDbLayer = new InventorySubagentClustersDBLayer(hibernateSession);
            
            
            
            if (!standaloneAgentMap.isEmpty()) {
                AgentStoreUtils.storeStandaloneAgent(standaloneAgentMap, filter.getControllerId(), filter.getOverwrite(), false, false,
                        agentInstanceDbLayer);
            }
            if (!clusterAgentMap.isEmpty()) {
                Set<SubAgent> requestedSubagents = clusterAgentMap.values().stream().map(ClusterAgent::getSubagents).flatMap(List::stream).collect(
                        Collectors.toSet());
                requestedSubagents.stream().collect(Collectors.groupingBy(SubAgent::getUrl, Collectors.counting())).entrySet().stream().filter(e -> e
                        .getValue() > 1L).findAny().ifPresent(e -> {
                            throw new JocBadRequestException(AgentStoreUtils.getUniquenessMsg("Subagent url", e));
                        });

                Set<String> requestedSubagentIds = requestedSubagents.stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());
                AgentStoreUtils.storeClusterAgent(clusterAgentMap, requestedSubagents, requestedSubagentIds, filter.getControllerId(), filter
                        .getOverwrite(), false, false, agentInstanceDbLayer, subagentClusterDbLayer);
            }
            if (!subagentClusters.isEmpty()) {

                AgentStoreUtils.storeSubagentCluster(subagentClusters, agentInstanceDbLayer, subagentClusterDbLayer, filter.getOverwrite(), false,
                        false);
            }
            
            Globals.commit(hibernateSession);
            
            EventBus.getInstance().post(new AgentInventoryEvent(filter.getControllerId(), agents.stream().map(agent -> {
                if(agent.getAgentCluster() != null) {
                    return agent.getAgentCluster().getAgentId();
                } else {
                    return agent.getStandaloneAgent().getAgentId();
                }
            }).collect(Collectors.toList())));
            
//            Set<String> agentNamesAndAliases = Stream.concat(
//                    agents.stream().map(agent -> agent.getAgentCluster() != null ?  agent.getAgentCluster().getAgentName() :
//                        agent.getStandaloneAgent().getAgentName()), 
//                    agents.stream().map(agent -> agent.getAgentCluster() != null ? agent.getAgentCluster().getAgentNameAliases() : 
//                        agent.getStandaloneAgent().getAgentNameAliases()).flatMap(Set::stream)).collect(Collectors.toSet());
            // TODO investigate missedNames instead Collections.emptySet()
            //AgentHelper.validateWorkflowsByAgentNames(agentInstanceDbLayer, agentNamesAndAliases, Collections.emptySet());
            Globals.disconnect(hibernateSession);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
    }
    
}
