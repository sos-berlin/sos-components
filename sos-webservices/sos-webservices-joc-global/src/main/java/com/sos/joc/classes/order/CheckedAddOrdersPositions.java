package com.sos.joc.classes.order;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrdersPositions;
import com.sos.joc.model.order.Position;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.workflow.Workflow;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;

public class CheckedAddOrdersPositions extends OrdersPositions {
    
//    @JsonIgnore
//    private Set<Positions> positionsWithImplicitEnds = new LinkedHashSet<>();
    
    public CheckedAddOrdersPositions() {
        //
    }
    
    @JsonIgnore
    public CheckedAddOrdersPositions get(WorkflowId workflowId, JControllerState currentState, Set<Folder> permittedFolders)
            throws JsonParseException, JsonMappingException, IOException, JocException {
        return get(workflowId, currentState, permittedFolders, null);
    }
    
    @JsonIgnore
    public CheckedAddOrdersPositions get(WorkflowId workflowId, JControllerState currentState, Set<Folder> permittedFolders,
            List<Object> afterPosition) throws JsonParseException, JsonMappingException, IOException, JocException {

        WorkflowPath wPath = WorkflowPath.of(JocInventory.pathToName(workflowId.getPath()));
        String path = WorkflowPaths.getPath(wPath.string());
        workflowId.setPath(path);
        
        if (!OrdersHelper.canAdd(path, permittedFolders)) {
            throw new JocFolderPermissionsException("Access denied");
        }

        Either<Problem, JWorkflow> e = null;
        if (workflowId.getVersionId() != null && !workflowId.getVersionId().isEmpty()) {
            JWorkflowId jWorkflowId = JWorkflowId.of(JocInventory.pathToName(workflowId.getPath()), workflowId.getVersionId());
            e = currentState.repo().idToCheckedWorkflow(jWorkflowId);
        } else {
            e = currentState.repo().pathToCheckedWorkflow(wPath);
        }
        ProblemHelper.throwProblemIfExist(e);
        JWorkflow w = e.get();
//        final JPosition afterPos = getAfterPos(afterPosition);
        
        // JsonNode node = Globals.objectMapper.readTree(e.get().withPositions().toJson());
        // List<Instruction> instructions = Globals.objectMapper.reader().forType(new TypeReference<List<Instruction>>() {}).readValue(node.get("instructions"));
        // List<Instruction> instructions = Arrays.asList(Globals.objectMapper.reader().treeToValue(node.get("instructions"), Instruction[].class));
        //Set<String> implicitEnds = WorkflowsHelper.extractImplicitEnds(instructions);

        final Set<Position> pos = new LinkedHashSet<>();
        JPosition pos0 = JPosition.apply(js7.data.workflow.position.Position.First());
        
        w.reachablePositions(pos0).stream().forEachOrdered(jPos -> {
            if (isReachable(jPos)) {
                Position p = createPosition(jPos, w.asScala());
                //positionsWithImplicitEnds.add(p);
                //if (!implicitEnds.contains(p.getPositionString())) {
                    pos.add(p);
                //}
            }
        });
        
//        TODO if (afterPos != null) {
//            
//        }

        setWorkflowId(workflowId);
        setPositions(pos);

        setDeliveryDate(Date.from(Instant.now()));
        setSurveyDate(Date.from(currentState.instant()));
        
        return this;
    }
    
//    @JsonIgnore
//    public Set<Positions> getPositionsWithImplicitEnds() {
//        return positionsWithImplicitEnds;
//    }
    
    @JsonIgnore
    public static Set<String> getReachablePositions(JWorkflow workflow) {
        Set<String> pos = new LinkedHashSet<>();
        workflow.reachablePositions(JPosition.apply(js7.data.workflow.position.Position.First())).stream().forEachOrdered(jPos -> {
            if (isReachable(jPos)) {
                pos.add(jPos.toString());
            }
        });
        return pos;
    }
    
//    @JsonIgnore
//    private static JPosition getAfterPos(List<Object> afterPosition) {
//        if (afterPosition != null) {
//            Either<Problem, JPosition> afterPosE = JPosition.fromList(afterPosition);
//            ProblemHelper.throwProblemIfExist(afterPosE);
//            return afterPosE.get();
//        }
//        return null;
//    }
    
    private static boolean isReachable(JPosition jPos) {
        // only root level position or first level inside a "(re)try" or "if" instruction
        List<Object> posA = jPos.toList();
        return posA.size() == 1 || (posA.size() == 3 && (((String) posA.get(1)).contains("try")) || ((String) posA.get(1)).equals("if"));
    }
    
    private static Position createPosition(JPosition jPos, Workflow w) {
        Position p = new Position();
        p.setPosition(jPos.toList());
        p.setPositionString(jPos.toString());
        p.setType(w.instruction(jPos.asScala()).instructionName().replace("Execute.Named", "Job"));
        if ("Job".equals(p.getType())) {
            try {
                p.setLabel(w.labeledInstruction(jPos.asScala()).toOption().get().labelString().trim().replaceFirst(":$", ""));
            } catch (Throwable e) {
                //
            }
        }
        return p;
    }

}
