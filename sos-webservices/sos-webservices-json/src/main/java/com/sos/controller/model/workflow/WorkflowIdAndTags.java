
package com.sos.controller.model.workflow;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * workflowIdAndTags
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowTags"
})
public class WorkflowIdAndTags
    extends WorkflowId
{

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> workflowTags = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public WorkflowIdAndTags() {
    }

    /**
     * 
     * @param path
     * @param versionId
     * @param workflowTags
     */
    public WorkflowIdAndTags(Set<String> workflowTags, String path, String versionId) {
        super(path, versionId);
        this.workflowTags = workflowTags;
    }
    
    /**
     * 
     * @param path
     * @param versionId
     */
    public WorkflowIdAndTags(String path, String versionId) {
        super(path, versionId);
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public Set<String> getWorkflowTags() {
        return workflowTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public void setWorkflowTags(Set<String> workflowTags) {
        this.workflowTags = workflowTags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowTags", workflowTags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(workflowTags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowIdAndTags) == false) {
            return false;
        }
        WorkflowIdAndTags rhs = ((WorkflowIdAndTags) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(workflowTags, rhs.workflowTags).isEquals();
    }

}
