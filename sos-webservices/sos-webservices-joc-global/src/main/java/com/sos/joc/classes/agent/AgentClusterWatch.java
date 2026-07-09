package com.sos.joc.classes.agent;

import java.util.Map;
import java.util.Optional;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.agent.AgentClusterFailoverConfirmEvent;
import com.sos.joc.event.bean.agent.AgentClusterNodeLossEvent;

import js7.data.agent.AgentPath;
import js7.data.cluster.ClusterEvent.ClusterNodeLostEvent;
import js7.data.cluster.ClusterWatchProblems;
import js7.data.cluster.ClusterWatchProblems.ClusterFailoverNotConfirmedProblem;
import js7.data.cluster.ClusterWatchProblems.ClusterNodeLostEventNotConfirmedProblem;
import js7.data.node.NodeId;
import js7.data_for_java.agent.JAgentRefState;
import scala.collection.JavaConverters;

public class AgentClusterWatch {
    
    private static AgentClusterWatch instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(AgentClusterWatch.class);
    private volatile ConcurrentMap<String, ConcurrentMap<AgentPath, ClusterNodeLostEventNotConfirmedProblem>> problems = new ConcurrentHashMap<>();
    private Timer timer;
    private TimerTask timerTask;
    private final static long execPeriodInMillis = TimeUnit.SECONDS.toMillis(120);
    private final static String logMessageFormatNodeLoss =
            "[AgentClusterWatchService] ClusterNodeLossNotConfirmedProblem of Agent cluster '%s' of controllerId '%s': %s";
    private final static String logMessageFormatFailover =
            "[AgentClusterWatchService] FailoverNotConfirmedProblem of Agent cluster '%s' of controllerId '%s': %s";
    private final static String eventMessageFragmentFormat = "'%s' director in Agent Cluster '%s' of controllerId '%s'";
    private static Predicate<ClusterNodeLostEventNotConfirmedProblem> isFailOver = e -> (e instanceof ClusterFailoverNotConfirmedProblem);

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
    
    public static String put(String controllerId, AgentPath agentPath, ClusterNodeLostEventNotConfirmedProblem problem) {
        return AgentClusterWatch.getInstance()._put(controllerId, agentPath, problem);
    }
    
    public static void clean(String controllerId, AgentPath agentPath) {
        AgentClusterWatch.getInstance()._clean(controllerId, agentPath);
    }
    
    public static Optional<NodeId> getLostNodeId(String controllerId, AgentPath agentPath, JAgentRefState agentRefState) {
        return AgentClusterWatch.getInstance()._getLostNodeId(controllerId, agentPath, agentRefState);
    }
    
    public static void close() {
        AgentClusterWatch.getInstance()._close();
    }
    
    private void _init() {
        Proxies.getControllerDbInstances().keySet().forEach(controllerId -> _init(controllerId));
    }
    
    private void _init(String controllerId) {
        try {
            problems.put(controllerId, Proxy.of(controllerId).currentState().pathToAgentRefState().entrySet().stream().filter(
                    e -> _getConfirmedProblem(e.getValue()).isPresent()).collect(Collectors.toConcurrentMap(Map.Entry::getKey,
                            e -> _getConfirmedProblem(e.getValue()).get())));
            if (!problems.get(controllerId).isEmpty()) {
                Predicate<Map.Entry<AgentPath, ClusterNodeLostEventNotConfirmedProblem>> isFailOver2 = e -> isFailOver.test(e.getValue());
                getAgentClusterFailoverEvent(controllerId, problems.get(controllerId).entrySet().stream().filter(isFailOver2).peek(e -> log(
                        controllerId, e.getKey(), e.getValue()))).ifPresent(EventBus.getInstance()::post);
                getAgentClusterNodeLossEvent(controllerId, problems.get(controllerId).entrySet().stream().filter(isFailOver2.negate()).peek(e -> log(
                        controllerId, e.getKey(), e.getValue()))).ifPresent(EventBus.getInstance()::post);
                startTimer();
            }
        } catch (Exception e) {
            //
        }
    }
    
    private void log(String controllerId, AgentPath agent, ClusterNodeLostEventNotConfirmedProblem problem) {
        if (isFailOver.test(problem)) {
            LOGGER.error(String.format(logMessageFormatFailover, agent.string(), controllerId, problem.messageWithCause()));
        } else { // ClusterNodeLostEventNotConfirmedProblem
            LOGGER.error(String.format(logMessageFormatNodeLoss, agent.string(), controllerId, problem.messageWithCause()));
        }
    }
    
    private Optional<NodeId> _getLostNodeId(String controllerId, AgentPath agentPath, JAgentRefState agentRefState) {
        return _getConfirmedProblem(controllerId, agentPath, agentRefState).map(ClusterNodeLostEventNotConfirmedProblem::event).map(
                ClusterNodeLostEvent::lostNodeId);
    }
   
//    private Optional<NodeId> _getLostNodeId(JAgentRefState agentRefState) {
//        Map<NodeId, ClusterWatchProblems.ClusterNodeLostEventNotConfirmedProblem> lostNodeIds = JavaConverters.asJava(agentRefState.asScala()
//                .nodeToLossNotConfirmedProblem());
//        if (!lostNodeIds.isEmpty()) {
//            return Optional.of(lostNodeIds.values().iterator().next().event().lostNodeId());
//        }
//        return Optional.empty();
//    }
    
