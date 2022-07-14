package com.sos.joc.classes.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.common.SyncStateText;
import com.sos.controller.model.workflow.Workflow;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.model.inventory.common.ConfigurationType;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.agent.AgentPath;
import js7.data.board.BoardPath;
import js7.data.job.JobResourcePath;
import js7.data.lock.LockPath;
import js7.data.orderwatch.OrderWatchPath;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControl;
import js7.data_for_java.common.JJsonable;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;

public class SyncStateHelper {
    
    public static final Map<SyncStateText, Integer> severityByStates = Collections.unmodifiableMap(new HashMap<SyncStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(SyncStateText.IN_SYNC, 6); //blue
            put(SyncStateText.NOT_IN_SYNC, 5); //orange
            put(SyncStateText.NOT_DEPLOYED, 4);
            put(SyncStateText.SUSPENDED, 11); //lightorange
            put(SyncStateText.OUTSTANDING, 11);//lightorange
            put(SyncStateText.UNKNOWN, 2);
        }
    });

    public static SyncState getState(SyncStateText stateText) {
        SyncState state = new SyncState();
        state.set_text(stateText);
        state.setSeverity(severityByStates.get(stateText));
        return state;
    }
    
    public static SyncState getState(JControllerState currentstate, Long invCId, Integer type, Map<Long, String> deployedNames) {
        return getState(currentstate, invCId, ConfigurationType.fromValue(type), deployedNames);
    }
    
    public static SyncState getState(JControllerState currentstate, Long invCId, ConfigurationType type, Map<Long, String> deployedNames) {
        SyncStateText stateText = SyncStateText.UNKNOWN;
        switch (type) {
        case WORKFLOW:
            if (deployedNames == null || !deployedNames.containsKey(invCId)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    WorkflowPath wPath = WorkflowPath.of(deployedNames.get(invCId));
                    stateText = getWorkflowState(currentstate.repo().pathToCheckedWorkflow(wPath), currentstate);
                }
            }
            break;
        case JOBRESOURCE:
            if (deployedNames == null || !deployedNames.containsKey(invCId)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.pathToJobResource().get(JobResourcePath.of(deployedNames.get(invCId))));
                }
            }
            break;
        case LOCK:
            if (deployedNames == null || !deployedNames.containsKey(invCId)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.pathToLock().get(LockPath.of(deployedNames.get(invCId))));
                }
            }
            break;
        case FILEORDERSOURCE:
            if (deployedNames == null || !deployedNames.containsKey(invCId)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.pathToFileWatch().get(OrderWatchPath.of(deployedNames.get(invCId))));
                }
            }
            break;
        case NOTICEBOARD:
            if (deployedNames == null || !deployedNames.containsKey(invCId)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.pathToBoard().get(BoardPath.of(deployedNames.get(invCId))));
                }
            }
            break;
        default:
            return null;
        }
        return SyncStateHelper.getState(stateText);
    }
    
    public static boolean isNotInSync(JControllerState currentstate, String name, Integer intType) {
        ConfigurationType type = ConfigurationType.fromValue(intType);
        return isNotInSync(currentstate, name, type);
    }
    
    public static boolean isNotInSync(JControllerState currentstate, String name, ConfigurationType type) {
        if (currentstate == null) {
            return false;
        }
        switch (type) {
        case WORKFLOW:
            return isNotInSync(currentstate.repo().pathToCheckedWorkflow(WorkflowPath.of(name)));
        case JOBRESOURCE:
            return currentstate.pathToJobResource().get(JobResourcePath.of(name)) != null;
        case LOCK:
            return currentstate.pathToLock().get(LockPath.of(name)) != null;
        case FILEORDERSOURCE:
            return currentstate.pathToFileWatch().get(OrderWatchPath.of(name)) != null;
        case NOTICEBOARD:
            return currentstate.pathToBoard().get(BoardPath.of(name)) != null;
        default:
            return true;
        }
    }
    
    public static SyncStateText getWorkflowState(Either<Problem, JWorkflow> either, JControllerState currentstate) {
        SyncStateText stateText = SyncStateText.NOT_IN_SYNC;
        if (either != null && either.isRight()) {
            stateText = SyncStateText.IN_SYNC;
            WorkflowPath wPath = either.get().id().path();
            Optional<WorkflowPathControl> controlState = WorkflowsHelper.getWorkflowPathControl(currentstate, wPath, false);
            if (controlState.isPresent()) {
                Set<AgentPath> agentsThatIgnoreCommand = currentstate.workflowPathControlToIgnorantAgent().getOrDefault(wPath, Collections
                        .emptySet());
                int numOfgentsThatIgnoreCommand = agentsThatIgnoreCommand.size();
                // int numOfAgentsThatConfirmedSuspendOrResume = JavaConverters.asJava(controlState.attachedToAgents()).size();
                // int totalNumOfAgents = JavaConverters.asJava(workflowE.get().asScala().referencedAgentPaths()).size();
//                if (controlState.workflowPathControl().suspended()) {
//                    if (numOfAgentsThatConfirmedSuspendOrResume >= totalNumOfAgents) {
//                        stateText = SyncStateText.SUSPENDED;
//                    } else {
//                        stateText = SyncStateText.SUSPENDING;
//                    }
//                } else if (numOfAgentsThatConfirmedSuspendOrResume < totalNumOfAgents) {
//                    stateText = SyncStateText.RESUMING;
//                }
                
                if (numOfgentsThatIgnoreCommand > 0) {
                    stateText = SyncStateText.OUTSTANDING;
                } else if (controlState.get().suspended()) {
                    stateText = SyncStateText.SUSPENDED;
                }
            }
        }
        return stateText;
    }
    
    public static void setWorkflowWithStateAndSuspended(Workflow workflow, Either<Problem, JWorkflow> either, JControllerState currentstate) {
        workflow.setSuspended(false);
        SyncStateText stateText = SyncStateText.NOT_IN_SYNC;
        if (either != null && either.isRight()) {
            stateText = SyncStateText.IN_SYNC;
            WorkflowPath wPath = either.get().id().path();
            Optional<WorkflowPathControl> controlState = WorkflowsHelper.getWorkflowPathControl(currentstate, wPath, false);
            if (controlState.isPresent()) {
                if (currentstate.workflowPathControlToIgnorantAgent().getOrDefault(wPath, Collections.emptySet()).size() > 0) {
                    stateText = SyncStateText.OUTSTANDING;
                } else if (controlState.get().suspended()) {
                    workflow.setSuspended(true);
                    stateText = SyncStateText.SUSPENDED;
                }
            }
        }
        workflow.setState(getState(stateText));
    }
    
    private static SyncStateText getState(JJsonable<?> o) {
        SyncStateText stateText = SyncStateText.NOT_IN_SYNC;
        if (o != null) {
            stateText = SyncStateText.IN_SYNC;
        }
        return stateText;
    }
    
    private static boolean isNotInSync(Either<Problem, ?> either) {
        boolean inSync = true;
        if (either != null && either.isRight()) {
            inSync = false;
        }
        return inSync;
    }
    
    public static JControllerState getControllerState(String controllerId, String accessToken, JocError jocError) {
        if (controllerId != null && !controllerId.isEmpty()) {
            try {
                return Proxy.of(controllerId).currentState();
            } catch (Exception e) {
                ProblemHelper.postExceptionEventIfExist(Either.left(e), accessToken, jocError, controllerId);
            }
        }
        return null;
    }
}
