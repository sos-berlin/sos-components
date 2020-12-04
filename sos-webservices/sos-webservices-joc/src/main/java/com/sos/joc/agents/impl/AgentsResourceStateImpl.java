package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceState;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.AgentState;
import com.sos.joc.model.agent.AgentStateText;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.agent.AgentsV;
import com.sos.joc.model.common.ControllerId;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.controller.data.agent.AgentRefState;
import js7.data.agent.AgentName;
import js7.proxy.javaapi.data.agent.JAgentRefState;
import js7.proxy.javaapi.data.controller.JControllerState;

@Path("agents")
public class AgentsResourceStateImpl extends JOCResourceImpl implements IAgentsResourceState {

    private static String API_CALL = "./agents";
    private static final Map<AgentStateText, Integer> agentStates = Collections.unmodifiableMap(new HashMap<AgentStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(AgentStateText.COUPLED, 0);
            put(AgentStateText.DECOUPLED, 1);
            put(AgentStateText.COUPLINGFAILED, 2);
            put(AgentStateText.UNKNOWN, 4);
        }
    });

    @Override
    public JOCDefaultResponse getState(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerId.class);
            ControllerId controllerParam = Globals.objectMapper.readValue(filterBytes, ControllerId.class);
            String controllerId = controllerParam.getControllerId();
            // TODO permissions?
            boolean permission = getPermissonsJocCockpit(controllerId, accessToken).getJS7Controller().getView().isStatus();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIds(Arrays.asList(controllerId), false, true);
            JControllerState currentState = Proxy.of(controllerId).currentState();

            AgentsV agents = new AgentsV();
            agents.setSurveyDate(Date.from(Instant.ofEpochMilli(currentState.eventId() / 1000)));
            agents.setAgents(dbAgents.stream().map(dbAgent -> {
                Either<Problem, JAgentRefState> either = currentState.nameToAgentRefState(AgentName.of(dbAgent.getAgentId()));
                AgentV agent = new AgentV();
                agent.setAgentId(dbAgent.getAgentId());
                agent.setAgentName(dbAgent.getAgentName());
                agent.setControllerId(controllerId);
                agent.setUrl(dbAgent.getUri());
                agent.setIsClusterWatcher(dbAgent.getIsWatcher());
                if (either.isRight()) {
                    AgentRefState.CouplingState couplingState = either.get().asScala().couplingState();
                    if (couplingState instanceof AgentRefState.CouplingFailed) {
                        agent.setErrorMessage(ProblemHelper.getErrorMessage(((AgentRefState.CouplingFailed) couplingState).problem()));
                        agent.setState(getState(AgentStateText.COUPLINGFAILED));
                    } else if (couplingState instanceof AgentRefState.Coupled$) {
                        agent.setState(getState(AgentStateText.COUPLED));
                    } else if (couplingState instanceof AgentRefState.Decoupled$) {
                        agent.setState(getState(AgentStateText.DECOUPLED));
                    }
                } else {
                    agent.setErrorMessage(ProblemHelper.getErrorMessage(either.getLeft()));
                    agent.setState(getState(AgentStateText.UNKNOWN));
                }
                return agent;
            }).filter(Objects::nonNull).collect(Collectors.toList()));
            agents.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(agents);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    private static AgentState getState(AgentStateText state) {
        AgentState s = new AgentState();
        s.set_text(state);
        s.setSeverity(agentStates.get(state));
        return s;
    }
}
