package com.sos.joc.workflow.impl;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.sos.controller.model.workflow.Workflow;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.instruction.ForkJoin;
import com.sos.inventory.model.instruction.IfElse;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.Lock;
import com.sos.inventory.model.instruction.TryCatch;
import com.sos.inventory.model.workflow.Branch;

import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;

public class WorkflowsHelper {
    
    public static boolean isCurrentVersion(String versionId, JControllerState currentState) {
        if (versionId == null || versionId.isEmpty()) {
            return true;
        }
        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).anyMatch(o -> o.workflowId().versionId().string().equals(versionId));
    }
    
    public static boolean isCurrentVersion(WorkflowId workflowId, JControllerState currentState) {
        if (workflowId == null) {
            return true;
        }
        return isCurrentVersion(workflowId.getVersionId(), currentState);
    }
    
    public static Stream<String> currentVersions(JControllerState currentState) {
        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).map(o -> o.workflowId().versionId().string());
    }
    
    public static Stream<JWorkflowId> currentJWorkflowIds(JControllerState currentState) {
        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).map(JOrder::workflowId);
    }
    
    public static Stream<WorkflowId> currentWorkflowIds(JControllerState currentState) {
        return currentState.ordersBy(currentState.orderIsInCurrentVersionWorkflow()).map(o -> new WorkflowId(o.workflowId().path().string(), o
                .workflowId().versionId().string()));
    }
    
    public static Stream<String> oldVersions(JControllerState currentState) {
        return currentState.ordersBy(JOrderPredicates.not(currentState.orderIsInCurrentVersionWorkflow())).map(o -> o.workflowId().versionId()
                .string());
    }
    
    public static Stream<WorkflowId> oldWorkflowIds(JControllerState currentState) {
        return currentState.ordersBy(JOrderPredicates.not(currentState.orderIsInCurrentVersionWorkflow())).map(o -> new WorkflowId(o.workflowId()
                .path().string(), o.workflowId().versionId().string()));
    }

    public static Stream<JWorkflowId> oldJWorkflowIds(JControllerState currentState) {
        return currentState.ordersBy(JOrderPredicates.not(currentState.orderIsInCurrentVersionWorkflow())).map(JOrder::workflowId);
    }
    
    public static Workflow addWorkflowPositions(Workflow w) {
        if (w == null) {
            return null;
        }
        Object[] o = {};
        setWorkflowPositions(o, w.getInstructions());
        return w;
    }
    
    private static void setWorkflowPositions(Object[] parentPosition, List<Instruction> insts) {
        if (insts != null) {
            for (int i = 0; i < insts.size(); i++) {
                Object[] pos = extendArray(parentPosition, i);
                pos[parentPosition.length] = i;
                Instruction inst = insts.get(i);
                inst.setPosition(Arrays.asList(pos));
                switch (inst.getTYPE()) {
                case FORK:
                    ForkJoin f = inst.cast();
                    for(Branch b : f.getBranches()) {
                        setWorkflowPositions(extendArray(pos, "fork+" + b.getId()), b.getWorkflow().getInstructions());
                    }
                    break;
                case IF:
                    IfElse ie = inst.cast();
                    setWorkflowPositions(extendArray(pos, "then"), ie.getThen().getInstructions());
                    if (ie.getElse() != null) {
                        setWorkflowPositions(extendArray(pos, "else"), ie.getElse().getInstructions());
                    }
                    break;
                case TRY:
                    TryCatch tc = inst.cast();
                    setWorkflowPositions(extendArray(pos, "try+0"), tc.getTry().getInstructions());
                    if (tc.getCatch() != null) {
                        setWorkflowPositions(extendArray(pos, "catch+0"), tc.getCatch().getInstructions());
                    }
                    break;
                case LOCK:
                    Lock l = inst.cast();
                    setWorkflowPositions(extendArray(pos, "lock"), l.getLockedWorkflow().getInstructions());
                    break;
                default:
                    break;
                }
            }
        }
    }
    
    private static Object[] extendArray(Object[] position, Object extValue) {
        Object[] pos = Arrays.copyOf(position, position.length + 1);
        pos[position.length] = extValue;
        return pos;
    }

}
