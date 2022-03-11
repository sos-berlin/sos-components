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
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.agent.AgentState;
import com.sos.joc.model.agent.AgentStateText;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.agent.AgentsV;
import com.sos.joc.model.agent.ReadAgentsV;
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
import js7.data_for_java.agent.JAgentRefState;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
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

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(Collections.singleton(controllerId),
                    agentsParam.getAgentIds(), false, agentsParam.getOnlyEnabledAgents());

            List<AgentV> agentsList = new ArrayList<>();

            boolean withStateFilter = agentsParam.getStates() != null && !agentsParam.getStates().isEmpty();
            AgentsV agents = new AgentsV();

            if (dbAgents != null) {

                Map<String, Integer> ordersCountPerAgent = new HashMap<>();
                Map<String, List<OrderV>> ordersPerAgent = new HashMap<>();
                try {
                    JControllerProxy proxy = Proxy.of(controllerId);
                    JControllerState currentState = proxy.currentState();
                    Instant currentStateMoment = currentState.instant();
                    Long surveyDateMillis = currentStateMoment.toEpochMilli();
                    boolean olderThan30sec = currentStateMoment.isBefore(Instant.now().minusSeconds(30));
                    LOGGER.debug("current state older than 30sec? " + olderThan30sec + ",  Proxies.isCoupled? " + Proxies.isCoupled(controllerId));
                    if (!Proxies.isCoupled(controllerId)) {
                        JControllerCommand noOpCommand = JControllerCommand.apply(new ControllerCommand.NoOperation(OptionConverters.toScala(Optional.empty())));
                        try {
                            Either<Problem, js7.data.controller.ControllerCommand.Response> anEither = proxy.api().executeCommand(noOpCommand).get(
                                    1, TimeUnit.SECONDS);
                            if (anEither.isRight()) {
                                Proxies.setCoupled(controllerId, true);
                                LOGGER.info("noOp answer: " + anEither.get().toString() + ",  Proxies.isCoupled? " + Proxies.isCoupled(controllerId));
                            }
                        } catch (Exception e) {
                        }
                    }
                    agents.setSurveyDate(Date.from(currentStateMoment));
                    
                    Stream<JOrder> jOrderStream = currentState.ordersBy(JOrderPredicates.byOrderState(Order.Processing.class)).filter(o -> o
                            .attached() != null && o.attached().isRight());
                    if (agentsParam.getCompact() == Boolean.TRUE) {
                        ordersCountPerAgent.putAll(jOrderStream.collect(Collectors.groupingBy(o -> o.attached().get().string(), Collectors.reducing(0,
                                o -> 1, Integer::sum))));
                    } else {
                        Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
                        List<JOrder> jOrders = jOrderStream.collect(Collectors.toList());
                        Set<OrderId> waitingOrders = OrdersHelper.getWaitingForAdmissionOrderIds(jOrders.stream().map(JOrder::id).collect(Collectors
                                .toSet()), currentState);
                        ordersPerAgent.putAll(jOrders.stream().map(o -> {
                            try {
                                // TODO remove final Parameters 
                                return OrdersHelper.mapJOrderToOrderV(o, currentState, true, permittedFolders, waitingOrders, null, surveyDateMillis);
                            } catch (Exception e) {
                                return null;
                            }
                        }).filter(Objects::nonNull).collect(Collectors.groupingBy(OrderV::getAgentId)));
                    }
                    
                    agentsList.addAll(dbAgents.stream().map(dbAgent -> {
                        Either<Problem, JAgentRefState> either = currentState.pathToAgentRefState(AgentPath.of(dbAgent.getAgentId()));
                        AgentV agent = mapDbAgentToAgentV(dbAgent);
                        AgentStateText stateText = AgentStateText.UNKNOWN;
                        if (Proxies.isCoupled(controllerId)) {
                        //if (!olderThan30sec || Proxies.isCoupled(controllerId)) {
                            if (either.isRight()) {
                                LOGGER.debug("Agent '" + dbAgent.getAgentId() + "',  state = " + either.get().toJson());
                                AgentRefState agentRefState = either.get().asScala();
                                DelegateCouplingState couplingState = agentRefState.couplingState();
                                Optional<Problem> optProblem = OptionConverters.toJava(agentRefState.problem());
                                if (optProblem.isPresent()) {
                                    agent.setErrorMessage(ProblemHelper.getErrorMessage(optProblem.get()));
                                }
                                if (couplingState instanceof DelegateCouplingState.ShutDown$) {
                                    stateText = AgentStateText.SHUTDOWN;
                                } else if (optProblem.isPresent()) {
                                    stateText = AgentStateText.COUPLINGFAILED;
                                } else if (couplingState instanceof DelegateCouplingState.Coupled$) {
                                    stateText = AgentStateText.COUPLED;
                                } else if (couplingState instanceof DelegateCouplingState.Resetting$) {
                                    stateText = AgentStateText.RESETTING;
                                } else if (couplingState instanceof DelegateCouplingState.Reset$) {
                                    stateText = AgentStateText.RESET;
                                }
                            } else {
                                LOGGER.debug("Agent '" + dbAgent.getAgentId() + "',  problem = " + either.getLeft().messageWithCause());
                                agent.setErrorMessage(ProblemHelper.getErrorMessage(either.getLeft()));
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
                        return agent;
                    }).filter(Objects::nonNull).sorted(Comparator.comparingInt(AgentV::getRunningTasks).reversed()).collect(Collectors.toList()));
                    
                } catch (ControllerConnectionRefusedException e1) {
                    agentsList.addAll(dbAgents.stream().map(dbAgent -> {
                        AgentV agent = mapDbAgentToAgentV(dbAgent);
                        if (withStateFilter && !agentsParam.getStates().contains(AgentStateText.UNKNOWN)) {
                            return null;
                        }
                        return agent;
                    }).filter(Objects::nonNull).collect(Collectors.toList()));
                }
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
    
    private static AgentV mapDbAgentToAgentV(DBItemInventoryAgentInstance dbAgent) {
        AgentV agent = new AgentV();
        agent.setRunningTasks(0);
        agent.setOrders(null);
        agent.setAgentId(dbAgent.getAgentId());
        agent.setAgentName(dbAgent.getAgentName());
        agent.setUrl(dbAgent.getUri());
        agent.setControllerId(dbAgent.getControllerId());
        agent.setIsClusterWatcher(dbAgent.getIsWatcher());
        agent.setState(getState(AgentStateText.UNKNOWN));
        return agent;
    }
}
