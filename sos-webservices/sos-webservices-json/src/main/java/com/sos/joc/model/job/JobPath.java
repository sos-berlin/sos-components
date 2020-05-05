
package com.sos.joc.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobPath
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "workflow",
    "job"
})
public class JobPath {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String workflow;
    /**
     * if job undefined or empty then all jobs of specified workflow are requested
     * 
     */
    @JsonProperty("job")
    @JsonPropertyDescription("if job undefined or empty then all jobs of specified workflow are requested")
    private String job;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public String getWorkflow() {
        return workflow;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
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
        return new ToStringBuilder(this).append("workflow", workflow).append("job", job).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflow).append(job).toHashCode();
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
        return new EqualsBuilder().append(workflow, rhs.workflow).append(job, rhs.job).isEquals();
    }

}
