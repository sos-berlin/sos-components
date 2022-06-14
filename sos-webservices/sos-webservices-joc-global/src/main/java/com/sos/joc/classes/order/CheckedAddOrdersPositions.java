package com.sos.joc.classes.order;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.OrdersPositions;
import com.sos.joc.model.order.Positions;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.position.Position;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;

public class CheckedAddOrdersPositions extends OrdersPositions {
    
    @JsonIgnore
    private Set<Positions> positionsWithImplicitEnds = new LinkedHashSet<>();
    
    public CheckedAddOrdersPositions() {
        //
    }
    
    @JsonIgnore
    public CheckedAddOrdersPositions get(WorkflowId workflowId, JControllerState currentState, Set<Folder> permittedFolders) throws JsonParseException,
            JsonMappingException, IOException, JocException {
        
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
        JsonNode node = Globals.objectMapper.readTree(e.get().withPositions().toJson());
        List<Instruction> instructions = Globals.objectMapper.reader().forType(new TypeReference<List<Instruction>>() {}).readValue(node.get("instructions"));
        // List<Instruction> instructions = Arrays.asList(Globals.objectMapper.reader().treeToValue(node.get("instructions"), Instruction[].class));
        Set<String> implicitEnds = WorkflowsHelper.extractImplicitEnds(instructions);

        final Set<Positions> pos = new LinkedHashSet<>();
        JPosition pos0 = JPosition.apply(Position.First());

        e.get().reachablePositions(pos0).stream().forEachOrdered(jPos -> {
            // only root level position or first level inside a "(re)try" instruction
            // if (jPos.toList().size() == 1 || (jPos.toList().size() == 3 && ((String) jPos.toList().get(1)).contains("try"))) {
            if (jPos.toList().size() == 1) { // only root level position is yet implemented
                String positionString = jPos.toString();
                Positions p = new Positions();
                p.setPosition(jPos.toList());
                p.setPositionString(positionString);
                positionsWithImplicitEnds.add(p);
                if (!implicitEnds.contains(p.getPositionString())) {
                    pos.add(p);
                }
            }
        });

        setWorkflowId(workflowId);
        setPositions(pos);

        setDeliveryDate(Date.from(Instant.now()));
        setSurveyDate(Date.from(currentState.instant()));
        
        return this;
    }
    
    @JsonIgnore
    public Set<Positions> getPositionsWithImplicitEnds() {
        return positionsWithImplicitEnds;
    }

}
