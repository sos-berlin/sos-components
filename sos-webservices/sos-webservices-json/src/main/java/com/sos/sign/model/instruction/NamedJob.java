
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.Environment;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job
 * <p>
 * instruction with fixed property 'TYPE':'Execute.Named'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jobName",
    "label",
    "defaultArguments"
})
public class NamedJob
    extends Instruction
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    private String jobName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("label")
    private String label;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment defaultArguments;

    /**
     * No args constructor for use in serialization
     * 
     */
    public NamedJob() {
    }

    /**
     * 
     * @param jobName
     * @param defaultArguments
     * @param label
     */
    public NamedJob(String jobName, String label, Environment defaultArguments) {
        super();
        this.jobName = jobName;
        this.label = label;
        this.defaultArguments = defaultArguments;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    public String getJobName() {
        return jobName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    public Environment getDefaultArguments() {
        return defaultArguments;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    public void setDefaultArguments(Environment defaultArguments) {
        this.defaultArguments = defaultArguments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jobName", jobName).append("label", label).append("defaultArguments", defaultArguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jobName).append(label).append(defaultArguments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof NamedJob) == false) {
            return false;
        }
        NamedJob rhs = ((NamedJob) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jobName, rhs.jobName).append(label, rhs.label).append(defaultArguments, rhs.defaultArguments).isEquals();
    }

}
