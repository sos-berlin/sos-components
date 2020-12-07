package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
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
import com.sos.joc.model.agent.ReadAgentsV;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.controller.data.agent.AgentRefState;
import js7.data.agent.AgentName;
import js7.data.order.Order;
import js7.proxy.javaapi.data.agent.JAgentRefState;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrderPredicates;

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
            JsonValidator.validateFailFast(filterBytes, ReadAgentsV.class);
            ReadAgentsV agentsParam = Globals.objectMapper.readValue(filterBytes, ReadAgentsV.class);
            String controllerId = agentsParam.getControllerId();
            // TODO permissions?
            boolean permission = getPermissonsJocCockpit(controllerId, accessToken).getJS7Controller().getView().isStatus();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("controllerId", controllerId);

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(controllerId, agentsParam.getAgentIds(), false,
                    agentsParam.getOnlyEnabledAgents());

            List<AgentV> agentsList = new ArrayList<>();

            boolean withStateFilter = agentsParam.getStates() != null && !agentsParam.getStates().isEmpty();
            AgentsV agents = new AgentsV();

            if (dbAgents != null) {

                JControllerState currentState = Proxy.of(controllerId).currentState();
                agents.setSurveyDate(Date.from(Instant.ofEpochMilli(currentState.eventId() / 1000)));

                Map<String, Integer> ordersCountPerAgent = new HashMap<>();
                Map<String, List<String>> ordersPerAgent = new HashMap<>();
                if (agentsParam.getCompact() == Boolean.TRUE) {
                    ordersCountPerAgent.putAll(currentState.ordersBy(JOrderPredicates.byOrderState(Order.Processing$.class)).filter(o -> o.attached()
                            .isRight()).collect(Collectors.groupingBy(o -> o.attached().get().string(), Collectors.reducing(0, o -> 1,
                                    Integer::sum))));
                } else {
                    ordersPerAgent.putAll(currentState.ordersBy(JOrderPredicates.byOrderState(Order.Processing$.class)).filter(o -> o.attached()
                            .isRight()).collect(Collectors.groupingBy(o -> o.attached().get().string(), Collectors.mapping(o -> o.id().string(),
                                    Collectors.toList()))));
                }

                agentsList.addAll(dbAgents.stream().map(dbAgent -> {
                    Either<Problem, JAgentRefState> either = currentState.nameToAgentRefState(AgentName.of(dbAgent.getAgentId()));
                    AgentV agent = new AgentV();
                    AgentStateText stateText = AgentStateText.UNKNOWN;
                    if (either.isRight()) {
                        AgentRefState.CouplingState couplingState = either.get().asScala().couplingState();
                        if (couplingState instanceof AgentRefState.CouplingFailed) {
                            stateText = AgentStateText.COUPLINGFAILED;
                            agent.setErrorMessage(ProblemHelper.getErrorMessage(((AgentRefState.CouplingFailed) couplingState).problem()));
                        } else if (couplingState instanceof AgentRefState.Coupled$) {
                            stateText = AgentStateText.COUPLED;
                        } else if (couplingState instanceof AgentRefState.Decoupled$) {
                            stateText = AgentStateText.DECOUPLED;
                        }
                    } else {
                        agent.setErrorMessage(ProblemHelper.getErrorMessage(either.getLeft()));
                    }
                    if (withStateFilter && !agentsParam.getStates().contains(stateText)) {
                        return null;
                    }
                    agent.setRunningTasks(0);
                    if (agentsParam.getCompact() == Boolean.TRUE) {
                        agent.setRunningTasks(ordersCountPerAgent.getOrDefault(dbAgent.getAgentId(), 0));
                        agent.setOrderIds(null);
                    } else {
                        if (ordersPerAgent.containsKey(dbAgent.getAgentId())) {
                            agent.setOrderIds(ordersPerAgent.get(dbAgent.getAgentId()));
                            agent.setRunningTasks(agent.getOrderIds().size());
                        }
                    }
                    agent.setAgentId(dbAgent.getAgentId());
                    agent.setAgentName(dbAgent.getAgentName());
                    agent.setControllerId(controllerId);
                    agent.setUrl(dbAgent.getUri());
                    agent.setIsClusterWatcher(dbAgent.getIsWatcher());
                    agent.setState(getState(stateText));
                    return agent;
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }

            agents.setDeliveryDate(Date.from(Instant.now()));
            agents.setAgents(agentsList);

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
