package com.sos.joc.classes.common;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.model.inventory.common.ConfigurationType;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.board.BoardPath;
import js7.data.job.JobResourcePath;
import js7.data.lock.LockPath;
import js7.data.orderwatch.OrderWatchPath;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;

public class SyncStateHelper {
    
    public static final Map<SyncStateText, Integer> severityByStates = Collections.unmodifiableMap(new HashMap<SyncStateText, Integer>() {

        private static final long serialVersionUID = 1L;

        {
            put(SyncStateText.IN_SYNC, 6);
            put(SyncStateText.NOT_IN_SYNC, 5);
            put(SyncStateText.NOT_DEPLOYED, 4);
            put(SyncStateText.UNKNOWN, 2);
        }
    });

    public static SyncState getState(SyncStateText stateText) {
        SyncState state = new SyncState();
        state.set_text(stateText);
        state.setSeverity(severityByStates.get(stateText));
        return state;
    }
    
//    public static SyncState getState(JControllerState currentstate, WorkflowPath name) {
//        SyncStateText stateText = SyncStateText.UNKNOWN;
//        if (currentstate != null) {
//            stateText = getState(currentstate.repo().pathToWorkflow(name));
//        }
//        return SyncStateHelper.getState(stateText);
//    }
//    
//    public static SyncState getState(JControllerState currentstate, BoardPath name) {
//        SyncStateText stateText = SyncStateText.UNKNOWN;
//        if (currentstate != null) {
//            stateText = getState(currentstate.pathToBoard(name));
//        }
//        return SyncStateHelper.getState(stateText);
//    }
//    
//    public static SyncState getState(JControllerState currentstate, LockPath name) {
//        SyncStateText stateText = SyncStateText.UNKNOWN;
//        if (currentstate != null) {
//            stateText = getState(currentstate.pathToLock(name));
//        }
//        return SyncStateHelper.getState(stateText);
//    }
//    
//    public static SyncState getState(JControllerState currentstate, JobResourcePath name) {
//        SyncStateText stateText = SyncStateText.UNKNOWN;
//        if (currentstate != null) {
//            stateText = getState(currentstate.pathToJobResource(name));
//        }
//        return SyncStateHelper.getState(stateText);
//    }
//    
//    public static SyncState getState(JControllerState currentstate, OrderWatchPath name) {
//        SyncStateText stateText = SyncStateText.UNKNOWN;
//        if (currentstate != null) {
//            stateText = getState(currentstate.pathToFileWatch(name));
//        }
//        return SyncStateHelper.getState(stateText);
//    }
    
    public static SyncState getState(JControllerState currentstate, String name, Integer type, Set<String> deployedNames) {
        return getState(currentstate, name, ConfigurationType.fromValue(type), deployedNames);
    }
    
    public static SyncState getState(JControllerState currentstate, String name, ConfigurationType type, Set<String> deployedNames) {
        SyncStateText stateText = SyncStateText.UNKNOWN;
        switch (type) {
        case WORKFLOW:
            if (deployedNames == null || !deployedNames.contains(name)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.repo().pathToWorkflow(WorkflowPath.of(name)));
                }
            }
            break;
        case JOBRESOURCE:
            if (deployedNames == null || !deployedNames.contains(name)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.pathToJobResource(JobResourcePath.of(name)));
                }
            }
            break;
        case LOCK:
            if (deployedNames == null || !deployedNames.contains(name)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.pathToLock(LockPath.of(name)));
                }
            }
            break;
        case FILEORDERSOURCE:
            if (deployedNames == null || !deployedNames.contains(name)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.pathToFileWatch(OrderWatchPath.of(name)));
                }
            }
            break;
        case NOTICEBOARD:
            if (deployedNames == null || !deployedNames.contains(name)) {
                stateText = SyncStateText.NOT_DEPLOYED;
            } else {
                if (currentstate != null) {
                    stateText = getState(currentstate.pathToBoard(BoardPath.of(name)));
                }
            }
            break;
        default:
            return null;
        }
        return SyncStateHelper.getState(stateText);
    }
    
    private static SyncStateText getState(Either<Problem, ?> either) {
        SyncStateText stateText = SyncStateText.NOT_IN_SYNC;
        if (either != null && either.isRight()) {
            stateText = SyncStateText.IN_SYNC;
        }
        return stateText;
    }
}
