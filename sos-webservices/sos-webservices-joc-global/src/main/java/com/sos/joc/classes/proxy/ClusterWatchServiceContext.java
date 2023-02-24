package com.sos.joc.classes.proxy;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.proxy.ClusterNodeLossEvent;
import com.sos.joc.exceptions.ControllerConflictException;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;

import js7.base.problem.Problem;
import js7.cluster.watch.ClusterWatchService;
import js7.cluster.watch.api.ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem;
import js7.data.cluster.ClusterState;
import js7.data.cluster.ClusterWatchId;
import js7.data.node.NodeId;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.eventbus.JStandardEventBus;
import scala.jdk.javaapi.OptionConverters;

public class ClusterWatchServiceContext {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterWatchServiceContext.class);
    private String controllerId;
    private ClusterWatchService service;
    private JControllerApi controllerApi;
    private static final NodeId primaryId = NodeId.of("Primary");
    private static final NodeId backupId = NodeId.of("Backup");
    private Instant burstFilter = null;
    private NodeId lossNode = null;
    private String message = null;
    
    protected ClusterWatchServiceContext(String controllerId, String clusterWatchId, JControllerApi controllerApi) throws InterruptedException,
            ExecutionException {
        this.controllerId = controllerId;
        this.controllerApi = controllerApi;
        this.service = controllerApi.startClusterWatch(ClusterWatchId.of(clusterWatchId), startEventbus()).get();
    }
    
    private JStandardEventBus<ClusterNodeLossNotConfirmedProblem> startEventbus() {
        JStandardEventBus<ClusterNodeLossNotConfirmedProblem> eventBus = JControllerApi.newClusterWatchEventBus();
        eventBus.subscribe(Collections.singleton(ClusterNodeLossNotConfirmedProblem.class), this::onNodeLossNotConfirmedProblem);
        return eventBus;
    }
    
    private void onNodeLossNotConfirmedProblem(ClusterNodeLossNotConfirmedProblem problem) {
        lossNode = problem.event().lostNodeId();
        message = problem.messageWithCause();
        Instant now = Instant.now();
        if (burstFilter == null || !burstFilter.isAfter(now)) {
            burstFilter = Instant.now().plusSeconds(120);
            LOGGER.warn("[ClusterWatchService] ClusterNodeLossNotConfirmedProblem of cluster '" + controllerId + "' received: " + problem
                    .messageWithCause());
            EventBus.getInstance().post(new ClusterNodeLossEvent(controllerId, lossNode.string(), message));
        }
    }
    
    protected NodeId getClusterNodeLoss() {
        if (service.clusterNodeLossEventToBeConfirmed(primaryId).isDefined()) {
            return primaryId;
        }
        if (service.clusterNodeLossEventToBeConfirmed(backupId).isDefined()) {
            return backupId;
        }
        return lossNode;
    }
    
    protected Optional<String> getAndCleanLastMessage() {
        final String m = message;
        message = null;
        return m == null ? Optional.empty() : Optional.of(m);
    }
    
    protected void confirmNodeLoss(NodeId lossNodeId) throws ControllerObjectNotExistException, ControllerConflictException, JocBadRequestException {
        if (lossNodeId == null) {
            throw new ControllerObjectNotExistException("Missing cluster node id");
        }
        if (!primaryId.equals(lossNodeId) && !backupId.equals(lossNodeId)) {
            throw new ControllerObjectNotExistException("Invalid cluster node id '" + lossNodeId.string() + "'. Must be 'Primary' or 'Backup'.");
        }
//        if (service.clusterNodeLossEventToBeConfirmed(lossNodeId).isDefined()) {
//            throw new ControllerConflictException("The cluster node with id '" + lossNodeId.string() + "' is not lost.");
//        } else {
            LOGGER.info("[ClusterWatchService] " + OptionConverters.toJava(service.clusterNodeLossEventToBeConfirmed(lossNodeId)).get().toString());
            scala.util.Either<Problem, ClusterState> state = service.clusterState();
            if (state.isLeft()) {
                LOGGER.info("[ClusterWatchService] " + OptionConverters.toJava(state.left().toOption()).get().toString());
            } else {
                LOGGER.info("[ClusterWatchService] " + OptionConverters.toJava(state.toOption()).get().toString());
            }
            LOGGER.info("[ClusterWatchService] send service.confirmNodeLoss(" + lossNodeId.string() + ")");
            scala.util.Either<Problem,?> checked = service.confirmNodeLoss(lossNodeId);
            if (checked.isLeft()) {
                throw new JocBadRequestException(OptionConverters.toJava(checked.left().toOption()).get().toString());
            } else {
                burstFilter = null;
                lossNode = null;
                message = null;
            }
//        }
//        if (service.clusterNodeLossEventToBeConfirmed(lossNodeId).isDefined()) {
//            ClusterNodeLostEvent clusterNodeLostEvent = service.clusterNodeLossEventToBeConfirmed(lossNodeId).get();
//            // assert clusterNodeLostEvent.lostNodeId().equals(primaryId);
//
//            // In case of broken connection between the nodes, both primaryId and backupId
//            // may require manual ClusterNodeLostEvent.
//            // The user must decide which node is considered to be lost.
//            // Before this, they must terminate the lost node.
//            // assert service.clusterNodeLossEventToBeConfirmed(backupId).isEmpty();
//
//            // Don't do this automatically! The user must be sure that the node is down.
//            // Otherwise, both cluster nodes may get active, with destroying consequences.
//            Optional<Problem> maybeProblem = OptionConverters.toJava(service.confirmNodeLoss(lossNodeId).left().toOption());
//        }
    }
    
    protected boolean stop() {
        if (service != null) {
            try {
                controllerApi.stopClusterWatch().get();
                LOGGER.info("[ClusterWatch] Watch is stopped for '" + controllerId + "'");
            } catch (Exception e) {
                LOGGER.error("[ClusterWatch] stopping watch for '" + controllerId + "' failed", e);
                return false;
            }
        }
        return true;
    }

}
