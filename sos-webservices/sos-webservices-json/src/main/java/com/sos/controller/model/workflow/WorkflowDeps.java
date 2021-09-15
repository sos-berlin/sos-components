
package com.sos.controller.model.workflow;

import java.util.Date;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.fileordersource.FileOrderSource;
import com.sos.inventory.model.deploy.DeployType;
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
    "postNoticeBoards"
})
public class WorkflowDeps
    extends Workflow
{

    @JsonProperty("expectedNoticeBoards")
    private BoardWorkflows expectedNoticeBoards;
    @JsonProperty("postNoticeBoards")
    private BoardWorkflows postNoticeBoards;

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
     * @param tYPE
     * @param title
     * @param versionDate
     * @param version
     * @param postNoticeBoards
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
    public WorkflowDeps(BoardWorkflows expectedNoticeBoards, BoardWorkflows postNoticeBoards, String path, Boolean isCurrentVersion, Date versionDate, SyncState state, List<FileOrderSource> fileOrderSources, Set<String> forkListVariables, Boolean hasExpectedNoticeBoards, Boolean hasPostNoticeBoards, DeployType tYPE, String version, String versionId, String timeZone, String title, String documentationName, Requirements orderPreparation, List<String> jobResourceNames, List<Instruction> instructions, Jobs jobs) {
        super(path, isCurrentVersion, versionDate, state, fileOrderSources, forkListVariables, hasExpectedNoticeBoards, hasPostNoticeBoards, tYPE, version, versionId, timeZone, title, documentationName, orderPreparation, jobResourceNames, instructions, jobs);
        this.expectedNoticeBoards = expectedNoticeBoards;
        this.postNoticeBoards = postNoticeBoards;
    }

    @JsonProperty("expectedNoticeBoards")
    public BoardWorkflows getExpectedNoticeBoards() {
        return expectedNoticeBoards;
    }

    @JsonProperty("expectedNoticeBoards")
    public void setExpectedNoticeBoards(BoardWorkflows expectedNoticeBoards) {
        this.expectedNoticeBoards = expectedNoticeBoards;
    }

    @JsonProperty("postNoticeBoards")
    public BoardWorkflows getPostNoticeBoards() {
        return postNoticeBoards;
    }

    @JsonProperty("postNoticeBoards")
    public void setPostNoticeBoards(BoardWorkflows postNoticeBoards) {
        this.postNoticeBoards = postNoticeBoards;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("expectedNoticeBoards", expectedNoticeBoards).append("postNoticeBoards", postNoticeBoards).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(expectedNoticeBoards).append(postNoticeBoards).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(expectedNoticeBoards, rhs.expectedNoticeBoards).append(postNoticeBoards, rhs.postNoticeBoards).isEquals();
    }

}
