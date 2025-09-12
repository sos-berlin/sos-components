package com.sos.joc.agents.impl;

import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
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
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.cluster.ClusterState;
import com.sos.controller.model.cluster.ClusterType;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceState;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.agent.AgentClusterWatch;
import com.sos.joc.classes.agent.AgentDirectorClusterState;
import com.sos.joc.classes.agent.AgentHelper;
import com.sos.joc.classes.controller.States;
import com.sos.joc.classes.order.OrderTags;
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
import com.sos.joc.model.agent.AgentConnectionState;
import com.sos.joc.model.agent.AgentConnectionStateText;
import com.sos.joc.model.agent.AgentState;
import com.sos.joc.model.agent.AgentStateReason;
import com.sos.joc.model.agent.AgentStateText;
import com.sos.joc.model.agent.AgentStateTextFilter;
import com.sos.joc.model.agent.AgentStateV;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.agent.AgentsV;
import com.sos.joc.model.agent.AgentsVFlat;
import com.sos.joc.model.agent.ReadAgentsV;
import com.sos.joc.model.agent.SubagentDirectorType;
import com.sos.joc.model.agent.SubagentV;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrderV;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.base.version.Version;
import js7.data.agent.AgentPath;
import js7.data.agent.AgentRefState;
import js7.data.cluster.ClusterWatchProblems;
import js7.data.controller.ControllerCommand;
import js7.data.delegate.DelegateCouplingState;
import js7.data.node.NodeId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.platform.PlatformInfo;
import js7.data.subagent.SubagentId;
import js7.data.subagent.SubagentItemState;
import js7.data_for_java.agent.JAgentRefState;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.subagent.JSubagentItemState;
import js7.proxy.javaapi.JControllerProxy;
import scala.collection.JavaConverters;
import scala.jdk.javaapi.OptionConverters;

