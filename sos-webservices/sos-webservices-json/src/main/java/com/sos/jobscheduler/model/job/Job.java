
package com.sos.jobscheduler.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.inventory.common.JobCriticality;
import com.sos.joc.model.inventory.common.JobLogLevel;
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
    "agentName",
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
    "criticality",
    "path"
})
public class Job implements IConfigurationObject
{

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    private String agentName;
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
     * log levels
     * <p>
     * 
     * 
     */
    @JsonProperty("logLevel")
    private JobLogLevel logLevel;
    /**
     * criticalities
     * <p>
     * 
     * 
     */
    @JsonProperty("criticality")
    private JobCriticality criticality;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String path;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param taskLimit
     * @param documentationId
     * @param criticality
     * @param agentName
     * @param title
     * @param executable
     * @param timeout
     * @param returnCodeMeaning
     * @param path
     * @param graceTimeout
     * @param defaultArguments
     * @param logLevel
     * @param jobClass
     */
    public Job(String agentName, ExecutableScript executable, JobReturnCode returnCodeMeaning, Integer taskLimit, Integer timeout, Integer graceTimeout, String jobClass, Variables defaultArguments, String title, Long documentationId, JobLogLevel logLevel, JobCriticality criticality, String path) {
        super();
        this.agentName = agentName;
        this.executable = executable;
        this.returnCodeMeaning = returnCodeMeaning;
        this.taskLimit = taskLimit;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.jobClass = jobClass;
        this.defaultArguments = defaultArguments;
        this.title = title;
        this.documentationId = documentationId;
        this.logLevel = logLevel;
        this.criticality = criticality;
        this.path = path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
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
     * log levels
     * <p>
     * 
     * 
     */
    @JsonProperty("logLevel")
    public JobLogLevel getLogLevel() {
        return logLevel;
    }

    /**
     * log levels
     * <p>
     * 
     * 
     */
    @JsonProperty("logLevel")
    public void setLogLevel(JobLogLevel logLevel) {
        this.logLevel = logLevel;
    }

    /**
     * criticalities
     * <p>
     * 
     * 
     */
    @JsonProperty("criticality")
    public JobCriticality getCriticality() {
        return criticality;
    }

    /**
     * criticalities
     * <p>
     * 
     * 
     */
    @JsonProperty("criticality")
    public void setCriticality(JobCriticality criticality) {
        this.criticality = criticality;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentName", agentName).append("executable", executable).append("returnCodeMeaning", returnCodeMeaning).append("taskLimit", taskLimit).append("timeout", timeout).append("graceTimeout", graceTimeout).append("jobClass", jobClass).append("defaultArguments", defaultArguments).append("title", title).append("documentationId", documentationId).append("logLevel", logLevel).append("criticality", criticality).append("path", path).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(taskLimit).append(documentationId).append(criticality).append(agentName).append(title).append(executable).append(timeout).append(returnCodeMeaning).append(path).append(graceTimeout).append(defaultArguments).append(logLevel).append(jobClass).toHashCode();
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
        return new EqualsBuilder().append(taskLimit, rhs.taskLimit).append(documentationId, rhs.documentationId).append(criticality, rhs.criticality).append(agentName, rhs.agentName).append(title, rhs.title).append(executable, rhs.executable).append(timeout, rhs.timeout).append(returnCodeMeaning, rhs.returnCodeMeaning).append(path, rhs.path).append(graceTimeout, rhs.graceTimeout).append(defaultArguments, rhs.defaultArguments).append(logLevel, rhs.logLevel).append(jobClass, rhs.jobClass).isEquals();
    }

}
