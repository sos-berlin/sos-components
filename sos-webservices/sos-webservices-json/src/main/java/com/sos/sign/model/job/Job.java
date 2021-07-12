
package com.sos.sign.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.job.Executable;
import com.sos.inventory.model.job.JobReturnCode;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job
 * <p>
 * returnCodeMeaning is deprecated: moved to ShellScriptExecutable
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "agentPath",
    "executable",
    "returnCodeMeaning",
    "parallelism",
    "timeout",
    "sigkillDelay",
    "failOnErrWritten",
    "jobClass",
    "defaultArguments",
    "jobResourcePaths"
})
public class Job {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    @JsonAlias({
        "agentId",
        "agentName"
    })
    private String agentPath;
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
    @JsonProperty("parallelism")
    @JsonAlias({
        "taskLimit"
    })
    private Integer parallelism = 1;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("timeout")
    private Integer timeout;
    /**
     * in seconds
     * 
     */
    @JsonProperty("sigkillDelay")
    @JsonPropertyDescription("in seconds")
    @JsonAlias({
        "graceTimeout"
    })
    private Integer sigkillDelay = 15;
    @JsonProperty("failOnErrWritten")
    private Boolean failOnErrWritten = false;
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
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment defaultArguments;
    @JsonProperty("jobResourcePaths")
    @JsonAlias({
        "jobResourceNames"
    })
    private List<String> jobResourcePaths = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param agentPath
     * @param returnCodeMeaning
     * @param sigkillDelay
     * @param defaultArguments
     * @param jobResourcePaths
     * @param jobClass
     * @param parallelism
     * @param failOnErrWritten
     * @param executable
     * @param timeout
     */
    public Job(String agentPath, Executable executable, JobReturnCode returnCodeMeaning, Integer parallelism, Integer timeout, Integer sigkillDelay, Boolean failOnErrWritten, String jobClass, Environment defaultArguments, List<String> jobResourcePaths) {
        super();
        this.agentPath = agentPath;
        this.executable = executable;
        this.returnCodeMeaning = returnCodeMeaning;
        this.parallelism = parallelism;
        this.timeout = timeout;
        this.sigkillDelay = sigkillDelay;
        this.failOnErrWritten = failOnErrWritten;
        this.jobClass = jobClass;
        this.defaultArguments = defaultArguments;
        this.jobResourcePaths = jobResourcePaths;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public String getAgentPath() {
        return agentPath;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentPath")
    public void setAgentPath(String agentPath) {
        this.agentPath = agentPath;
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

    @JsonProperty("parallelism")
    public Integer getParallelism() {
        return parallelism;
    }

    @JsonProperty("parallelism")
    public void setParallelism(Integer parallelism) {
        this.parallelism = parallelism;
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
     * in seconds
     * 
     */
    @JsonProperty("sigkillDelay")
    public Integer getSigkillDelay() {
        return sigkillDelay;
    }

    /**
     * in seconds
     * 
     */
    @JsonProperty("sigkillDelay")
    public void setSigkillDelay(Integer sigkillDelay) {
        this.sigkillDelay = sigkillDelay;
    }

    @JsonProperty("failOnErrWritten")
    public Boolean getFailOnErrWritten() {
        return failOnErrWritten;
    }

    @JsonProperty("failOnErrWritten")
    public void setFailOnErrWritten(Boolean failOnErrWritten) {
        this.failOnErrWritten = failOnErrWritten;
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

    @JsonProperty("jobResourcePaths")
    public List<String> getJobResourcePaths() {
        return jobResourcePaths;
    }

    @JsonProperty("jobResourcePaths")
    public void setJobResourcePaths(List<String> jobResourcePaths) {
        this.jobResourcePaths = jobResourcePaths;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentPath", agentPath).append("executable", executable).append("returnCodeMeaning", returnCodeMeaning).append("parallelism", parallelism).append("timeout", timeout).append("sigkillDelay", sigkillDelay).append("failOnErrWritten", failOnErrWritten).append("jobClass", jobClass).append("defaultArguments", defaultArguments).append("jobResourcePaths", jobResourcePaths).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(agentPath).append(returnCodeMeaning).append(sigkillDelay).append(defaultArguments).append(jobResourcePaths).append(jobClass).append(parallelism).append(failOnErrWritten).append(executable).append(timeout).toHashCode();
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
        return new EqualsBuilder().append(agentPath, rhs.agentPath).append(returnCodeMeaning, rhs.returnCodeMeaning).append(sigkillDelay, rhs.sigkillDelay).append(defaultArguments, rhs.defaultArguments).append(jobResourcePaths, rhs.jobResourcePaths).append(jobClass, rhs.jobClass).append(parallelism, rhs.parallelism).append(failOnErrWritten, rhs.failOnErrWritten).append(executable, rhs.executable).append(timeout, rhs.timeout).isEquals();
    }

}
