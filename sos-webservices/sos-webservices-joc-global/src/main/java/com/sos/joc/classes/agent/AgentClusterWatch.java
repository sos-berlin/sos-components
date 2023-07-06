package com.sos.joc.classes.agent;

import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentClusterNodeLossEvent;

import js7.data.agent.AgentPath;
import js7.data.cluster.ClusterWatchProblems;
import js7.data.node.NodeId;
import js7.data_for_java.agent.JAgentRefState;
import scala.collection.JavaConverters;

public class AgentClusterWatch {
    
    private static AgentClusterWatch instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentClusterWatch.class);
    private volatile ConcurrentMap<String, ConcurrentMap<AgentPath, NodeId>> lostNodes = new ConcurrentHashMap<>();
    private Timer timer;
    private TimerTask timerTask;
    private final static long execPeriodInMillis = TimeUnit.SECONDS.toMillis(120);
    private final static String logMessageFormat = "[AgentClusterWatchService] ClusterNodeLossNotConfirmedProblem of Agent cluster '%s' of controllerId '%s': %s";
    private final static String eventMessageFragmentFormat = "'%s' director in Agent Cluster '%s' of controllerId '%s'";
    
    private AgentClusterWatch() {
    }
    
    public static synchronized AgentClusterWatch getInstance() {
        if (instance == null) {
            instance = new AgentClusterWatch();
        }
        return instance;
    }
    
    public static void init() {
        AgentClusterWatch.getInstance()._init();
    }
    
    public static void init(String controllerId) {
        AgentClusterWatch.getInstance()._init(controllerId);
    }
    
    public static String put(String controllerId, AgentPath agentPath, ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem problem) {
        return AgentClusterWatch.getInstance()._put(controllerId, agentPath, problem);
    }
    
    public static void clean(String controllerId, AgentPath agentPath) {
        AgentClusterWatch.getInstance()._clean(controllerId, agentPath);
    }
    
    public static Optional<NodeId> getLostNodeId(String controllerId, AgentPath agentPath, JAgentRefState agentRefState) {
        return AgentClusterWatch.getInstance()._getLostNodeId(controllerId, agentPath, agentRefState);
    }
    
    public static void getMessage(String controllerId, AgentPath agentPath) {
        AgentClusterWatch.getInstance()._clean(controllerId, agentPath);
    }
    
    public static void close() {
        AgentClusterWatch.getInstance()._close();
    }
    
    private void _init() {
        Proxies.getControllerDbInstances().keySet().forEach(controllerId -> _init(controllerId));
    }
    
    private void _init(String controllerId) {
        try {
            lostNodes.put(controllerId, Proxy.of(controllerId).currentState().pathToAgentRefState().entrySet().stream()
                    .filter(e -> _getLostNodeId(e.getValue()).isPresent())
                    .peek(e -> LOGGER.error(String.format(logMessageFormat, e.getKey().string(), 
                            controllerId, e.getValue().problem().map(p -> p.messageWithCause()).orElse(""))))
                    .collect(Collectors.toConcurrentMap(e -> e.getKey(), e -> _getLostNodeId(e.getValue()).get())));
            if (!lostNodes.get(controllerId).isEmpty()) {
                EventBus.getInstance().post(getAgentClusterNodeLossEvent(controllerId, lostNodes.get(controllerId)));
                startTimer();
            }
        } catch (Exception e) {
            //
        }
    }
    
    private Optional<NodeId> _getLostNodeId(String controllerId, AgentPath agentPath, JAgentRefState agentRefState) {
        try {
            return Optional.of(lostNodes.get(controllerId).get(agentPath));
        } catch (Throwable e) {
            try {
                return _getLostNodeId(agentRefState);
            } catch (Throwable e1) {
            }
        }
        return Optional.empty();
    }
    
    private Optional<NodeId> _getLostNodeId(JAgentRefState agentRefState) {
        Map<NodeId, ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem> lostNodeIds = JavaConverters.asJava(agentRefState.asScala()
                .nodeToClusterNodeProblem());
        if (!lostNodeIds.isEmpty()) {
            return Optional.of(lostNodeIds.values().iterator().next().event().lostNodeId());
        }
        return Optional.empty();
    }
    
    private String _put(String controllerId, AgentPath agentPath, ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem problem) {
        lostNodes.putIfAbsent(controllerId, new ConcurrentHashMap<>());
        lostNodes.get(controllerId).put(agentPath, problem.event().lostNodeId());
        String message = String.format(logMessageFormat, agentPath.string(), controllerId, problem.messageWithCause());
        LOGGER.error(message);
        startTimer();
        return message;
    }
    
    private void _clean(String controllerId, AgentPath agentPath) {
        ConcurrentMap<AgentPath, NodeId> lostNodesPerController = lostNodes.get(controllerId);
        if (lostNodesPerController != null) {
            lostNodesPerController.remove(agentPath);
        }
    }
    
    private void _close() {
        try {
            if (timerTask != null) {
                timerTask.cancel(); 
            }
            if (timer != null) {
                timer.cancel();
                timer.purge();
            }
        } catch (Throwable e) {
            //
        }
        lostNodes.clear();
    }
    
    private void startTimer() {
        startTimer(execPeriodInMillis);
    }
    
    private void startTimer(long delay) {
        if (timer == null && !lostNodes.isEmpty()) {
            timer = new Timer("Timer-AgentClusterWatch");
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    lostNodes.forEach((controllerId, lostNodesPerController) -> {
                        if (!lostNodesPerController.isEmpty()) {
                            EventBus.getInstance().post(getAgentClusterNodeLossEvent(controllerId, lostNodesPerController));
                        }
                    });
                }

            };
            timer.scheduleAtFixedRate(timerTask, delay, execPeriodInMillis);
        }
    }
    
    private AgentClusterNodeLossEvent getAgentClusterNodeLossEvent(String controllerId, ConcurrentMap<AgentPath, NodeId> lostNodesPerController) {
        return new AgentClusterNodeLossEvent(controllerId, getMessage(controllerId, lostNodesPerController));
    }
    
    private String getMessage(String controllerId, ConcurrentMap<AgentPath, NodeId> lostNodesPerController) {
        return lostNodesPerController.entrySet().stream().map(e -> String.format(eventMessageFragmentFormat, e.getValue().string(), e.getKey()
                .string(), controllerId)).collect(Collectors.joining(", ", "[AgentClusterWatchService] ClusterNodeLossNotConfirmedProblem: Loss of ",
                        " requires user confirmation"));
    }
    
}
