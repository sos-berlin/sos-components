
package com.sos.joc.model.jobtemplate.propagate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter for Job updates from JobTemplates
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowPath",
    "jobName"
})
public class JobPropagateFilter
    extends JobTemplatesPropagateBaseFilter
{

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    private String workflowPath;
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    private String jobName;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    public String getJobName() {
        return jobName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("workflowPath", workflowPath).append("jobName", jobName).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jobName).append(workflowPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobPropagateFilter) == false) {
            return false;
        }
        JobPropagateFilter rhs = ((JobPropagateFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jobName, rhs.jobName).append(workflowPath, rhs.workflowPath).isEquals();
    }

}
