
package com.sos.controller.model.workflow;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Requirements;


/**
 * workflow with dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "expectedNoticeBoards"
})
public class WorkflowDeps
    extends Workflow
{

    @JsonProperty("expectedNoticeBoards")
    private ExpectedWorkflows expectedNoticeBoards;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowDeps() {
    }

    /**
     * 
     * @param hasExpectedNoticeBoards
     * @param instructions
     * @param jobResourceNames
     * @param jobs
     * @param title
     * @param versionDate
     * @param orderPreparation
     * @param path
     * @param fileOrderSources
     * @param versionId
     * @param forkListVariables
     * @param expectedNoticeBoards
     * @param isCurrentVersion
     * @param state
     * @param documentationName
     */
    public WorkflowDeps(ExpectedWorkflows expectedNoticeBoards, String path, Boolean isCurrentVersion, Date versionDate, SyncState state, List<FileOrderSource> fileOrderSources, Set<String> forkListVariables, Boolean hasExpectedNoticeBoards, String versionId, Requirements orderPreparation, List<String> jobResourceNames, List<Instruction> instructions, String title, String documentationName, Jobs jobs) {
        super(path, isCurrentVersion, versionDate, state, fileOrderSources, forkListVariables, hasExpectedNoticeBoards, versionId, orderPreparation, jobResourceNames, instructions, title, documentationName, jobs);
        this.expectedNoticeBoards = expectedNoticeBoards;
    }

    @JsonProperty("expectedNoticeBoards")
    public ExpectedWorkflows getExpectedNoticeBoards() {
        return expectedNoticeBoards;
    }

    @JsonProperty("expectedNoticeBoards")
    public void setExpectedNoticeBoards(ExpectedWorkflows expectedNoticeBoards) {
        this.expectedNoticeBoards = expectedNoticeBoards;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("expectedNoticeBoards", expectedNoticeBoards).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(expectedNoticeBoards).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowDeps) == false) {
            return false;
        }
        WorkflowDeps rhs = ((WorkflowDeps) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(expectedNoticeBoards, rhs.expectedNoticeBoards).isEquals();
    }

}
