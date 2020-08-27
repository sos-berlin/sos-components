
package com.sos.joc.model.workflow;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.workflow.WorkflowId;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * workflowsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "workflowIds"
})
public class WorkflowsFilter {

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("workflowIds")
    private List<WorkflowId> workflowIds = new ArrayList<WorkflowId>();

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * filename
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("workflowIds")
    public List<WorkflowId> getWorkflowIds() {
        return workflowIds;
    }

    @JsonProperty("workflowIds")
    public void setWorkflowIds(List<WorkflowId> workflowIds) {
        this.workflowIds = workflowIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("workflowIds", workflowIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(workflowIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowsFilter) == false) {
            return false;
        }
        WorkflowsFilter rhs = ((WorkflowsFilter) other);
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(workflowIds, rhs.workflowIds).isEquals();
    }

}
