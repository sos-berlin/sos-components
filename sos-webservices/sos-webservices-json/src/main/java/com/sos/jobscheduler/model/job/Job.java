
package com.sos.jobscheduler.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.joc.model.common.IJSObject;
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
    "timeout",
    "graceTimeout",
    "jobClass",
    "defaultArguments",
    "title",
    "documentationId",
    "logLevel",
    "criticality"
})
public class Job implements IJSObject
{

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("agentRefPath")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String agentRefPath;
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
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobClass")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    private String title;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationId")
    private Long documentationId;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("logLevel")
    private Integer logLevel;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("criticality")
    private Integer criticality;

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
     * @param graceTimeout
     * @param defaultArguments
     * @param jobClass
     * @param agentRefPath
     * @param executable
     * @param timeout
     */
    public Job(String agentRefPath, ExecutableScript executable, JobReturnCode returnCodeMeaning, Integer taskLimit, Integer timeout, Integer graceTimeout, String jobClass, Variables defaultArguments) {
        super();
        this.agentRefPath = agentRefPath;
        this.executable = executable;
        this.returnCodeMeaning = returnCodeMeaning;
        this.taskLimit = taskLimit;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.jobClass = jobClass;
        this.defaultArguments = defaultArguments;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
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
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("agentRefPath")
    public void setAgentRefPath(String agentRefPath) {
        this.agentRefPath = agentRefPath;
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

    @JsonProperty("taskLimit")
    public Integer getTaskLimit() {
        return taskLimit;
    }

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
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobClass")
    public String getJobClass() {
        return jobClass;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public String getTitle() {
        return title;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("title")
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationId")
    public Long getDocumentationId() {
        return documentationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationId")
    public void setDocumentationId(Long documentationId) {
        this.documentationId = documentationId;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("logLevel")
    public Integer getLogLevel() {
        return logLevel;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("logLevel")
    public void setLogLevel(Integer logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("criticality")
    public Integer getCriticality() {
        return criticality;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("criticality")
    public void setCriticality(Integer criticality) {
        this.criticality = criticality;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentRefPath", agentRefPath).append("executable", executable).append("returnCodeMeaning", returnCodeMeaning).append("taskLimit", taskLimit).append("timeout", timeout).append("graceTimeout", graceTimeout).append("jobClass", jobClass).append("defaultArguments", defaultArguments).append("title", title).append("documentationId", documentationId).append("logLevel", logLevel).append("criticality", criticality).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(taskLimit).append(documentationId).append(criticality).append(agentRefPath).append(title).append(executable).append(timeout).append(returnCodeMeaning).append(graceTimeout).append(defaultArguments).append(logLevel).append(jobClass).toHashCode();
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
        return new EqualsBuilder().append(taskLimit, rhs.taskLimit).append(documentationId, rhs.documentationId).append(criticality, rhs.criticality).append(agentRefPath, rhs.agentRefPath).append(title, rhs.title).append(executable, rhs.executable).append(timeout, rhs.timeout).append(returnCodeMeaning, rhs.returnCodeMeaning).append(graceTimeout, rhs.graceTimeout).append(defaultArguments, rhs.defaultArguments).append(logLevel, rhs.logLevel).append(jobClass, rhs.jobClass).isEquals();
    }

}
