package com.sos.joc.joc.versions.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import com.sos.joc.joc.versions.util.CheckVersion;
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
            // only for debugging, as curVersion is not set in dev environment (grizzly)
                final String jocVersion = !Globals.curVersion.isEmpty() ? Globals.curVersion : "2.5.0-SNAPSHOT_hardcoded";
            List<ControllerVersion> controllerVersions = new ArrayList<ControllerVersion>();
            List<AgentVersion> agentVersions = new ArrayList<AgentVersion>();
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            if (filter.getControllerIds() != null && !filter.getControllerIds().isEmpty() 
                    && filter.getAgentIds() != null && !filter.getAgentIds().isEmpty()) {
                if(filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
                    // only specific controllers
                    filter.getControllerIds().forEach(controllerId -> {
                        if(allowedControllerIds.contains(controllerId)) {
                            controllerVersions.addAll(createControllerVersion(controllerId, jocVersion));
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
                                    agentVersions.add(createAgentVersion(dbAgent, getControllerVersion(controllerVersions, dbAgent.getControllerId())));
                                    subagents = agentDbLayer.getSubAgentsByAgentId(agentId);
                                    if(!subagents.isEmpty()) {
                                        subagents.forEach(subagent -> agentVersions.add(
                                                createSubagentVersion(subagent, getControllerVersion(controllerVersions, dbAgent.getControllerId()))));
                                    }
                                }
                                if(!subagents.isEmpty()) {
                                    dbSubagents.addAll(subagents);
                                }
                            }
                        } catch (SOSHibernateException e) {
                            throw new JocSosHibernateException(e);
                        }
                    });
                }
            } else {
                // all allowed controllers and agents
                allowedControllerIds.forEach(controllerId -> {
                    controllerVersions.addAll(createControllerVersion(controllerId, jocVersion));
                    List<DBItemInventoryAgentInstance> dbAgents = agentDbLayer.getAgentsByControllerIds(allowedControllerIds);
                    if(!dbAgents.isEmpty()) {
                        dbAgents.forEach(agent -> {
                            agentVersions.add(createAgentVersion(agent, getControllerVersion(controllerVersions, agent.getControllerId())));
                            List<DBItemInventorySubAgentInstance> dbSubagents = agentDbLayer.getSubAgentInstancesByAgentId(agent.getAgentId());
                            if(!dbSubagents.isEmpty()) {
                                dbSubagents.forEach(subagent -> agentVersions.add(
                                        createSubagentVersion(subagent, getControllerVersion(controllerVersions, controllerId))));
                            }
                        });
                    }
                });
            }
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

    private static List<ControllerVersion> createControllerVersion (String controllerId, String jocVersion) {
        return Proxies.getControllerDbInstances().get(controllerId).stream()
                .map(dbItem -> {
                    ControllerVersion controllerVersion = new ControllerVersion();
                    controllerVersion.setControllerId(controllerId);
                    controllerVersion.setUri(dbItem.getUri());
                    controllerVersion.setVersion(dbItem.getVersion());
                    controllerVersion.setCompatibility(CheckVersion.checkControllerVersionMatches2Joc(controllerVersion.getVersion(), jocVersion));
                    return controllerVersion;
                }).collect(Collectors.toList());
    }
    
    private static AgentVersion createAgentVersion (DBItemInventoryAgentInstance agent, Optional<String> controllerVersion) {
        AgentVersion agentVersion = new AgentVersion();
        agentVersion.setAgentId(agent.getAgentId());
        agentVersion.setSubagentId(null);
        agentVersion.setUri(agent.getUri());
        agentVersion.setVersion(agent.getVersion());
        if (agent.getVersion() != null) {
            controllerVersion.ifPresent(item -> agentVersion.setCompatibility(CheckVersion.checkAgentVersionMatches2Controller(agent.getVersion(), item)));
        }
        return agentVersion;
    }

    private static AgentVersion createSubagentVersion (DBItemInventorySubAgentInstance subagent, Optional<String> controllerVersion) {
        AgentVersion agentVersion = new AgentVersion();
        agentVersion.setAgentId(subagent.getAgentId());
        agentVersion.setSubagentId(subagent.getSubAgentId());
        agentVersion.setUri(subagent.getUri());
        agentVersion.setVersion(subagent.getVersion());
        if(subagent.getVersion() != null) {
            controllerVersion.ifPresent(item -> agentVersion.setCompatibility(CheckVersion.checkAgentVersionMatches2Controller(subagent.getVersion(), item)));
        }
        return agentVersion;
    }

    private static Optional<String> getControllerVersion (List<ControllerVersion> controllerVersions, String controllerId) {
        return controllerVersions.stream()
                .filter(controllerVersion -> controllerId.equals(controllerVersion.getControllerId())).findAny().map(item -> item.getVersion());
    }
    
}
