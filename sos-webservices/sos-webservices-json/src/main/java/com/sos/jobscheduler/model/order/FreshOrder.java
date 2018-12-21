
package com.sos.jobscheduler.model.order;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import com.sos.jobscheduler.model.common.Variables;

public class FreshOrder {

    private String id;

    private String workflowPath;

    private Long scheduledFor;

    private Variables variables;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWorkflowPath() {
        return workflowPath;
    }

    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    public Long getScheduledFor() {
        return scheduledFor;
    }

    public void setScheduledFor(Long scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    public Variables getVariables() {
        return variables;
    }

    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("workflowPath", workflowPath).append("scheduledFor", scheduledFor).append(
                "variables", variables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variables).append(id).append(workflowPath).append(scheduledFor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FreshOrder) == false) {
            return false;
        }
        FreshOrder rhs = ((FreshOrder) other);
        return new EqualsBuilder().append(variables, rhs.variables).append(id, rhs.id).append(workflowPath, rhs.workflowPath).append(scheduledFor,
                rhs.scheduledFor).isEquals();
    }

}