@Path("agents")
public class AgentsResourceStateImpl extends JOCResourceImpl implements IAgentsResourceState {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsResourceStateImpl.class);
    private static final String API_CALL = "./agents";
    private static final Integer RED_SEVERITY = 2;
    private static final Integer ORANGE_SEVERITY = 5;
    private static final Integer OLIVGREEN_SEVERITY = 8;
    private static final Map<AgentStateText, Integer> agentStates = Collections.unmodifiableMap(new HashMap<AgentStateText, Integer>() {

        private static final long serialVersionUID = 2L;

        {
            put(AgentStateText.COUPLED, 0); // green
            put(AgentStateText.RESETTING, 1); // yellow
            put(AgentStateText.INITIALISED, 1);
            put(AgentStateText.COUPLINGFAILED, RED_SEVERITY); // obsolete
            put(AgentStateText.SHUTDOWN, RED_SEVERITY);
            put(AgentStateText.UNKNOWN, 4); // gray
        }
    });
    private static final Map<AgentConnectionStateText, Integer> agentConnectionStates = Collections.unmodifiableMap(
            new HashMap<AgentConnectionStateText, Integer>() {

                private static final long serialVersionUID = 1L;

                {
                    put(AgentConnectionStateText.NODE_LOSS, RED_SEVERITY);
                    put(AgentConnectionStateText.NOT_DEDICATED, ORANGE_SEVERITY);
                    put(AgentConnectionStateText.WITH_PERMANENT_ERROR, RED_SEVERITY);
                    put(AgentConnectionStateText.WITH_TEMPORARY_ERROR, OLIVGREEN_SEVERITY);
                }
            });
    private static final Map<AgentClusterStateText, Integer> agentHealthStates = Collections.unmodifiableMap(
            new HashMap<AgentClusterStateText, Integer>() {

                private static final long serialVersionUID = 2L;

                {
                    put(AgentClusterStateText.ALL_SUBAGENTS_ARE_COUPLED_AND_ENABLED, 0);
                    put(AgentClusterStateText.ONLY_SOME_SUBAGENTS_ARE_COUPLED_AND_ENABLED, 1);
                    put(AgentClusterStateText.NO_SUBAGENTS_ARE_COUPLED_AND_ENABLED, RED_SEVERITY);
                    put(AgentClusterStateText.UNKNOWN, 4); //grey
                }
            });
    private static final Map<String, AgentStateReason> agentStateReasons = Collections.unmodifiableMap(new HashMap<String, AgentStateReason>() {

        private static final long serialVersionUID = 1L;

        {
            put("fresh", AgentStateReason.FRESH);
            put("reset", AgentStateReason.RESET);
            put("resetcommand", AgentStateReason.RESET);
            put("restarted", AgentStateReason.RESTARTED);
        }
    });

    private static final Map<String, SubagentDirectorType> subagentDirectors = Collections.unmodifiableMap(
            new HashMap<String, SubagentDirectorType>() {

                private static final long serialVersionUID = 1L;

                {
                    put("primary", SubagentDirectorType.PRIMARY_DIRECTOR);
                    put("backup", SubagentDirectorType.SECONDARY_DIRECTOR);

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
                    .getAgentIds(), agentsParam.getOnlyVisibleAgents());
            Map<String, List<DBItemInventorySubAgentInstance>> dbSubagentsPerAgent = dbLayer.getSubAgentInstancesByControllerIds(Collections
                    .singleton(controllerId), agentsParam.getOnlyVisibleAgents());

            List<AgentV> agentsList = new ArrayList<>();
            Map<String, List<SubagentV>> subagentsPerAgentId = new HashMap<>();

            boolean withStateFilter = agentsParam.getStates() != null && !agentsParam.getStates().isEmpty();
            Instant currentStateMoment = null;
            ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();

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
                        ordersCountPerAgent.putAll(jOrders.stream().collect(Collectors.groupingBy(o -> o.attached().get().string(), Collectors
                                .reducing(0, o -> 1, Integer::sum))));
                        if (withClusterLicense) {
                            ordersCountPerSubagent.putAll(jOrders.stream().collect(Collectors.groupingBy(o -> ((Order.Processing) o.asScala().state())
                                    .subagentId().get().string(), Collectors.reducing(0, o -> 1, Integer::sum))));
                        }

                    } else {
                        Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                        Set<OrderId> waitingOrders = OrdersHelper.getWaitingForAdmissionOrderIds(jOrders.stream().map(JOrder::id).collect(Collectors
                                .toSet()), currentState);
                        Map<String, Set<String>> orderTags = OrderTags.getTags(controllerId, jOrders, connection);

                        ordersPerAgent.putAll(jOrders.stream().map(o -> {
                            try {
                                return OrdersHelper.mapJOrderToOrderV(o, currentState, true, permittedFolders, orderTags, waitingOrders, null,
                                        surveyDateMillis, zoneId);
                            } catch (Exception e) {
                                return null;
                            }
                        }).filter(Objects::nonNull).collect(Collectors.groupingBy(OrderV::getAgentId)));
                    }

                    Map<AgentPath, JAgentRefState> agentRefStates = currentState.pathToAgentRefState();
                    Map<SubagentId, JSubagentItemState> subagentItemStates = currentState.idToSubagentItemState();
                    Map<String, AgentDirectorClusterState> agentClusterStates = new HashMap<>();

                    for (Map.Entry<String, List<DBItemInventorySubAgentInstance>> dbSubagents : dbSubagentsPerAgent.entrySet()) {

                        if (!withClusterLicense) {
                            subagentsPerAgentId.put(dbSubagents.getKey(), Collections.emptyList());
                        } else {

                            Map<String, List<OrderV>> ordersPerSubagent = ordersPerAgent.getOrDefault(dbSubagents.getKey(), Collections.emptyList())
                                    .stream().filter(o -> o.getSubagentId() != null && !o.getSubagentId().isEmpty()).collect(Collectors.groupingBy(
                                            OrderV::getSubagentId));

                            JAgentRefState jAgentRefState = agentRefStates.get(AgentPath.of(dbSubagents.getKey()));
                            AgentDirectorClusterState clusterState = getClusterState(jAgentRefState);
                            agentClusterStates.put(dbSubagents.getKey(), clusterState);

                            Optional<String> agentStateRefProblem = Optional.ofNullable(jAgentRefState).map(JAgentRefState::asScala).map(
                                    AgentRefState::problem).map(OptionConverters::toJava).flatMap(Function.identity()).map(
                                            ProblemHelper::getErrorMessage);

                            subagentsPerAgentId.put(dbSubagents.getKey(), dbSubagents.getValue().stream().sorted(Comparator.comparingInt(
                                    DBItemInventorySubAgentInstance::getOrdering)).map(dbSubAgent -> {
                                        SubagentV subagent = mapDbSubagentToAgentV(dbSubAgent);

                                        Optional<SubagentDirectorType> subagentIsLost = Optional.empty();
                                        if (clusterState != null) {
                                            if (ClusterType.NODE_LOSS_TO_BE_CONFIRMED.equals(clusterState.getTYPE()) && clusterState
                                                    .getLostNodeId() != null) {
                                                if (subagent.getIsDirector().equals(subagentDirectors.get(clusterState.getLostNodeId()
                                                        .toLowerCase()))) {
                                                    subagentIsLost = Optional.of(subagent.getIsDirector());
                                                }
                                            } else {
                                                AgentClusterWatch.clean(controllerId, AgentPath.of(dbSubagents.getKey()));
                                            }
                                        }

                                        if (Proxies.isCoupled(controllerId)) {
                                            if (SubagentDirectorType.NO_DIRECTOR.equals(dbSubAgent.getDirectorAsEnum())) {
                                                addSubagentState(subagent, dbSubAgent, subagentItemStates, clusterState, subagentIsLost,
                                                        agentStateRefProblem, false);

                                            } else {
                                                boolean isActive = isActive(clusterState, dbSubAgent);
                                                subagent.setClusterNodeState(States.getClusterNodeState(isActive));
                                                if (subagentIsLost.isPresent()) {
                                                    subagent.setClusterNodeState(States.getClusterNodeState(false));
                                                }
                                                if (isActive && jAgentRefState != null) {
                                                    LOGGER.debug("Agent '" + dbSubAgent.getAgentId() + "',  state = " + jAgentRefState.toJson());
                                                    AgentRefState agentRefState = jAgentRefState.asScala();
                                                    DelegateCouplingState couplingState = agentRefState.couplingState();
                                                    OptionConverters.toJava(agentRefState.platformInfo()).map(PlatformInfo::js7Version).map(
                                                            Version::string).ifPresent(i -> subagent.setVersion(i));
                                                    Optional<Problem> optProblem = OptionConverters.toJava(agentRefState.problem());
                                                    subagent.setConnectionState(getAgentConnectionState(optProblem.map(
                                                            ProblemHelper::getErrorMessage), subagentIsLost, Optional.ofNullable(clusterState)));

                                                    AgentState aState = getAgentState(couplingState, Optional.ofNullable(subagent
                                                            .getConnectionState()));
                                                    subagent.setState(aState);
                                                } else {
                                                    addSubagentState(subagent, dbSubAgent, subagentItemStates, clusterState, subagentIsLost,
                                                            agentStateRefProblem, false);
                                                }
                                            }
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
                                        return subagent;
                                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                        }
                    }

                    agentsList.addAll(dbAgents.stream().map(dbAgent -> {
                        JAgentRefState jAgentRefState = agentRefStates.get(AgentPath.of(dbAgent.getAgentId()));
                        AgentV agent = mapDbAgentToAgentV(dbAgent, subagentsPerAgentId.get(dbAgent.getAgentId()), withStateFilter, agentsParam
                                .getStates());
                        if (agent.getSubagents() == null) { // only for standalone agent, cluster agents has no state (but its subagents)
                            if (Proxies.isCoupled(controllerId)) {
                                if (jAgentRefState != null) {
                                    LOGGER.debug("Agent '" + dbAgent.getAgentId() + "',  state = " + jAgentRefState.toJson());
                                    AgentRefState agentRefState = jAgentRefState.asScala();
                                    DelegateCouplingState couplingState = agentRefState.couplingState();
                                    OptionConverters.toJava(agentRefState.platformInfo()).map(PlatformInfo::js7Version).map(Version::string)
                                            .ifPresent(i -> agent.setVersion(i));
                                    Optional<Problem> optProblem = OptionConverters.toJava(agentRefState.problem());
                                    agent.setConnectionState(getAgentConnectionState(optProblem));
                                    agent.setState(getAgentState(couplingState, Optional.ofNullable(agent.getConnectionState())));
                                }
                            } else {
                                agent.setState(getState(AgentStateText.UNKNOWN));
                            }
                            if (withStateFilter && !agentsParam.getStates().contains(agent.getStateTextFilter())) {
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
                            if (withClusterLicense) {
                                agent.setHealthState(getHealthState(agent.getStateTextFilter(), dbAgent.getDisabled() == Boolean.TRUE));
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

                            // JOC-1611; clusterState only if two Directors defined in ClusterAgent
                            if (agent.getSubagents().stream().map(SubagentV::getIsDirector).filter(t -> !SubagentDirectorType.NO_DIRECTOR.equals(t))
                                    .mapToInt(d -> 1).sum() > 1) {
                                agent.setClusterState(States.getClusterState(getClusterStateType(agentClusterStates.get(dbAgent.getAgentId())),
                                        getClusterLostNodeId(agentClusterStates.get(dbAgent.getAgentId()))));
                            }
                        }
                        return agent;
                    }).filter(Objects::nonNull).sorted(Comparator.comparingInt(AgentV::getRunningTasks).reversed()).collect(Collectors.toList()));

                    // agentClusterStates.entrySet().stream().filter(e -> e.getValue().getLostNodeId() != null).collect(Collectors.joining(controllerId,
                    // accessToken, controllerId))

                } catch (ControllerConnectionRefusedException e1) {
                    for (Map.Entry<String, List<DBItemInventorySubAgentInstance>> dbSubagents : dbSubagentsPerAgent.entrySet()) {
                        subagentsPerAgentId.put(dbSubagents.getKey(), dbSubagents.getValue().stream().map(dbSubAgent -> {
                            SubagentV subagent = mapDbSubagentToAgentV(dbSubAgent);
                            if (withStateFilter && !agentsParam.getStates().contains(AgentStateTextFilter.UNKNOWN)) {
                                return null;
                            }
                            return subagent;
                        }).filter(Objects::nonNull).collect(Collectors.toList()));
                    }
                    agentsList.addAll(dbAgents.stream().map(dbAgent -> {
                        AgentV agent = mapDbAgentToAgentV(dbAgent, subagentsPerAgentId.get(dbAgent.getAgentId()), withStateFilter, agentsParam
                                .getStates());
                        if (agent.getSubagents() == null) { // only for standalone agent, cluster agents has no state (but its subagents)
                            if (withStateFilter && !agentsParam.getStates().contains(AgentStateTextFilter.UNKNOWN)) {
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

                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(agents));
            } else {
                AgentsV agents = new AgentsV();
                agents.setSurveyDate(currentStateMoment == null ? null : Date.from(currentStateMoment));
                agents.setDeliveryDate(Date.from(Instant.now()));
                agents.setAgents(agentsList);

                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(agents));
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

    private static AgentState getState(AgentStateText state) {
        AgentState s = new AgentState();
        s.set_text(state);
        s.setSeverity(agentStates.get(state));
        s.set_reason(null);
        return s;
    }

    private static AgentState getState(AgentStateText state, String stateReason) {
        AgentState s = new AgentState();
        s.set_text(state);
        s.setSeverity(agentStates.get(state));
        s.set_reason(agentStateReasons.get(stateReason.toLowerCase()));
        return s;
    }

    private static AgentConnectionState getConnectionState(AgentConnectionStateText state, String msg) {
        AgentConnectionState s = new AgentConnectionState();
        s.set_text(state);
        s.setSeverity(agentConnectionStates.get(state));
        s.setErrorMessage(msg);
        return s;
    }

    private static AgentClusterState getHealthState(AgentClusterStateText state) {
        AgentClusterState s = new AgentClusterState();
        s.set_text(state);
        s.setSeverity(agentHealthStates.get(state));
        return s;
    }

    private static AgentClusterState getHealthState(AgentStateTextFilter state, boolean disabled) {
        AgentClusterState s = new AgentClusterState();
        if (AgentStateTextFilter.COUPLED.equals(state) && !disabled) {
            s.set_text(AgentClusterStateText.ALL_SUBAGENTS_ARE_COUPLED_AND_ENABLED);
        } else if (AgentStateTextFilter.UNKNOWN.equals(state) && !disabled) {
            s.set_text(AgentClusterStateText.UNKNOWN);
        } else {
            s.set_text(AgentClusterStateText.NO_SUBAGENTS_ARE_COUPLED_AND_ENABLED);
        }
        s.setSeverity(agentHealthStates.get(s.get_text()));
        return s;
    }

    private static AgentClusterState getHealthState(List<SubagentV> subagents) {
        AgentClusterStateText healthstate = AgentClusterStateText.UNKNOWN;
        if (subagents != null) {
            List<AgentStateTextFilter> notCoupled = Arrays.asList(AgentStateTextFilter.COUPLINGFAILED, AgentStateTextFilter.SHUTDOWN,
                    AgentStateTextFilter.UNKNOWN);
            if (subagents.stream().map(SubagentV::getStateTextFilter).allMatch(AgentStateTextFilter.UNKNOWN::equals)) {
                healthstate = AgentClusterStateText.UNKNOWN;
            } else if (subagents.stream().map(SubagentV::getStateTextFilter).allMatch(notCoupled::contains)) { // SHUTDOWN, COUPLING_FAILED OR UNKNOWN
                healthstate = AgentClusterStateText.NO_SUBAGENTS_ARE_COUPLED_AND_ENABLED;
            } else if (subagents.stream().map(SubagentV::getStateTextFilter).allMatch(AgentStateTextFilter.COUPLED::equals)) { // severity 0 means COUPLED
                int numOfDisabledSubagents = subagents.stream().filter(SubagentV::getDisabled).mapToInt(e -> 1).sum();
                if (numOfDisabledSubagents == 0) {
                    healthstate = AgentClusterStateText.ALL_SUBAGENTS_ARE_COUPLED_AND_ENABLED;
                } else if (subagents.size() == numOfDisabledSubagents) {
                    healthstate = AgentClusterStateText.NO_SUBAGENTS_ARE_COUPLED_AND_ENABLED;
                } else {
                    healthstate = AgentClusterStateText.ONLY_SOME_SUBAGENTS_ARE_COUPLED_AND_ENABLED;
                }
            } else {
                healthstate = AgentClusterStateText.ONLY_SOME_SUBAGENTS_ARE_COUPLED_AND_ENABLED;
            }
        }
        return getHealthState(healthstate);
    }
    
//    private static boolean filterState(SubagentV subagent, List<AgentStateTextFilter> stateFilter) {
//        Optional<AgentStateTextFilter> couplingFailed = Optional.ofNullable(subagent.getConnectionState()).map(AgentConnectionState::get_text).filter(
//                s -> !AgentConnectionStateText.WITH_TEMPORARY_ERROR.equals(s)).map(s -> AgentStateTextFilter.COUPLINGFAILED);
//        AgentStateTextFilter state = null;
//        if (couplingFailed.isPresent()) {
//            state = couplingFailed.get();
//        } else {
//            try {
//                state = AgentStateTextFilter.fromValue(subagent.getState().get_text().value());
//            } catch (Exception e) {
//                //
//            }
//        }
//        return state != null && stateFilter.contains(state);
//    }

    private static AgentV mapDbAgentToAgentV(DBItemInventoryAgentInstance dbAgent, List<SubagentV> subagents, boolean withStateFilter,
            List<AgentStateTextFilter> stateFilter) {
        AgentV agent = new AgentV();
        agent.setRunningTasks(0);
        agent.setOrders(null);
        agent.setAgentId(dbAgent.getAgentId());
        agent.setAgentName(dbAgent.getAgentName());
        agent.setControllerId(dbAgent.getControllerId());
        agent.setProcessLimit(dbAgent.getProcessLimit());
        if (subagents == null) {
            agent.setSubagents(null);
            agent.setUrl(dbAgent.getUri());
            agent.setState(getState(AgentStateText.UNKNOWN));
            agent.setHealthState(null); // will be set later for standalone agents
            agent.setDisabled(dbAgent.getDisabled());
        } else {
            if (withStateFilter) {
                agent.setSubagents(subagents.stream().filter(s -> stateFilter.contains(s.getStateTextFilter())).collect(Collectors.toList()));
            } else {
                agent.setSubagents(subagents);
            }
            agent.setUrl(null);
            agent.setState(null);
            agent.setHealthState(getHealthState(subagents));
            agent.setClusterState(null);
            agent.setDisabled(null);
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

    private static AgentState getAgentState(DelegateCouplingState couplingState, Optional<AgentConnectionState> connectionState) {
        AgentState agentState = getState(AgentStateText.UNKNOWN);
        if (couplingState instanceof DelegateCouplingState.ShutDown$) {
            agentState = getState(AgentStateText.SHUTDOWN);
        } else if (couplingState instanceof DelegateCouplingState.Coupled$) {
            agentState = getState(AgentStateText.COUPLED); // green per default
            connectionState.map(AgentConnectionState::getSeverity).ifPresent(agentState::setSeverity);
        } else if (couplingState instanceof DelegateCouplingState.Resetting) {
            agentState = getState(AgentStateText.RESETTING);
        } else if (couplingState instanceof DelegateCouplingState.Reset) {
            String reason = ((DelegateCouplingState.Reset) couplingState).reason().string();
            if (reason.equalsIgnoreCase("Shutdown")) {
                agentState = getState(AgentStateText.SHUTDOWN);
            } else {
                agentState = getState(AgentStateText.INITIALISED, reason);
            }
        }
        return agentState;
    }

    private static AgentConnectionState getAgentConnectionState(Optional<Problem> optProblem) {
        return getAgentConnectionState(optProblem.map(ProblemHelper::getErrorMessage), Optional.empty(), Optional.empty());
    }

    private static AgentConnectionState getAgentConnectionState(Optional<String> optProblem, Optional<SubagentDirectorType> subagentIsLost,
            Optional<AgentDirectorClusterState> clusterState) {
        if (optProblem.isPresent()) {
            String errorMessage = optProblem.get();
            if (optProblem.get().toLowerCase().contains("missing heartbeat")) {
                return getConnectionState(AgentConnectionStateText.WITH_TEMPORARY_ERROR, errorMessage);
            } else if (optProblem.get().toLowerCase().contains("dedicated")) {
                return getConnectionState(AgentConnectionStateText.NOT_DEDICATED, errorMessage);
            } else {
                return getConnectionState(AgentConnectionStateText.WITH_PERMANENT_ERROR, errorMessage);
            }
        } else if (subagentIsLost.isPresent()) {
            return getConnectionState(AgentConnectionStateText.NODE_LOSS, clusterState.flatMap(AgentDirectorClusterState::getLostNodeProblem).orElse(
                    "ClusterNodeLossNotConfirmed: This director is lost. Requires user confirmation"));
        }
        return null;
    }

    private static AgentDirectorClusterState getClusterState(JAgentRefState jAgentRefState) {
        AgentDirectorClusterState clusterState = null;
        if (jAgentRefState != null) {
            try {
                clusterState = Globals.objectMapper.readValue(jAgentRefState.clusterState().toJson(), AgentDirectorClusterState.class);
                Map<NodeId, ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem> lostNodeIds = JavaConverters.asJava(jAgentRefState.asScala()
                        .nodeToLossNotConfirmedProblem());
                if (!lostNodeIds.isEmpty()) {
                    clusterState.setTYPE(ClusterType.NODE_LOSS_TO_BE_CONFIRMED);
                    ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem problem = lostNodeIds.values().iterator().next();
                    clusterState.setLostNodeId(problem.event().lostNodeId().string());
                    clusterState.setLostNodeProblem(ProblemHelper.getErrorMessage(problem));
                }
            } catch (Exception e) {
                LOGGER.error("", e);
            }
        }
        return clusterState;
    }

    private static ClusterType getClusterStateType(AgentDirectorClusterState clusterState) {
        return clusterState == null ? null : clusterState.getTYPE();
    }

    private static Optional<String> getClusterLostNodeId(AgentDirectorClusterState clusterState) {
        return clusterState == null || clusterState.getLostNodeId() == null ? Optional.empty() : Optional.of(clusterState.getLostNodeId());
    }

    private static boolean isActive(ClusterState clusterState, DBItemInventorySubAgentInstance subagent) {
        boolean isActive = false;
        if (clusterState != null) {
            switch (clusterState.getTYPE()) {
            case EMPTY:
                isActive = true;
                break;
            default:
                try {
                    String activeClusterUri = clusterState.getSetting().getIdToUri().getAdditionalProperties().get(clusterState.getSetting()
                            .getActiveId());
                    isActive = activeClusterUri.equalsIgnoreCase(subagent.getUri());
                    if (!isActive && subagent.getDirectorAsEnum().equals(subagentDirectors.get(clusterState.getSetting().getActiveId()
                            .toLowerCase()))) {
                        isActive = true;
                    }
                } catch (Exception e) {
                    LOGGER.warn("Couldn't determine if subagent '" + subagent.getSubAgentId() + "' is active or not: " + clusterState.toString());
                }
                break;
            }
        }
        return isActive;
    }

    private static SubagentV addSubagentState(SubagentV subagent, DBItemInventorySubAgentInstance dbSubAgent,
            Map<SubagentId, JSubagentItemState> subagentItemStates, AgentDirectorClusterState clusterState,
            Optional<SubagentDirectorType> subagentIsLost, Optional<String> agentStateRefProblem, boolean isActiveDirector) {
        AgentState agentState = getState(AgentStateText.UNKNOWN);
        JSubagentItemState jSubagentItemState = subagentItemStates.get(SubagentId.of(dbSubAgent.getSubAgentId()));
        if (jSubagentItemState != null) {
            LOGGER.debug("Subagent '" + dbSubAgent.getSubAgentId() + "',  state = " + jSubagentItemState.toJson());
            SubagentItemState subagentItemState = jSubagentItemState.asScala();
            DelegateCouplingState couplingState = subagentItemState.couplingState();
            OptionConverters.toJava(subagentItemState.platformInfo()).map(PlatformInfo::js7Version).map(Version::string).ifPresent(i -> subagent
                    .setVersion(i));
            Optional<String> optProblem = OptionConverters.toJava(subagentItemState.problem()).map(ProblemHelper::getErrorMessage);
            if (agentStateRefProblem.isPresent()) {
                optProblem = Optional.of(agentStateRefProblem.get());
            }
            subagent.setConnectionState(getAgentConnectionState(optProblem, subagentIsLost, Optional.ofNullable(clusterState)));
            if (isActiveDirector || !agentStateRefProblem.isPresent()) {
                agentState = getAgentState(couplingState, Optional.ofNullable(subagent.getConnectionState()));
            }
        }
        subagent.setState(agentState);
        if (AgentStateText.UNKNOWN.equals(agentState.get_text())) {
            subagent.setStateTextFilter(AgentStateTextFilter.UNKNOWN);
            subagent.setErrorMessage(null);
        } else if (AgentStateText.SHUTDOWN.equals(agentState.get_text())) {
            subagent.setStateTextFilter(AgentStateTextFilter.SHUTDOWN);
        } else if (AgentStateText.INITIALISED.equals(agentState.get_text())) {
            subagent.setStateTextFilter(AgentStateTextFilter.INITIALISED);
        } else if (AgentStateText.RESETTING.equals(agentState.get_text())) {
            subagent.setStateTextFilter(AgentStateTextFilter.RESETTING);
        }
        return subagent;
    }

}
