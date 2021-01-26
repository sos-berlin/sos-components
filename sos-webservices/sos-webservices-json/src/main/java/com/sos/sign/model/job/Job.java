
package com.sos.sign.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.Variables;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.job.JobReturnCode;
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
    "agentId",
    "executable",
    "returnCodeMeaning",
    "taskLimit",
    "timeout",
    "graceTimeout",
    "jobClass",
    "defaultArguments",
    "env"
})
public class Job {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    private String agentId;
    /**
     * executable script
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    private ExecutableScript executable;
    /**
     * job
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    private JobReturnCode returnCodeMeaning;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskLimit")
    private Integer taskLimit = 1;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    private Integer timeout;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("graceTimeout")
    private Integer graceTimeout;
    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("jobClass")
    @JsonPropertyDescription("absolute path of an object.")
    private String jobClass;
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
     * key-value pairs particulraly to assign parameters to environemnt
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment env;

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
     * @param agentId
     * @param graceTimeout
     * @param defaultArguments
     * @param jobClass
     * @param env
     * @param executable
     * @param timeout
     */
    public Job(String agentId, ExecutableScript executable, JobReturnCode returnCodeMeaning, Integer taskLimit, Integer timeout, Integer graceTimeout, String jobClass, Variables defaultArguments, Environment env) {
        super();
        this.agentId = agentId;
        this.executable = executable;
        this.returnCodeMeaning = returnCodeMeaning;
        this.taskLimit = taskLimit;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.jobClass = jobClass;
        this.defaultArguments = defaultArguments;
        this.env = env;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * executable script
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    public ExecutableScript getExecutable() {
        return executable;
    }

    /**
     * executable script
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    public void setExecutable(ExecutableScript executable) {
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
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskLimit")
    public Integer getTaskLimit() {
        return taskLimit;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("taskLimit")
    public void setTaskLimit(Integer taskLimit) {
        this.taskLimit = taskLimit;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    public Integer getTimeout() {
        return timeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    public void setTimeout(Integer timeout) {
        this.timeout = timeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("graceTimeout")
    public Integer getGraceTimeout() {
        return graceTimeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("graceTimeout")
    public void setGraceTimeout(Integer graceTimeout) {
        this.graceTimeout = graceTimeout;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("jobClass")
    public String getJobClass() {
        return jobClass;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("jobClass")
    public void setJobClass(String jobClass) {
        this.jobClass = jobClass;
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

    /**
     * key-value pairs particulraly to assign parameters to environemnt
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    public Environment getEnv() {
        return env;
    }

    /**
     * key-value pairs particulraly to assign parameters to environemnt
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("env")
    public void setEnv(Environment env) {
        this.env = env;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("executable", executable).append("returnCodeMeaning", returnCodeMeaning).append("taskLimit", taskLimit).append("timeout", timeout).append("graceTimeout", graceTimeout).append("jobClass", jobClass).append("defaultArguments", defaultArguments).append("env", env).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(returnCodeMeaning).append(taskLimit).append(agentId).append(graceTimeout).append(defaultArguments).append(jobClass).append(env).append(executable).append(timeout).toHashCode();
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
        return new EqualsBuilder().append(returnCodeMeaning, rhs.returnCodeMeaning).append(taskLimit, rhs.taskLimit).append(agentId, rhs.agentId).append(graceTimeout, rhs.graceTimeout).append(defaultArguments, rhs.defaultArguments).append(jobClass, rhs.jobClass).append(env, rhs.env).append(executable, rhs.executable).append(timeout, rhs.timeout).isEquals();
    }

}
