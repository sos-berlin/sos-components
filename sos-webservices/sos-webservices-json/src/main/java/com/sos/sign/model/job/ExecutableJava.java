
package com.sos.sign.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * java executable
 * <p>
 * executable with fixed property 'TYPE':'InternalExecutable'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "className",
    "jobArguments",
    "arguments"
})
public class ExecutableJava
    extends Executable
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("className")
    private String className;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("jobArguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables jobArguments;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment arguments;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ExecutableJava() {
    }

    /**
     * 
     * @param className
     * @param jobArguments
     * @param arguments
     * @param tYPE
     */
    public ExecutableJava(String className, Variables jobArguments, Environment arguments, ExecutableType tYPE) {
        super(tYPE);
        this.className = className;
        this.jobArguments = jobArguments;
        this.arguments = arguments;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("className")
    public String getClassName() {
        return className;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("className")
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("jobArguments")
    public Variables getJobArguments() {
        return jobArguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("jobArguments")
    public void setJobArguments(Variables jobArguments) {
        this.jobArguments = jobArguments;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Environment getArguments() {
        return arguments;
    }

    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Environment arguments) {
        this.arguments = arguments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("className", className).append("jobArguments", jobArguments).append("arguments", arguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(className).append(jobArguments).append(arguments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExecutableJava) == false) {
            return false;
        }
        ExecutableJava rhs = ((ExecutableJava) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(className, rhs.className).append(jobArguments, rhs.jobArguments).append(arguments, rhs.arguments).isEquals();
    }

}
