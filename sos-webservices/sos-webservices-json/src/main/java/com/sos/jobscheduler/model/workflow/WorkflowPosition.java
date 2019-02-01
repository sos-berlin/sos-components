
package com.sos.jobscheduler.model.workflow;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * WorkflowPosition
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowId",
    "position"
})
public class WorkflowPosition {

    /**
     * workflowId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowId")
    private WorkflowId workflowId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    private List<Integer> position = null;

    /**
     * workflowId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowId")
    public WorkflowId getWorkflowId() {
        return workflowId;
    }

    /**
     * workflowId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowId")
    public void setWorkflowId(WorkflowId workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public List<Integer> getPosition() {
        return position;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public void setPosition(List<Integer> position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflowId", workflowId).append("position", position).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowId).append(position).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowPosition) == false) {
            return false;
        }
        WorkflowPosition rhs = ((WorkflowPosition) other);
        return new EqualsBuilder().append(workflowId, rhs.workflowId).append(position, rhs.position).isEquals();
    }

}
