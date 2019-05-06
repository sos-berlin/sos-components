
package com.sos.jobscheduler.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.job.Job;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "label",
    "job"
})
public class AnonymousJob
    extends Instruction
    implements IInstructible
{

    @JsonProperty("label")
    private String label;
    /**
     * job
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    private Job job;

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * job
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public Job getJob() {
        return job;
    }

    /**
     * job
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(Job job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("label", label).append("job", job).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(job).append(label).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AnonymousJob) == false) {
            return false;
        }
        AnonymousJob rhs = ((AnonymousJob) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(job, rhs.job).append(label, rhs.label).isEquals();
    }

}
