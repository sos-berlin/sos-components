package com.sos.joc.db.deploy.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.NoticeToNoticesConverter;
import com.sos.joc.classes.inventory.WorkflowConverter;
import com.sos.joc.classes.workflow.WorkflowsHelper;

public class DeployedWorkflowWithBoards {

    private String instructionJson;
    private String workflowJson;
    private String path;
    private String commitId;
    
    public DeployedWorkflowWithBoards(String path, String commitId, String instructionJson) {
        this.path = path;
        this.instructionJson = instructionJson;
        this.commitId = commitId;
        this.workflowJson = null;
    }
    
    public DeployedWorkflowWithBoards(String path, String commitId, String workflowJson, String instructionJson) {
        this.path = path;
        this.instructionJson = instructionJson;
        this.commitId = commitId;
        this.workflowJson = workflowJson;
    }
    
    public WorkflowBoards mapToWorkflowBoardsWithTopLevelPositions() {
        try {
            WorkflowBoards wb = init();
            Map<String, Set<String>> topLevelPositions = new HashMap<>();
            if (workflowJson != null && wb.hasConsumeNotice() + wb.hasExpectNotice() > 0) {
                Workflow w = WorkflowConverter.convertInventoryWorkflow(workflowJson, Workflow.class);
                List<Instruction> instructions = w.getInstructions();
                if (instructions != null) {
                    for (int i = 0; i < instructions.size(); i++) {
                        Instruction inst = instructions.get(i);
                        if (inst != null) {
                            switch (inst.getTYPE()) {
                            case CONSUME_NOTICES:
                                ConsumeNotices cns = inst.cast();
                                String cnsNamesExpr = cns.getNoticeBoardNames();
                                Set<String> cnsNames = NoticeToNoticesConverter.expectNoticeBoardsToSet(cnsNamesExpr);
                                if (cnsNames != null && !cnsNames.isEmpty()) {
                                    topLevelPositions.put(i + "", cnsNames);
                                }
                                break;
                            case EXPECT_NOTICES:
                                ExpectNotices ens = inst.cast();
                                String ensNamesExpr = ens.getNoticeBoardNames();
                                Set<String> ensNames = NoticeToNoticesConverter.expectNoticeBoardsToSet(ensNamesExpr);
                                if (ensNames != null && !ensNames.isEmpty()) {
                                    topLevelPositions.put(i + "", ensNames);
                                }
                                break;
                            default:
                                break;
                            }
                        }
                    }
                }
            }
            wb.setTopLevelPositions(topLevelPositions);
            return wb;
        } catch (Exception e) {
            return null;
        }
    }
    
    public WorkflowBoards mapToWorkflowBoardsWithPositions() {
        try {
            WorkflowBoards wb = init();
            if (workflowJson != null && wb.hasConsumeNotice() + wb.hasExpectNotice() + wb.hasPostNotice() > 0) {
                Workflow w = WorkflowConverter.convertInventoryWorkflow(workflowJson, Workflow.class);
                //wb = WorkflowsHelper.setWorkflowBoardTopLevelPositions(w.getInstructions(), wb);
                wb = WorkflowsHelper.setWorkflowBoardPositions(w.getInstructions(), wb);
            }
            return wb;
        } catch (Exception e) {
            return null;
        }
    }
    
    public WorkflowBoards mapToWorkflowBoards() {
        try {
            return init();
        } catch (Exception e) {
            return null;
        }
    }
    
    @JsonIgnore
    public String getName() {
        return JocInventory.pathToName(path);
    }
    
    private WorkflowBoards init() throws JsonMappingException, JsonProcessingException {
        WorkflowBoards wb = Globals.objectMapper.readValue(instructionJson, WorkflowBoards.class);
        wb.setPath(path);
        wb.setVersionId(commitId);
        return wb;
    }
    
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(commitId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployedWorkflowWithBoards) == false) {
            return false;
        }
        DeployedWorkflowWithBoards rhs = ((DeployedWorkflowWithBoards) other);
        return new EqualsBuilder().append(path, rhs.path).append(commitId, rhs.commitId).isEquals();
    }
}
