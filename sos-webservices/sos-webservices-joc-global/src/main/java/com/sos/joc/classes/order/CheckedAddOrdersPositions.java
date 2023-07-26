package com.sos.joc.classes.order;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.DeployedConfigurationFilter;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.BlockPosition;
import com.sos.joc.model.order.OrdersPositions;
import com.sos.joc.model.order.Position;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.workflow.Workflow;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JBranchPath;
import js7.data_for_java.workflow.position.JPosition;

public class CheckedAddOrdersPositions extends OrdersPositions {
    
    public CheckedAddOrdersPositions() {
        //
    }
    
    @JsonIgnore
    public CheckedAddOrdersPositions get(WorkflowId workflowId, String controllerId, JControllerState currentState, Set<Folder> permittedFolders)
            throws JsonParseException, JsonMappingException, IOException, JocException {
        return get(workflowId, controllerId, currentState, permittedFolders, null);
    }
    
    @JsonIgnore
    public CheckedAddOrdersPositions get(WorkflowId workflowId, String controllerId, JControllerState currentState, Set<Folder> permittedFolders,
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
        
//        JPosition pos0 = JPosition.apply(js7.data.workflow.position.Position.First());
        
        setWorkflowId(workflowId);
        setPositions(getPositions(JBranchPath.empty(), w));
        setBlockPositions(getBlockPositions(w, controllerId));

        setDeliveryDate(Date.from(Instant.now()));
        setSurveyDate(Date.from(currentState.instant()));
        
        return this;
    }
    
    @JsonIgnore
    private static Set<Position> getPositions(JBranchPath parentFrom, JWorkflow w) {
        Set<Position> pos = new LinkedHashSet<>();
        JPosition from = getFirstPositionOfBranch(parentFrom);
        w.reachablePositions(from).stream().forEachOrdered(jPos -> {
            if (isReachable(jPos, parentFrom)) {
                Position p = createPosition(jPos, w.asScala());
                pos.add(p);
            }
        });
        
        if (pos.isEmpty()) {
            Position p = createPosition(from, w.asScala());
            pos.add(p);
        }
        return pos;
    }
    
    @JsonIgnore
    private static JPosition getFirstPositionOfBranch(JBranchPath from) {
        return getFirstPositionOfBranch(from.toList());
    }
    
    @JsonIgnore
    private static JPosition getFirstPositionOfBranch(List<Object> fromBranch) {
        List<Object> l = new ArrayList<>(fromBranch);
        l.add(0);
        return JPosition.fromList(l).get();
    }
    
    @JsonIgnore
    private static Set<Position> getPositions(List<Object> fromBranch, JWorkflow w) {
        return getPositions(JBranchPath.fromList(fromBranch).get(), w);
    }
    
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
    
    @JsonIgnore
    public static Set<BlockPosition> getBlockPositions(JWorkflow workflow, String controllerId) throws IOException {
        return WorkflowsHelper.getWorkflowBlockPositions(getInstructions(workflow, controllerId));
    }
    
    private static List<Instruction> getInstructions(JWorkflow workflow, String controllerId) throws IOException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./orders/add/positions");
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
            DeployedConfigurationFilter dbFilter = new DeployedConfigurationFilter();
            dbFilter.setControllerId(controllerId);
            dbFilter.setNames(Collections.singleton(workflow.id().path().string()));
            dbFilter.setObjectTypes(Collections.singleton(DeployType.WORKFLOW.intValue()));
            List<DeployedContent> dbWorkflows = dbLayer.getDeployedInventory(dbFilter);
            if (dbWorkflows != null && !dbWorkflows.isEmpty() && dbWorkflows.get(0).getContent() != null) {
                return JocInventory.workflowContent2Workflow(dbWorkflows.get(0).getContent()).getInstructions();
            } else {
                throw new DBMissingDataException("Couldn't find workflow '" + workflow.id().path().string() + "' as deployed object in database");
            }
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static boolean isReachable(JPosition jPos) {
        // only root level position or first level inside a "(re)try" or "if" instruction

        List<Object> posA = jPos.toList();
        return posA.size() == 1 || (posA.size() == 3 && (((String) posA.get(1)).contains("try") || ((String) posA.get(1)).equals("then")
                || ((String) posA.get(1)).equals("options")));
    }
    
    private static boolean isReachable(JPosition jPos, JBranchPath parentPos) {
        // only root level position or first level inside a "(re)try" or "if" instruction
        
        if (parentPos.toString().isEmpty()) {
            return isReachable(jPos);
        }
        
        if (!jPos.toString().startsWith(parentPos.toString() + ":")) {
            return false;
        }
        JPosition relativePosition = getRelativePosition(jPos, parentPos);
        if (relativePosition == null) {
            return false;
        }
        return isReachable(relativePosition);
    }
    
    private static JPosition getRelativePosition(JPosition jPos, JBranchPath parentPos) {
        List<Object> posAsList = jPos.toList();
        return JPosition.fromList(posAsList.subList(parentPos.toList().size(), posAsList.size())).getOrNull();
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
