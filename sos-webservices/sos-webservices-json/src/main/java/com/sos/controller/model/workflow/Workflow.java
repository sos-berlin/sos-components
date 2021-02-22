
package com.sos.controller.model.workflow;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.workflow.Jobs;
import com.sos.inventory.model.workflow.Requirements;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflow
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "isCurrentVersion",
    "state"
})
public class Workflow
    extends com.sos.inventory.model.workflow.Workflow
{

    @JsonProperty("isCurrentVersion")
    private Boolean isCurrentVersion = true;
    /**
     * workflow state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    private WorkflowState state;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Workflow() {
    }

    /**
     * 
     * @param documentationPath
     * @param path
     * @param instructions
     * @param versionId
     * @param isCurrentVersion
     * @param orderRequirements
     * @param jobs
     * @param state
     * @param title
     */
    public Workflow(Boolean isCurrentVersion, WorkflowState state, String path, String versionId, Requirements orderRequirements, List<Instruction> instructions, String title, String documentationPath, Jobs jobs) {
        super(path, versionId, orderRequirements, instructions, title, documentationPath, jobs);
        this.isCurrentVersion = isCurrentVersion;
        this.state = state;
    }

    @JsonProperty("isCurrentVersion")
    public Boolean getIsCurrentVersion() {
        return isCurrentVersion;
    }

    @JsonProperty("isCurrentVersion")
    public void setIsCurrentVersion(Boolean isCurrentVersion) {
        this.isCurrentVersion = isCurrentVersion;
    }

    /**
     * workflow state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public WorkflowState getState() {
        return state;
    }

    /**
     * workflow state
     * <p>
     * 
     * 
     */
    @JsonProperty("state")
    public void setState(WorkflowState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("isCurrentVersion", isCurrentVersion).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(isCurrentVersion).append(state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Workflow) == false) {
            return false;
        }
        Workflow rhs = ((Workflow) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(isCurrentVersion, rhs.isCurrentVersion).append(state, rhs.state).isEquals();
    }

}