    private Optional<ClusterNodeLostEventNotConfirmedProblem> _getConfirmedProblem(String controllerId, AgentPath agentPath,
            JAgentRefState agentRefState) {
        try {
            return Optional.ofNullable(problems.get(controllerId).get(agentPath));
        } catch (Throwable e) {
            try {
                return _getConfirmedProblem(agentRefState);
            } catch (Throwable e1) {
            }
        }
        return Optional.empty();
    }
    
    private Optional<ClusterNodeLostEventNotConfirmedProblem> _getConfirmedProblem(JAgentRefState agentRefState) {
        Map<NodeId, ClusterWatchProblems.ClusterNodeLostEventNotConfirmedProblem> confirmProblems = JavaConverters.asJava(agentRefState.asScala()
                .nodeToLossNotConfirmedProblem());
        if (!confirmProblems.isEmpty()) {
            return Optional.ofNullable(confirmProblems.values().iterator().next());
        }
        return Optional.empty();
    }
    
    private String _put(String controllerId, AgentPath agentPath, ClusterNodeLostEventNotConfirmedProblem problem) {
        problems.putIfAbsent(controllerId, new ConcurrentHashMap<>());
        problems.get(controllerId).put(agentPath, problem);
        String message = "";
        if (isFailOver.test(problem)) {
            message = String.format(logMessageFormatFailover, agentPath.string(), controllerId, problem.messageWithCause());
        } else { //ClusterNodeLostEventNotConfirmedProblem
            message = String.format(logMessageFormatNodeLoss, agentPath.string(), controllerId, problem.messageWithCause());
        }
        LOGGER.error(message);
        startTimer();
        return message;
    }
    
    private void _clean(String controllerId, AgentPath agentPath) {
        ConcurrentMap<AgentPath, ClusterNodeLostEventNotConfirmedProblem> lostNodesPerController = problems.get(controllerId);
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
        problems.clear();
    }
    
    private void startTimer() {
        startTimer(execPeriodInMillis);
    }
    
    private void startTimer(long delay) {
        if (timer == null && !problems.isEmpty()) {
            timer = new Timer("Timer-AgentClusterWatch");
            timerTask = new TimerTask() {

                @Override
                public void run() {
                    problems.forEach((controllerId, lostNodesPerController) -> {
                        if (!lostNodesPerController.isEmpty()) {
                            Predicate<Map.Entry<AgentPath, ClusterNodeLostEventNotConfirmedProblem>> isFailOver2 = e -> isFailOver.test(e.getValue());
                            getAgentClusterFailoverEvent(controllerId, lostNodesPerController.entrySet().stream().filter(isFailOver2)).ifPresent(
                                    EventBus.getInstance()::post);
                            getAgentClusterNodeLossEvent(controllerId, lostNodesPerController.entrySet().stream().filter(isFailOver2.negate()))
                                    .ifPresent(EventBus.getInstance()::post);
                        }
                    });
                }

            };
            timer.scheduleAtFixedRate(timerTask, delay, execPeriodInMillis);
        }
    }
    
    private Optional<AgentClusterNodeLossEvent> getAgentClusterNodeLossEvent(String controllerId,
            Stream<Map.Entry<AgentPath, ClusterNodeLostEventNotConfirmedProblem>> lostNodesPerController) {
        return getMessage(controllerId, lostNodesPerController, false).map(msg -> new AgentClusterNodeLossEvent(controllerId, msg));
    }
    
    private Optional<AgentClusterFailoverConfirmEvent> getAgentClusterFailoverEvent(String controllerId,
            Stream<Map.Entry<AgentPath, ClusterNodeLostEventNotConfirmedProblem>> lostNodesPerController) {
        return getMessage(controllerId, lostNodesPerController, true).map(msg -> new AgentClusterFailoverConfirmEvent(controllerId, msg));
    }
    
    private Optional<String> getMessage(String controllerId,
            Stream<Map.Entry<AgentPath, ClusterNodeLostEventNotConfirmedProblem>> lostNodesPerController, boolean failover) {
        AtomicBoolean exists = new AtomicBoolean(false);
        lostNodesPerController = lostNodesPerController.peek(e -> exists.set(true));
        if (exists.get()) {
            if (failover) {
                return Optional.of(lostNodesPerController.map(e -> String.format(eventMessageFragmentFormat, e.getValue().event().lostNodeId()
                        .string(), e.getKey().string(), controllerId)).collect(Collectors.joining(", ",
                                "[AgentClusterWatchService] FailoverNotConfirmedProblem: Failover of ", " requires user confirmation")));
            } else {
                return Optional.of(lostNodesPerController.map(e -> String.format(eventMessageFragmentFormat, e.getValue().event().lostNodeId()
                        .string(), e.getKey().string(), controllerId)).collect(Collectors.joining(", ",
                                "[AgentClusterWatchService] ClusterNodeLossNotConfirmedProblem: Loss of ", " requires user confirmation")));
            }
        } else {
            return Optional.empty();
        }
    }
    
}
