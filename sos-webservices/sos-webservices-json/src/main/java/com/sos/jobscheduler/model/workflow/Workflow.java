
package com.sos.jobscheduler.model.workflow;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.deploy.Deployable;
import com.sos.jobscheduler.model.deploy.IDeployable;
import com.sos.jobscheduler.model.instruction.IInstructible;
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
    "instructions",
    "jobs"
})
public class Workflow
    extends Deployable
    implements IDeployable
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    private List<IInstructible> instructions = null;
    @JsonProperty("jobs")
    private Jobs jobs;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    public List<IInstructible> getInstructions() {
        return instructions;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instructions")
    public void setInstructions(List<IInstructible> instructions) {
        this.instructions = instructions;
    }

    @JsonProperty("jobs")
    public Jobs getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(Jobs jobs) {
        this.jobs = jobs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("instructions", instructions).append("jobs", jobs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(instructions).append(jobs).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(instructions, rhs.instructions).append(jobs, rhs.jobs).isEquals();
    }

}
