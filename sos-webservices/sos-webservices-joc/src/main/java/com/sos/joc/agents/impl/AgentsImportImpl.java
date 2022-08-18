package com.sos.joc.agents.impl;

import java.io.InputStream;
import java.net.URLDecoder;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
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
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.agent.SubAgent;
import com.sos.joc.model.agent.SubagentCluster;
import com.sos.joc.model.agent.transfer.Agent;
import com.sos.joc.model.agent.transfer.AgentImportFilter;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.publish.ArchiveFormat;
import com.sos.joc.publish.util.ImportUtils;
import com.sos.schema.JsonValidator;

@Path("agents")
public class AgentsImportImpl extends JOCResourceImpl implements IAgentsImport {

    private static final String API_CALL = "./agents/export";

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
        return postImport(xAccessToken, body, filter, auditLog);
    }

    
    public JOCDefaultResponse postImport(String xAccessToken, FormDataBodyPart body, AgentImportFilter filter, AuditParams auditLog) {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, null, xAccessToken); 
            JsonValidator.validate(Globals.objectMapper.writeValueAsBytes(filter), AgentImportFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getControllers()
                    .getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            
            
            storeAuditLog(filter.getAuditLog(), CategoryType.CONTROLLER);
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

            InventoryAgentInstancesDBLayer agentInstanceDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            InventorySubagentClustersDBLayer subagentClusterDbLayer = new InventorySubagentClustersDBLayer(hibernateSession);
            agents.forEach(agent -> {
                if(agent.getAgentCluster() != null) {
                    Set<SubAgent> requestedSubagents = agent.getAgentCluster().getSubagents().stream().collect(Collectors.toSet());
                    Set<String> requestedSubagentIds = requestedSubagents.stream().map(SubAgent::getSubagentId).collect(Collectors.toSet());
                    // check java name rules of AgentIds
                    for (String subagentId : requestedSubagentIds) {
                        SOSCheckJavaVariableName.test("Subagent ID", subagentId);
                    }
                    try {
                        AgentStoreUtils.storeClusterAgent(agent.getAgentCluster(), filter.getControllerId(), filter.getOverwrite(),
                                agentInstanceDbLayer, subagentClusterDbLayer);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                    if(agent.getSubagentClusters() != null) {
                        for(SubagentCluster subagentCluster : agent.getSubagentClusters()) {
                            try {
                                AgentStoreUtils.storeSubagentCluster(subagentCluster, subagentClusterDbLayer, Date.from(Instant.now()));
                            } catch (SOSHibernateException e) {
                                throw new JocSosHibernateException(e);
                            }
                        }
                    }
                } else {
                    try {
                        AgentStoreUtils.storeStandaloneAgent(agent.getStandaloneAgent(), filter.getControllerId(), filter.getOverwrite(),
                                agentInstanceDbLayer);
                    } catch (SOSHibernateException e) {
                        throw new JocSosHibernateException(e);
                    }
                }
            });
            EventBus.getInstance().post(new AgentInventoryEvent(filter.getControllerId(), agents.stream().map(agent -> {
                if(agent.getAgentCluster() != null) {
                    return agent.getAgentCluster().getAgentId();
                } else {
                    return agent.getStandaloneAgent().getAgentId();
                }
            }).collect(Collectors.toList())));
            Globals.commit(hibernateSession);
            Globals.disconnect(hibernateSession);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
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
