package com.sos.joc.joc.versions.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.joc.versions.resource.IGetVersionsResource;
import com.sos.joc.model.joc.AgentVersion;
import com.sos.joc.model.joc.ControllerVersion;
import com.sos.joc.model.joc.VersionResponse;
import com.sos.joc.model.joc.VersionsFilter;
import com.sos.schema.JsonValidator;

@Path("joc")
public class GetVersionsImpl extends JOCResourceImpl implements IGetVersionsResource{

    private static final String API_CALL = "./joc/versions";
    @Override
    public JOCDefaultResponse postGetVersions(String xAccessToken, byte[] versionsFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, versionsFilter, xAccessToken);
            JsonValidator.validateFailFast(versionsFilter, VersionsFilter.class);
            VersionsFilter filter = Globals.objectMapper.readValue(versionsFilter, VersionsFilter.class);
            Set<String> allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream()
                    .filter(availableController -> getControllerPermissions(availableController, xAccessToken).getView())
                    .collect(Collectors.toSet());
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            List<ControllerVersion> controllerVersions = new ArrayList<ControllerVersion>();
            List<AgentVersion> agentVersions = new ArrayList<AgentVersion>();
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            if (filter.getControllerIds() != null && !filter.getControllerIds().isEmpty() 
                    && filter.getAgentIds() != null && !filter.getAgentIds().isEmpty()) {
                if(filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
                    // only specific controllers
                    filter.getControllerIds().forEach(controllerId -> {
                        if(allowedControllerIds.contains(controllerId)) {
                            controllerVersions.addAll(getControllerVersion(controllerId));
                        }
                    });
                }
                if(filter.getAgentIds() != null && !filter.getAgentIds().isEmpty()) {
                    // only specific agents
                    Set<DBItemInventoryAgentInstance> dbAgents = new HashSet<DBItemInventoryAgentInstance>();
                    Set<DBItemInventorySubAgentInstance> dbSubagents = new HashSet<DBItemInventorySubAgentInstance>();
                    filter.getAgentIds().forEach(agentId -> {
                        try {
                            DBItemInventoryAgentInstance dbAgent = agentDbLayer.getAgentInstance(agentId);
                            if(dbAgent != null) {
                                List<DBItemInventorySubAgentInstance> subagents = new ArrayList<DBItemInventorySubAgentInstance>();
                                if (allowedControllerIds.contains(dbAgent.getControllerId())) {
                                    dbAgents.add(dbAgent);
                                    subagents = agentDbLayer.getSubAgentsByAgentId(agentId);
                                }
                                if(!subagents.isEmpty()) {
                                    dbSubagents.addAll(subagents);
                                }
                            }
                        } catch (SOSHibernateException e) {
                            throw new JocSosHibernateException(e);
                        }
                    });
                    if(!dbAgents.isEmpty()) {
                        dbAgents.forEach(agent -> agentVersions.add(getAgentVersion(agent)));
                    }
                    if(!dbSubagents.isEmpty()) {
                        dbSubagents.forEach(subagent -> agentVersions.add(getSubagentVersion(subagent)));
                    }
                }
            } else {
                // all allowed controllers and agents
                allowedControllerIds.forEach(controllerId -> {
                    controllerVersions.addAll(getControllerVersion(controllerId));
                    List<DBItemInventoryAgentInstance> dbAgents = agentDbLayer.getAgentsByControllerIds(allowedControllerIds);
                    if(!dbAgents.isEmpty()) {
                        dbAgents.forEach(agent -> agentVersions.add(getAgentVersion(agent)));
                    }
                    List<DBItemInventorySubAgentInstance> dbSubagents = agentDbLayer.getSubAgentInstancesByControllerIds(allowedControllerIds);
                    if(!dbSubagents.isEmpty()) {
                        dbSubagents.forEach(subagent -> agentVersions.add(getSubagentVersion(subagent)));
                    }
                });
            }
            String jocVersion = Globals.curVersion;
            VersionResponse response = new VersionResponse();
            response.setAgentVersions(agentVersions);
            response.setControllerVersions(controllerVersions);
            response.setJocVersion(jocVersion);
            return JOCDefaultResponse.responseStatus200(response);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private static List<ControllerVersion> getControllerVersion (String controllerId) {
        return Proxies.getControllerDbInstances().get(controllerId).stream()
                .map(dbItem -> {
                    ControllerVersion controllerVersion = new ControllerVersion();
                    controllerVersion.setControllerId(controllerId);
                    controllerVersion.setUri(dbItem.getUri());
                    controllerVersion.setVersion(dbItem.getVersion());
                    return controllerVersion;
                }).collect(Collectors.toList());
    }
    
    private static AgentVersion getAgentVersion (DBItemInventoryAgentInstance agent) {
        AgentVersion agentVersion = new AgentVersion();
        agentVersion.setAgentId(agent.getAgentId());
        agentVersion.setSubagentId(null);
        agentVersion.setUri(agent.getUri());
        agentVersion.setVersion(agent.getVersion());
        return agentVersion;
    }
    
    private static AgentVersion getSubagentVersion (DBItemInventorySubAgentInstance subagent) {
        AgentVersion agentVersion = new AgentVersion();
        agentVersion.setAgentId(subagent.getAgentId());
        agentVersion.setSubagentId(subagent.getSubAgentId());
        agentVersion.setUri(subagent.getUri());
        agentVersion.setVersion(subagent.getVersion());
        return agentVersion;
    }
}
