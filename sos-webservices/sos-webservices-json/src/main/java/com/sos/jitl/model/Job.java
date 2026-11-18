
package com.sos.jitl.model;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * job
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "job",
    "arguments",
    "returnVariables"
})
public class Job {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    private JobDefinition job;
    @JsonProperty("arguments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Param> arguments = null;
    @JsonProperty("returnVariables")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Param> returnVariables = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param returnVariables
     * @param arguments
     * @param job
     */
    public Job(JobDefinition job, Set<Param> arguments, Set<Param> returnVariables) {
        super();
        this.job = job;
        this.arguments = arguments;
        this.returnVariables = returnVariables;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public JobDefinition getJob() {
        return job;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("job")
    public void setJob(JobDefinition job) {
        this.job = job;
    }

    @JsonProperty("arguments")
    public Set<Param> getArguments() {
        return arguments;
    }

    @JsonProperty("arguments")
    public void setArguments(Set<Param> arguments) {
        this.arguments = arguments;
    }

    @JsonProperty("returnVariables")
    public Set<Param> getReturnVariables() {
        return returnVariables;
    }

    @JsonProperty("returnVariables")
    public void setReturnVariables(Set<Param> returnVariables) {
        this.returnVariables = returnVariables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("job", job).append("arguments", arguments).append("returnVariables", returnVariables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(arguments).append(job).append(returnVariables).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Job) == false) {
            return false;
        }
        Job rhs = ((Job) other);
        return new EqualsBuilder().append(arguments, rhs.arguments).append(job, rhs.job).append(returnVariables, rhs.returnVariables).isEquals();
    }

}
