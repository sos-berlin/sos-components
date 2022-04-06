package com.sos.joc.agents.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceState;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.AgentClusterState;
import com.sos.joc.model.agent.AgentClusterStateText;
import com.sos.joc.model.agent.AgentState;
import com.sos.joc.model.agent.AgentStateText;
import com.sos.joc.model.agent.AgentStateV;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.agent.AgentsV;
import com.sos.joc.model.agent.AgentsVFlat;
import com.sos.joc.model.agent.ReadAgentsV;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.agent.SubagentV;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderV;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.agent.AgentPath;
import js7.data.agent.AgentRefState;
import js7.data.controller.ControllerCommand;
import js7.data.delegate.DelegateCouplingState;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.subagent.SubagentId;
import js7.data.subagent.SubagentItemState;
import js7.data_for_java.agent.JAgentRefState;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.subagent.JSubagentItemState;
import js7.proxy.javaapi.JControllerProxy;
import scala.compat.java8.OptionConverters;

@Path("agents")
public class AgentsResourceStateImpl extends JOCResourceImpl implements IAgentsResourceState {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsResourceStateImpl.class);
    private static final String API_CALL = "./agents";
    private static final Map<AgentStateText, Integer> agentStates = Collections.unmodifiableMap(new HashMap<AgentStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(AgentStateText.COUPLED, 0);
            put(AgentStateText.RESETTING, 1);
            put(AgentStateText.RESET, 1);
            put(AgentStateText.COUPLINGFAILED, 2);
            put(AgentStateText.SHUTDOWN, 2);
            put(AgentStateText.UNKNOWN, 2);
        }
    });
    private static final Map<AgentClusterStateText, Integer> agentHealthStates = Collections.unmodifiableMap(new HashMap<AgentClusterStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(AgentClusterStateText.ALL_SUBAGENTS_ARE_COUPLED, 0);
            put(AgentClusterStateText.ONLY_SOME_SUBAGENTS_ARE_COUPLED, 1);
            put(AgentClusterStateText.ALL_SUBAGENTS_ARE_RESET, 1);
            put(AgentClusterStateText.ONLY_SOME_SUBAGENTS_ARE_RESET, 1);
            put(AgentClusterStateText.SUBAGENTS_ARE_NOT_COUPLED, 2);
            put(AgentClusterStateText.UNKNOWN, 2);
        }
            });
    private static final Map<AgentStateText, AgentClusterStateText> agentStatesToHealthStates = Collections.unmodifiableMap(
            new HashMap<AgentStateText, AgentClusterStateText>() {

                private static final long serialVersionUID = 1L;

                {
                    put(AgentStateText.COUPLED, AgentClusterStateText.ALL_SUBAGENTS_ARE_COUPLED);
                    put(AgentStateText.RESETTING, AgentClusterStateText.ALL_SUBAGENTS_ARE_RESET);
                    put(AgentStateText.RESET, AgentClusterStateText.ALL_SUBAGENTS_ARE_RESET);
                    put(AgentStateText.COUPLINGFAILED, AgentClusterStateText.SUBAGENTS_ARE_NOT_COUPLED);
                    put(AgentStateText.SHUTDOWN, AgentClusterStateText.SUBAGENTS_ARE_NOT_COUPLED);
                    put(AgentStateText.UNKNOWN, AgentClusterStateText.UNKNOWN);
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
            boolean permission = getControllerPermissions(controllerId, accessToken).getAgents().getView();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            boolean withClusterLicense = AgentHelper.hasClusterLicense();
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(Collections.singleton(controllerId), agentsParam
                    .getAgentIds(), false, agentsParam.getOnlyEnabledAgents());
            Map<String, List<DBItemInventorySubAgentInstance>> dbSubagentsPerAgent = dbLayer.getSubAgentInstancesByControllerIds(Collections
                    .singleton(controllerId), false, agentsParam.getOnlyEnabledAgents());

            List<AgentV> agentsList = new ArrayList<>();
            Map<String, List<SubagentV>> subagentsPerAgentId = new HashMap<>();

            boolean withStateFilter = agentsParam.getStates() != null && !agentsParam.getStates().isEmpty();
            Instant currentStateMoment = null;

            if (dbAgents != null) {

                Map<String, Integer> ordersCountPerAgent = new HashMap<>();
                Map<String, List<OrderV>> ordersPerAgent = new HashMap<>();
                Map<String, Integer> ordersCountPerSubagent = new HashMap<>();
                try {
                    JControllerProxy proxy = Proxy.of(controllerId);
                    JControllerState currentState = proxy.currentState();
                    currentStateMoment = currentState.instant();
                    Long surveyDateMillis = currentStateMoment.toEpochMilli();
                    boolean olderThan30sec = currentStateMoment.isBefore(Instant.now().minusSeconds(30));
                    LOGGER.debug("current state older than 30sec? " + olderThan30sec + ",  Proxies.isCoupled? " + Proxies.isCoupled(controllerId));
                    if (!Proxies.isCoupled(controllerId)) {
                        JControllerCommand noOpCommand = JControllerCommand.apply(new ControllerCommand.NoOperation(OptionConverters.toScala(Optional
                                .empty())));
                        try {
                            Either<Problem, js7.data.controller.ControllerCommand.Response> anEither = proxy.api().executeCommand(noOpCommand).get(1,
                                    TimeUnit.SECONDS);
                            if (anEither.isRight()) {
                                Proxies.setCoupled(controllerId, true);
                                LOGGER.info("noOp answer: " + anEither.get().toString() + ",  Proxies.isCoupled? " + Proxies.isCoupled(controllerId));
                            }
                        } catch (Exception e) {
                        }
                    }

                    List<JOrder> jOrders = currentState.ordersBy(JOrderPredicates.byOrderState(Order.Processing.class)).filter(o -> o
                            .attached() != null && o.attached().isRight()).collect(Collectors.toList());
                    if (agentsParam.getCompact() == Boolean.TRUE) {
                        ordersCountPerAgent.putAll(jOrders.stream().collect(Collectors.groupingBy(o -> o.attached().get().string(), Collectors.reducing(0,
                                o -> 1, Integer::sum))));
                        if (withClusterLicense) {
                            ordersCountPerSubagent.putAll(jOrders.stream().collect(Collectors.groupingBy(o -> ((Order.Processing) o.asScala().state())
                                    .subagentId().get().string(), Collectors.reducing(0, o -> 1, Integer::sum))));
                        }

                    } else {
                        Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                        Set<OrderId> waitingOrders = OrdersHelper.getWaitingForAdmissionOrderIds(jOrders.stream().map(JOrder::id).collect(Collectors
                                .toSet()), currentState);
                        ordersPerAgent.putAll(jOrders.stream().map(o -> {
                            try {
                                return OrdersHelper.mapJOrderToOrderV(o, currentState, true, permittedFolders, waitingOrders, null, surveyDateMillis);
                            } catch (Exception e) {
                                return null;
                            }
                        }).filter(Objects::nonNull).collect(Collectors.groupingBy(OrderV::getAgentId)));
                    }

                    Map<AgentPath, JAgentRefState> agentRefStates = currentState.pathToAgentRefState();
                    Map<SubagentId, JSubagentItemState> subagentItemStates = currentState.idToSubagentItemState();

                    for (Map.Entry<String, List<DBItemInventorySubAgentInstance>> dbSubagents : dbSubagentsPerAgent.entrySet()) {

                        if (!withClusterLicense) {
                            subagentsPerAgentId.put(dbSubagents.getKey(), Collections.emptyList());
                        } else {

                            Map<String, List<OrderV>> ordersPerSubagent = ordersPerAgent.getOrDefault(dbSubagents.getKey(), Collections.emptyList())
                                    .stream().filter(o -> o.getSubagentId() != null && !o.getSubagentId().isEmpty()).collect(Collectors.groupingBy(
                                            OrderV::getSubagentId));

                            subagentsPerAgentId.put(dbSubagents.getKey(), dbSubagents.getValue().stream().sorted(Comparator.comparingInt(
                                    DBItemInventorySubAgentInstance::getOrdering)).map(dbSubAgent -> {
                                        SubagentV subagent = mapDbSubagentToAgentV(dbSubAgent);
                                        AgentStateText stateText = AgentStateText.UNKNOWN;
                                        if (Proxies.isCoupled(controllerId)) {
                                            if (SubagentDirectorType.NO_DIRECTOR.equals(dbSubAgent.getDirectorAsEnum())) {
                                                JSubagentItemState jSubagentItemState = subagentItemStates.get(SubagentId.of(dbSubAgent.getSubAgentId()));
                                                if (jSubagentItemState != null) {
                                                    LOGGER.debug("Subagent '" + dbSubAgent.getSubAgentId() + "',  state = " + jSubagentItemState
                                                            .toJson());
                                                    SubagentItemState subagentItemState = jSubagentItemState.asScala();
                                                    DelegateCouplingState couplingState = subagentItemState.couplingState();
                                                    Optional<Problem> optProblem = OptionConverters.toJava(subagentItemState.problem());
                                                    if (optProblem.isPresent()) {
                                                        subagent.setErrorMessage(ProblemHelper.getErrorMessage(optProblem.get()));
                                                    }
                                                    stateText = getAgentStateText(couplingState, optProblem);
                                                }
                                            } else {
                                                JAgentRefState jAgentRefState = agentRefStates.get(AgentPath.of(dbSubAgent.getAgentId()));
                                                if (jAgentRefState != null) {
                                                    LOGGER.debug("Agent '" + dbSubAgent.getAgentId() + "',  state = " + jAgentRefState.toJson());
                                                    AgentRefState agentRefState = jAgentRefState.asScala();
                                                    DelegateCouplingState couplingState = agentRefState.couplingState();
                                                    Optional<Problem> optProblem = OptionConverters.toJava(agentRefState.problem());
                                                    if (optProblem.isPresent()) {
                                                        subagent.setErrorMessage(ProblemHelper.getErrorMessage(optProblem.get()));
                                                    }
                                                    stateText = getAgentStateText(couplingState, optProblem);
                                                }
                                            }
                                        }
                                        if (withStateFilter && !agentsParam.getStates().contains(stateText)) {
                                            return null;
                                        }
                                        if (agentsParam.getCompact() == Boolean.TRUE) {
                                            subagent.setRunningTasks(ordersCountPerSubagent.getOrDefault(dbSubAgent.getSubAgentId(), 0));
                                            subagent.setOrders(null);
                                        } else {
                                            if (ordersPerSubagent.containsKey(dbSubAgent.getSubAgentId())) {
                                                subagent.setOrders(ordersPerSubagent.get(dbSubAgent.getSubAgentId()));
                                                subagent.setRunningTasks(subagent.getOrders().size());
                                            }
                                        }
                                        subagent.setState(getState(stateText));
                                        // Comparator.comparingInt(SubagentV::getRunningTasks).reversed()
                                        return subagent;
                                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                        }
                    }


                    agentsList.addAll(dbAgents.stream().map(dbAgent -> {
                        JAgentRefState jAgentRefState = agentRefStates.get(AgentPath.of(dbAgent.getAgentId()));
                        AgentV agent = mapDbAgentToAgentV(dbAgent, subagentsPerAgentId.get(dbAgent.getAgentId()));
                        if (agent.getSubagents() == null) { // only for standalone agent, cluster agents has no state (but its subagents)
                            AgentStateText stateText = AgentStateText.UNKNOWN;
                            if (Proxies.isCoupled(controllerId)) {
                                if (jAgentRefState != null) {
                                    LOGGER.debug("Agent '" + dbAgent.getAgentId() + "',  state = " + jAgentRefState.toJson());
                                    AgentRefState agentRefState = jAgentRefState.asScala();
                                    DelegateCouplingState couplingState = agentRefState.couplingState();
                                    Optional<Problem> optProblem = OptionConverters.toJava(agentRefState.problem());
                                    if (optProblem.isPresent()) {
                                        agent.setErrorMessage(ProblemHelper.getErrorMessage(optProblem.get()));
                                    }
                                    stateText = getAgentStateText(couplingState, optProblem);
                                }
                            }
                            if (withStateFilter && !agentsParam.getStates().contains(stateText)) {
                                return null;
                            }
                            if (agentsParam.getCompact() == Boolean.TRUE) {
                                agent.setRunningTasks(ordersCountPerAgent.getOrDefault(dbAgent.getAgentId(), 0));
                                agent.setOrders(null);
                            } else {
                                if (ordersPerAgent.containsKey(dbAgent.getAgentId())) {
                                    agent.setOrders(ordersPerAgent.get(dbAgent.getAgentId()));
                                    agent.setRunningTasks(agent.getOrders().size());
                                }
                            }
                            agent.setState(getState(stateText));
                            if (withClusterLicense) {
                                agent.setHealthState(getHealthState(stateText));
                            }
                        } else { // cluster agents (subagents are already filtered)
                            
                            if (!withClusterLicense || agentsParam.getFlat() == Boolean.TRUE) {
                                return null;
                            }
                            if (withStateFilter && agent.getSubagents().isEmpty()) {
                                return null;
                            }
                            agent.setRunningTasks(agent.getSubagents().stream().mapToInt(SubagentV::getRunningTasks).sum());
                            agent.setOrders(null);
                        }
                        return agent;
                    }).filter(Objects::nonNull).sorted(Comparator.comparingInt(AgentV::getRunningTasks).reversed()).collect(Collectors.toList()));

                } catch (ControllerConnectionRefusedException e1) {
                    for (Map.Entry<String, List<DBItemInventorySubAgentInstance>> dbSubagents : dbSubagentsPerAgent.entrySet()) {
                        subagentsPerAgentId.put(dbSubagents.getKey(), dbSubagents.getValue().stream().map(dbSubAgent -> {
                            SubagentV subagent = mapDbSubagentToAgentV(dbSubAgent);
                            if (withStateFilter && !agentsParam.getStates().contains(AgentStateText.UNKNOWN)) {
                                return null;
                            }
                            return subagent;
                        }).filter(Objects::nonNull).collect(Collectors.toList()));
                    }
                    agentsList.addAll(dbAgents.stream().map(dbAgent -> {
                        AgentV agent = mapDbAgentToAgentV(dbAgent, subagentsPerAgentId.get(dbAgent.getAgentId()));
                        if (agent.getSubagents() == null) { // only for standalone agent, cluster agents has no state (but its subagents)
                            if (withStateFilter && !agentsParam.getStates().contains(AgentStateText.UNKNOWN)) {
                                return null;
                            }
                        } else { // cluster agents (subagents are already filtered)
                            if (!withClusterLicense || (withStateFilter && agent.getSubagents().isEmpty())) {
                                return null;
                            }
                        }
                        return agent;
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                }
            }
            if (agentsParam.getFlat() == Boolean.TRUE) {
                AgentsVFlat agents = new AgentsVFlat();
                agents.setSurveyDate(currentStateMoment == null ? null : Date.from(currentStateMoment));
                agents.setDeliveryDate(Date.from(Instant.now()));
                agents.setAgents(Stream.concat(agentsList.stream(), subagentsPerAgentId.values().stream().flatMap(List::stream)).sorted(Comparator
                        .comparingInt(AgentStateV::getRunningTasks).reversed()).collect(Collectors.toList()));

                return JOCDefaultResponse.responseStatus200(agents);
            } else {
                AgentsV agents = new AgentsV();
                agents.setSurveyDate(currentStateMoment == null ? null : Date.from(currentStateMoment));
                agents.setDeliveryDate(Date.from(Instant.now()));
                agents.setAgents(agentsList);

                return JOCDefaultResponse.responseStatus200(agents);
            }
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
    
    private static AgentClusterState getHealthState(AgentClusterStateText state) {
        AgentClusterState s = new AgentClusterState();
        s.set_text(state);
        s.setSeverity(agentHealthStates.get(state));
        return s;
    }
    
    private static AgentClusterState getHealthState(AgentStateText state) {
        AgentClusterState s = new AgentClusterState();
        s.set_text(agentStatesToHealthStates.get(state));
        s.setSeverity(agentStates.get(state));
        return s;
    }
    
    private static AgentClusterState getHealthState(List<SubagentV> subagents) {
        AgentClusterStateText healthstate = AgentClusterStateText.UNKNOWN;
        if (subagents != null) {
            Set<AgentStateText> stateSeverities = subagents.stream().map(SubagentV::getState).map(AgentState::get_text).collect(Collectors.toSet());
            if (stateSeverities.contains(AgentStateText.COUPLED)) {
                healthstate = AgentClusterStateText.ALL_SUBAGENTS_ARE_COUPLED;
                if (stateSeverities.size() > 1) {
                    healthstate = AgentClusterStateText.ONLY_SOME_SUBAGENTS_ARE_COUPLED;
                }
            } else if (stateSeverities.contains(AgentStateText.RESET) || stateSeverities.contains(AgentStateText.RESETTING)) {
                healthstate = AgentClusterStateText.ALL_SUBAGENTS_ARE_RESET;
                if (stateSeverities.size() > 1) {
                    healthstate = AgentClusterStateText.ONLY_SOME_SUBAGENTS_ARE_RESET;
                }
            } else if (stateSeverities.contains(AgentStateText.COUPLINGFAILED) || stateSeverities.contains(AgentStateText.SHUTDOWN)) {
                healthstate = AgentClusterStateText.SUBAGENTS_ARE_NOT_COUPLED;
            }
        }
        return getHealthState(healthstate);
    }

    private static AgentV mapDbAgentToAgentV(DBItemInventoryAgentInstance dbAgent, List<SubagentV> subagents) {
        AgentV agent = new AgentV();
        agent.setRunningTasks(0);
        agent.setOrders(null);
        agent.setAgentId(dbAgent.getAgentId());
        agent.setAgentName(dbAgent.getAgentName());
        agent.setControllerId(dbAgent.getControllerId());
        agent.setSubagents(subagents);
        if (subagents == null) {
            agent.setIsClusterWatcher(dbAgent.getIsWatcher());
            agent.setUrl(dbAgent.getUri());
            agent.setState(getState(AgentStateText.UNKNOWN));
            agent.setHealthState(null);
        } else {
            agent.setIsClusterWatcher(null);
            agent.setUrl(null);
            agent.setState(null);
            agent.setHealthState(getHealthState(subagents));
        }
        return agent;
    }

    private static SubagentV mapDbSubagentToAgentV(DBItemInventorySubAgentInstance dbSubagent) {
        SubagentV agent = new SubagentV();
        agent.setRunningTasks(0);
        agent.setOrders(null);
        agent.setAgentId(dbSubagent.getAgentId());
        agent.setSubagentId(dbSubagent.getSubAgentId());
        agent.setUrl(dbSubagent.getUri());
        agent.setState(getState(AgentStateText.UNKNOWN));
        agent.setDisabled(dbSubagent.getDisabled());
        agent.setIsDirector(dbSubagent.getDirectorAsEnum());
        return agent;
    }

    private static AgentStateText getAgentStateText(DelegateCouplingState couplingState, Optional<Problem> optProblem) {
        if (couplingState instanceof DelegateCouplingState.ShutDown$) {
            return AgentStateText.SHUTDOWN;
        } else if (optProblem.isPresent()) {
            return AgentStateText.COUPLINGFAILED;
        } else if (couplingState instanceof DelegateCouplingState.Coupled$) {
            return AgentStateText.COUPLED;
        } else if (couplingState instanceof DelegateCouplingState.Resetting$) {
            return AgentStateText.RESETTING;
        } else if (couplingState instanceof DelegateCouplingState.Reset$) {
            return AgentStateText.RESET;
        }
        return AgentStateText.UNKNOWN;
    }
}
