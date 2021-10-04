
package com.sos.joc.model.workflow;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.workflow.WorkflowId;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflowIdsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "workflowIds"
})
public class WorkflowIdsFilter {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<WorkflowId> workflowIds = new LinkedHashSet<WorkflowId>();

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowIds")
    public Set<WorkflowId> getWorkflowIds() {
        return workflowIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowIds")
    public void setWorkflowIds(Set<WorkflowId> workflowIds) {
        this.workflowIds = workflowIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("workflowIds", workflowIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(workflowIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowIdsFilter) == false) {
            return false;
        }
        WorkflowIdsFilter rhs = ((WorkflowIdsFilter) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(workflowIds, rhs.workflowIds).isEquals();
    }

}
