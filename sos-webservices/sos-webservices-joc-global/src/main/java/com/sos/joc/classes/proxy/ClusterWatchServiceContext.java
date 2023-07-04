package com.sos.joc.classes.proxy;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.proxy.ClusterNodeLossEvent;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocError;

import js7.base.problem.Problem;
import js7.cluster.watch.ClusterWatchService;
import js7.data.cluster.ClusterState;
import js7.data.cluster.ClusterWatchId;
import js7.data.cluster.ClusterWatchProblems;
import js7.data.node.NodeId;
import js7.proxy.javaapi.JControllerApi;
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
        this.service = controllerApi.startClusterWatch(ClusterWatchId.of(clusterWatchId), this::onNodeLossNotConfirmedProblem).get();
        logClusterState(primaryId, backupId);
    }
    
    private void onNodeLossNotConfirmedProblem(ClusterWatchProblems.ClusterNodeLossNotConfirmedProblem problem) {
        lossNode = problem.event().lostNodeId();
        message = problem.messageWithCause();
        Instant now = Instant.now();
        if (burstFilter == null || !burstFilter.isAfter(now)) {
            burstFilter = Instant.now().plusSeconds(120);
            LOGGER.error("[ClusterWatchService] ClusterNodeLossNotConfirmedProblem of cluster '" + controllerId + "' received: " + problem
                    .messageWithCause());
            EventBus.getInstance().post(new ClusterNodeLossEvent(controllerId, lossNode.string(), message));
        }
    }
    
    protected NodeId getClusterNodeLoss() {
        return lossNode;
    }
    
    protected Optional<String> getAndCleanLastMessage() {
        final String m = message;
        message = null;
        return m == null ? Optional.empty() : Optional.of(m);
    }
    
    protected void confirmNodeLoss(NodeId lossNodeId, String confirmer, String accessToken, JocError jocError)
            throws ControllerObjectNotExistException {
        if (lossNodeId == null) {
            throw new ControllerObjectNotExistException("Missing cluster node id");
        }
        if (confirmer == null) {
            confirmer = service.clusterWatchId().string();
        }
        if (!primaryId.equals(lossNodeId) && !backupId.equals(lossNodeId)) {
            throw new ControllerObjectNotExistException("Invalid cluster node id '" + lossNodeId.string() + "'. Must be 'Primary' or 'Backup'.");
        }
//        if (service.clusterNodeLossEventToBeConfirmed(lossNodeId).isDefined()) {
//            throw new ControllerConflictException("The cluster node with id '" + lossNodeId.string() + "' is not lost.");
//        } else {
            LOGGER.info("[ClusterWatchService] send service.confirmNodeLoss(" + lossNodeId.string() + ")");
            controllerApi.manuallyConfirmNodeLoss(lossNodeId, confirmer).thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, controllerId);
                if (either.isRight()) {
                    burstFilter = null;
                    lossNode = null;
                    message = null;
                    logClusterState(lossNodeId);
                }
            });
//            scala.util.Either<Problem,?> checked = service.manuallyConfirmNodeLoss(lossNodeId, confirmer);
//            if (checked.isLeft()) {
//                throw new JocBadRequestException(checked.left().toOption().get().toString());
//                //throw new JocBadRequestException(OptionConverters.toJava(checked.left().toOption()).get().toString());
//            } else {
//                burstFilter = null;
//                lossNode = null;
//                message = null;
//                logClusterState(lossNodeId);
//            }
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
                LOGGER.info("[ClusterWatchService] Watch is stopped for '" + controllerId + "'");
            } catch (Exception e) {
                LOGGER.error("[ClusterWatchService] stopping watch for '" + controllerId + "' failed", e);
                return false;
            }
        }
        return true;
    }
    
    private void logClusterState(NodeId... lossNodeIds) {
        if (lossNodeIds != null) {
            for (NodeId lossNodeId : lossNodeIds) {
                OptionConverters.toJava(service.clusterNodeLossEventToBeConfirmed(lossNodeId)).ifPresent(e -> LOGGER.info("[ClusterWatchService] "
                        + lossNodeId + ": " + e.toString()));
            }
        }
        scala.util.Either<Problem, ClusterState> state = service.clusterState();
        if (state.isLeft()) {
            LOGGER.info("[ClusterWatchService] " + state.left().toOption().get().toString());
        } else {
            LOGGER.info("[ClusterWatchService] " + state.toOption().get().toString());
        }
    }

}
