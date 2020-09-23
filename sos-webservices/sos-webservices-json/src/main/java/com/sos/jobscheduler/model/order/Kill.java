
package com.sos.jobscheduler.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.workflow.WorkflowPosition;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "immediately",
    "workflowPosition"
})
public class Kill {

    @JsonProperty("immediately")
    private Boolean immediately = false;
    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    private WorkflowPosition workflowPosition;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Kill() {
    }

    /**
     * 
     * @param workflowPosition
     * @param immediately
     */
    public Kill(Boolean immediately, WorkflowPosition workflowPosition) {
        super();
        this.immediately = immediately;
        this.workflowPosition = workflowPosition;
    }

    @JsonProperty("immediately")
    public Boolean getImmediately() {
        return immediately;
    }

    @JsonProperty("immediately")
    public void setImmediately(Boolean immediately) {
        this.immediately = immediately;
    }

    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    public WorkflowPosition getWorkflowPosition() {
        return workflowPosition;
    }

    /**
     * WorkflowPosition
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowPosition")
    public void setWorkflowPosition(WorkflowPosition workflowPosition) {
        this.workflowPosition = workflowPosition;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("immediately", immediately).append("workflowPosition", workflowPosition).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowPosition).append(immediately).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Kill) == false) {
            return false;
        }
        Kill rhs = ((Kill) other);
        return new EqualsBuilder().append(workflowPosition, rhs.workflowPosition).append(immediately, rhs.immediately).isEquals();
    }

}
