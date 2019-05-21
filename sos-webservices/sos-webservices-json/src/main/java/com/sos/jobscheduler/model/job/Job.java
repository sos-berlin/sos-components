
package com.sos.jobscheduler.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.Variables;
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
    "agentRefPath",
    "executable",
    "returnCodeMeaning",
    "taskLimit",
    "defaultArguments"
})
public class Job {

    /**
     * path
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentRefPath")
    private String agentRefPath;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    private Executable executable;
    /**
     * job
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    private JobReturnCode returnCodeMeaning;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("taskLimit")
    private Integer taskLimit;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables defaultArguments;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param returnCodeMeaning
     * @param taskLimit
     * @param defaultArguments
     * @param agentRefPath
     * @param executable
     */
    public Job(String agentRefPath, Executable executable, JobReturnCode returnCodeMeaning, Integer taskLimit, Variables defaultArguments) {
        super();
        this.agentRefPath = agentRefPath;
        this.executable = executable;
        this.returnCodeMeaning = returnCodeMeaning;
        this.taskLimit = taskLimit;
        this.defaultArguments = defaultArguments;
    }

    /**
     * path
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentRefPath")
    public String getAgentRefPath() {
        return agentRefPath;
    }

    /**
     * path
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentRefPath")
    public void setAgentRefPath(String agentRefPath) {
        this.agentRefPath = agentRefPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    public Executable getExecutable() {
        return executable;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    public void setExecutable(Executable executable) {
        this.executable = executable;
    }

    /**
     * job
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    public JobReturnCode getReturnCodeMeaning() {
        return returnCodeMeaning;
    }

    /**
     * job
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    public void setReturnCodeMeaning(JobReturnCode returnCodeMeaning) {
        this.returnCodeMeaning = returnCodeMeaning;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("taskLimit")
    public Integer getTaskLimit() {
        return taskLimit;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("taskLimit")
    public void setTaskLimit(Integer taskLimit) {
        this.taskLimit = taskLimit;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    public Variables getDefaultArguments() {
        return defaultArguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    public void setDefaultArguments(Variables defaultArguments) {
        this.defaultArguments = defaultArguments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentRefPath", agentRefPath).append("executable", executable).append("returnCodeMeaning", returnCodeMeaning).append("taskLimit", taskLimit).append("defaultArguments", defaultArguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(returnCodeMeaning).append(taskLimit).append(agentRefPath).append(defaultArguments).append(executable).toHashCode();
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
        return new EqualsBuilder().append(returnCodeMeaning, rhs.returnCodeMeaning).append(taskLimit, rhs.taskLimit).append(agentRefPath, rhs.agentRefPath).append(defaultArguments, rhs.defaultArguments).append(executable, rhs.executable).isEquals();
    }

}
