package com.sos.joc.joc.versions.impl;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.agent.AgentStoreUtils;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.versions.resource.IGetVersionsResource;
import com.sos.joc.joc.versions.util.CheckVersion;
import com.sos.joc.model.joc.AgentVersion;
import com.sos.joc.model.joc.ControllerVersion;
import com.sos.joc.model.joc.VersionResponse;
import com.sos.joc.model.joc.VersionsFilter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.base.version.Version;
import js7.data.agent.AgentPath;
import js7.data.platform.PlatformInfo;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRefState;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.subagent.JSubagentItemState;
import scala.jdk.javaapi.OptionConverters;

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
            final String jocVersion = !Globals.curVersion.isEmpty() ? Globals.curVersion : "2.5.1-SNAPSHOT_hardcoded";
            InventoryAgentInstancesDBLayer agentDbLayer = new InventoryAgentInstancesDBLayer(hibernateSession);
            InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(hibernateSession);
            Set<String> filteredControllerIds = Collections.emptySet();
            if(filter.getControllerIds() != null && !filter.getControllerIds().isEmpty()) {
                filteredControllerIds = filter.getControllerIds().stream()
                        .filter(controllerId -> allowedControllerIds.contains(controllerId)).collect(Collectors.toSet());
            } else {
                filteredControllerIds = allowedControllerIds;
            }
            if(filteredControllerIds.size() == Proxies.getControllerDbInstances().keySet().size()) {
                filteredControllerIds = Collections.emptySet();
            }
            List<ControllerVersion> controllerVersions = instanceLayer.getInventoryInstancesByControllerIds(filteredControllerIds).stream()
                    .map(controllerDbItem -> createControllerVersion(controllerDbItem, jocVersion))
                    .collect(Collectors.toList());
            
            Map<String, Optional<JControllerState>> controllerStates = controllerVersions.stream().map(ControllerVersion::getControllerId).distinct()
                    .collect(Collectors.toMap(s -> s, s -> getControllerState(s), (k, v) -> v));

            List<DBItemInventoryAgentInstance> agentDbItems = agentDbLayer.getAgentInstances(filter.getAgentIds());
            List<AgentVersion> agentVersions = agentDbItems.stream().filter(agentDbItem -> allowedControllerIds.contains(agentDbItem
                    .getControllerId())).map(agent -> createAgentVersion(agent, getControllerVersion(controllerVersions, agent.getControllerId()),
                            controllerStates.getOrDefault(agent.getControllerId(), Optional.empty()))).collect(Collectors.toList());
            agentDbItems.stream().forEach(agentDbItem -> {
                List<DBItemInventorySubAgentInstance> subagents = agentDbLayer.getSubAgentsByAgentId(agentDbItem.getAgentId());
                if (!subagents.isEmpty()) {
                    subagents.forEach(subagent -> agentVersions.add(createSubagentVersion(subagent, getControllerVersion(controllerVersions,
                            agentDbItem.getControllerId()), controllerStates.getOrDefault(agentDbItem.getControllerId(), Optional.empty()))));
                }
            });
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
    
    private static Optional<JControllerState> getControllerState(String controllerId) {
        try {
            return Optional.of(Proxy.of(controllerId, 10).currentState());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    private static ControllerVersion createControllerVersion (DBItemInventoryJSInstance controllerDbItem, String jocVersion) {
        ControllerVersion controllerVersion = new ControllerVersion();
        controllerVersion.setControllerId(controllerDbItem.getControllerId());
        controllerVersion.setUri(controllerDbItem.getUri());
        controllerVersion.setVersion(controllerDbItem.getVersion());
        if (controllerDbItem.getVersion() != null) {
            controllerVersion.setCompatibility(CheckVersion.checkControllerVersionMatches2Joc(controllerVersion.getVersion(), jocVersion));
        }
        return controllerVersion;
    }
    
    private static AgentVersion createAgentVersion(DBItemInventoryAgentInstance agent, Optional<String> controllerVersion,
            Optional<JControllerState> controllerState) {
        AgentVersion agentVersion = new AgentVersion();
        agentVersion.setAgentId(agent.getAgentId());
        agentVersion.setSubagentId(null);
        agentVersion.setUri(agent.getUri());
        agentVersion.setVersion(agent.getVersion());
        if (agent.getVersion() == null) {
            controllerState.ifPresent(c -> {
                JAgentRefState state = c.pathToAgentRefState().get(AgentPath.of(agent.getAgentId()));
                if (state != null) {
                    AgentStoreUtils.updateAgentVersion(agent.getAgentId(), state.platformInfo());
                    state.platformInfo().map(PlatformInfo::js7Version).map(Version::string).ifPresent(
                            i -> agentVersion.setVersion(i));
                }
            });
        }
        if (agent.getVersion() != null) {
            controllerVersion.ifPresent(item -> agentVersion.setCompatibility(CheckVersion.checkAgentVersionMatches2Controller(agent.getVersion(),
                    item)));
        }
        return agentVersion;
    }

    private static AgentVersion createSubagentVersion(DBItemInventorySubAgentInstance subagent, Optional<String> controllerVersion,
            Optional<JControllerState> controllerState) {
        AgentVersion agentVersion = new AgentVersion();
        agentVersion.setAgentId(subagent.getAgentId());
        agentVersion.setSubagentId(subagent.getSubAgentId());
        agentVersion.setUri(subagent.getUri());
        agentVersion.setVersion(subagent.getVersion());
        if (subagent.getVersion() == null) {
            controllerState.ifPresent(c -> {
                JSubagentItemState state = c.idToSubagentItemState().get(SubagentId.of(subagent.getSubAgentId()));
                if (state != null) {
                    Optional<PlatformInfo> pOpt = OptionConverters.toJava(state.asScala().platformInfo());
                    AgentStoreUtils.updateSubagentVersion(subagent.getAgentId(), subagent.getSubAgentId(), pOpt);
                    pOpt.map(PlatformInfo::js7Version).map(Version::string).ifPresent(i -> agentVersion.setVersion(i));
                }
            });
        }
        if (subagent.getVersion() != null) {
            controllerVersion.ifPresent(item -> agentVersion.setCompatibility(CheckVersion.checkAgentVersionMatches2Controller(subagent.getVersion(),
                    item)));
        }
        return agentVersion;
    }

    private static Optional<String> getControllerVersion (List<ControllerVersion> controllerVersions, String controllerId) {
        return controllerVersions.stream()
                .filter(controllerVersion -> controllerId.equals(controllerVersion.getControllerId())).findAny().map(item -> item.getVersion());
    }
    
}
