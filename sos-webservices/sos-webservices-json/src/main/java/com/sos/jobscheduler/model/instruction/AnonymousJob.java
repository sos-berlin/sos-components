
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
    "TYPE",
    "job",
    "label"
})
public class AnonymousJob
    extends Instruction
{

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
    private String label;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AnonymousJob() {
    }

    public AnonymousJob(Job job) {
        super();
        this.job = job;
    }

    /**
     * 
     * @param label
     * @param job
     */
    public AnonymousJob(Job job, String label) {
        super();
        this.job = job;
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

    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("job", job).append("label", label).toString();
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
