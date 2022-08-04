
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
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
    "returnCodeMeaning",
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
     * job return code warning
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    private JobReturnCodeWarning returnCodeMeaning;
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
     * @param returnCodeMeaning
     * @param className
     * @param jobArguments
     * @param arguments
     * @param tYPE
     */
    public ExecutableJava(String className, Variables jobArguments, JobReturnCodeWarning returnCodeMeaning, Environment arguments, ExecutableType tYPE) {
        super(tYPE);
        this.className = className;
        this.jobArguments = jobArguments;
        this.returnCodeMeaning = returnCodeMeaning;
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
     * job return code warning
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    public JobReturnCodeWarning getReturnCodeMeaning() {
        return returnCodeMeaning;
    }

    /**
     * job return code warning
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    public void setReturnCodeMeaning(JobReturnCodeWarning returnCodeMeaning) {
        this.returnCodeMeaning = returnCodeMeaning;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("className", className).append("jobArguments", jobArguments).append("returnCodeMeaning", returnCodeMeaning).append("arguments", arguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(returnCodeMeaning).append(className).append(jobArguments).append(arguments).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(returnCodeMeaning, rhs.returnCodeMeaning).append(className, rhs.className).append(jobArguments, rhs.jobArguments).append(arguments, rhs.arguments).isEquals();
    }

}
