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
import java.util.stream.Stream;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.agents.resource.IAgentsResourceTasks;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.model.agent.AgentTaskOrder;
import com.sos.joc.model.agent.AgentTasks;
import com.sos.joc.model.agent.AgentsTasks;
import com.sos.joc.model.agent.ReadAgentsV;
import com.sos.joc.model.audit.CategoryType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.order.Order;
import js7.data.subagent.SubagentId;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import scala.Option;

@Path("agents")
public class AgentsResourceTasksImpl extends JOCResourceImpl implements IAgentsResourceTasks {

    private static final String API_CALL = "./agents/tasks";
    
    // obsolete: the same like ./agents with compact=false

    @Override
    public JOCDefaultResponse getTasks(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, ReadAgentsV.class);
            ReadAgentsV agentsParam = Globals.objectMapper.readValue(filterBytes, ReadAgentsV.class);
            String controllerId = agentsParam.getControllerId();
            boolean permission = getBasicControllerPermissions(controllerId, accessToken).getOrders().getView();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(connection);
            dbLayer.setWithAgentOrdering(true);
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIdAndAgentIds(Collections.singleton(controllerId),
                    agentsParam.getAgentIds(), agentsParam.getOnlyVisibleAgents());

            List<AgentTasks> agentsList = new ArrayList<>();
            AgentsTasks agents = new AgentsTasks();

            if (dbAgents != null) {
                JControllerState currentState = Proxy.of(controllerId).currentState();
                agents.setSurveyDate(Date.from(currentState.instant()));
                Map<String, Integer> ordersCountPerAgent = new HashMap<>();
                Map<String, List<AgentTaskOrder>> ordersPerAgent = new HashMap<>();
                Stream<JOrder> jOrderStream = currentState.ordersBy(JOrderPredicates.byOrderState(Order.Processing.class)).filter(o -> o
                        .attached() != null && o.attached().isRight());
                if (agentsParam.getCompact() == Boolean.TRUE) {
                    ordersCountPerAgent.putAll(jOrderStream.collect(Collectors.groupingBy(o -> o.attached().get().string(), Collectors.reducing(0,
                            o -> 1, Integer::sum))));
                } else {
                    ordersPerAgent.putAll(jOrderStream.collect(Collectors.groupingBy(o -> o.attached().get().string(), Collectors.mapping(o -> {
                        AgentTaskOrder ao = new AgentTaskOrder();
                        ao.setOrderId(o.id().string());
                        Option<SubagentId> subAgentId = ((Order.Processing) o.asScala().state()).subagentId();
                        if (subAgentId.nonEmpty()) {
                            ao.setSubagentId(subAgentId.get().string());
                        }
                        return ao;
                    }, Collectors.toList()))));
                }

                agentsList.addAll(dbAgents.stream().map(dbAgent -> {
                    AgentTasks agent = new AgentTasks();
                    agent.setAgentId(dbAgent.getAgentId());
                    agent.setAgentName(dbAgent.getAgentName());
                    agent.setControllerId(controllerId);
                    agent.setProcessLimit(dbAgent.getProcessLimit());
                    agent.setRunningTasks(0);
                    if (agentsParam.getCompact() == Boolean.TRUE) {
                        agent.setRunningTasks(ordersCountPerAgent.getOrDefault(dbAgent.getAgentId(), 0));
                        agent.setOrders(null);
                    } else {
                        if (ordersPerAgent.containsKey(dbAgent.getAgentId())) {
                            agent.setOrders(ordersPerAgent.get(dbAgent.getAgentId()));
                            agent.setRunningTasks(agent.getOrders().size());
                        }
                    }
                    return agent;
                }).filter(Objects::nonNull).collect(Collectors.toList()));
            }

            agents.setDeliveryDate(Date.from(Instant.now()));
            agents.setAgents(agentsList);

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(agents));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
}