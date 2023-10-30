
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * jobPath
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflowPath",
    "job"
})
public class JobPath {

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
     * if job undefined or empty then all jobs of specified workflow are requested
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("if job undefined or empty then all jobs of specified workflow are requested")
    private String job;

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
     * if job undefined or empty then all jobs of specified workflow are requested
     * 
     */
    @JsonProperty("job")
    public String getJob() {
        return job;
    }

    /**
     * if job undefined or empty then all jobs of specified workflow are requested
     * 
     */
    @JsonProperty("job")
    public void setJob(String job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("workflowPath", workflowPath).append("job", job).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(job).append(workflowPath).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobPath) == false) {
            return false;
        }
        JobPath rhs = ((JobPath) other);
        return new EqualsBuilder().append(job, rhs.job).append(workflowPath, rhs.workflowPath).isEquals();
    }

}
