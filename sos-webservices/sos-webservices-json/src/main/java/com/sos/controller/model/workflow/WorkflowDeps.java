
package com.sos.controller.model.workflow;

import java.util.Date;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Requirements;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflow with dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "expectedNoticeBoards",
    "postNoticeBoards",
    "addOrderToWorkflows",
    "addOrderFromWorkflows"
})
public class WorkflowDeps
    extends Workflow
{

    /**
     * workflow with dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("expectedNoticeBoards")
    private BoardWorkflows expectedNoticeBoards;
    /**
     * workflow with dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("postNoticeBoards")
    private BoardWorkflows postNoticeBoards;
    @JsonProperty("addOrderToWorkflows")
    private List<Workflow> addOrderToWorkflows = null;
    @JsonProperty("addOrderFromWorkflows")
    private List<Workflow> addOrderFromWorkflows = null;

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
     * @param hasPostNoticeBoards
     * @param jobResourceNames
     * @param jobs
     * @param timeZone
     * @param title
     * @param versionDate
     * @param version
     * @param addOrderToWorkflows
     * @param postNoticeBoards
     * @param orderPreparation
     * @param path
     * @param fileOrderSources
     * @param hasAddOrderDependencies
     * @param versionId
     * @param forkListVariables
     * @param expectedNoticeBoards
     * @param isCurrentVersion
     * @param state
     * @param documentationName
     * @param addOrderFromWorkflows
     */
    public WorkflowDeps(BoardWorkflows expectedNoticeBoards, BoardWorkflows postNoticeBoards, List<Workflow> addOrderToWorkflows, List<Workflow> addOrderFromWorkflows, String path, Boolean isCurrentVersion, Date versionDate, SyncState state, List<FileOrderSource> fileOrderSources, Set<String> forkListVariables, Boolean hasExpectedNoticeBoards, Boolean hasPostNoticeBoards, Boolean hasAddOrderDependencies, String version, String versionId, String timeZone, String title, String documentationName, Requirements orderPreparation, List<String> jobResourceNames, List<Instruction> instructions, Jobs jobs) {
        super(path, isCurrentVersion, versionDate, state, fileOrderSources, forkListVariables, hasExpectedNoticeBoards, hasPostNoticeBoards, hasAddOrderDependencies, version, versionId, timeZone, title, documentationName, orderPreparation, jobResourceNames, instructions, jobs);
        this.expectedNoticeBoards = expectedNoticeBoards;
        this.postNoticeBoards = postNoticeBoards;
        this.addOrderToWorkflows = addOrderToWorkflows;
        this.addOrderFromWorkflows = addOrderFromWorkflows;
    }

    /**
     * workflow with dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("expectedNoticeBoards")
    public BoardWorkflows getExpectedNoticeBoards() {
        return expectedNoticeBoards;
    }

    /**
     * workflow with dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("expectedNoticeBoards")
    public void setExpectedNoticeBoards(BoardWorkflows expectedNoticeBoards) {
        this.expectedNoticeBoards = expectedNoticeBoards;
    }

    /**
     * workflow with dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("postNoticeBoards")
    public BoardWorkflows getPostNoticeBoards() {
        return postNoticeBoards;
    }

    /**
     * workflow with dependencies
     * <p>
     * 
     * 
     */
    @JsonProperty("postNoticeBoards")
    public void setPostNoticeBoards(BoardWorkflows postNoticeBoards) {
        this.postNoticeBoards = postNoticeBoards;
    }

    @JsonProperty("addOrderToWorkflows")
    public List<Workflow> getAddOrderToWorkflows() {
        return addOrderToWorkflows;
    }

    @JsonProperty("addOrderToWorkflows")
    public void setAddOrderToWorkflows(List<Workflow> addOrderToWorkflows) {
        this.addOrderToWorkflows = addOrderToWorkflows;
    }

    @JsonProperty("addOrderFromWorkflows")
    public List<Workflow> getAddOrderFromWorkflows() {
        return addOrderFromWorkflows;
    }

    @JsonProperty("addOrderFromWorkflows")
    public void setAddOrderFromWorkflows(List<Workflow> addOrderFromWorkflows) {
        this.addOrderFromWorkflows = addOrderFromWorkflows;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("expectedNoticeBoards", expectedNoticeBoards).append("postNoticeBoards", postNoticeBoards).append("addOrderToWorkflows", addOrderToWorkflows).append("addOrderFromWorkflows", addOrderFromWorkflows).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(expectedNoticeBoards).append(addOrderToWorkflows).append(addOrderFromWorkflows).append(postNoticeBoards).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(expectedNoticeBoards, rhs.expectedNoticeBoards).append(addOrderToWorkflows, rhs.addOrderToWorkflows).append(addOrderFromWorkflows, rhs.addOrderFromWorkflows).append(postNoticeBoards, rhs.postNoticeBoards).isEquals();
    }

}
