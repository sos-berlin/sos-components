
package com.sos.inventory.model.job;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.Variables;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.model.common.IConfigurationObject;
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
    "v1Compatible",
    "timeout",
    "graceTimeout",
    "jobClass",
    "jobRequirements",
    "defaultArguments",
    "title",
    "documentationPath",
    "logLevel",
    "criticality"
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
    @JsonProperty("v1Compatible")
    private Boolean v1Compatible = true;
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
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("jobRequirements")
    private Requirements jobRequirements;
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
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    @JsonPropertyDescription("absolute path of an object.")
    private String documentationPath;
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
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param documentationPath
     * @param taskLimit
     * @param agentId
     * @param jobRequirements
     * @param criticality
     * @param title
     * @param executable
     * @param v1Compatible
     * @param timeout
     * @param returnCodeMeaning
     * @param graceTimeout
     * @param defaultArguments
     * @param logLevel
     * @param jobClass
     */
    public Job(String agentId, ExecutableScript executable, JobReturnCode returnCodeMeaning, Integer taskLimit, Boolean v1Compatible, Integer timeout, Integer graceTimeout, String jobClass, Requirements jobRequirements, Variables defaultArguments, String title, String documentationPath, JobLogLevel logLevel, JobCriticality criticality) {
        super();
        this.agentId = agentId;
        this.executable = executable;
        this.returnCodeMeaning = returnCodeMeaning;
        this.taskLimit = taskLimit;
        this.v1Compatible = v1Compatible;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.jobClass = jobClass;
        this.jobRequirements = jobRequirements;
        this.defaultArguments = defaultArguments;
        this.title = title;
        this.documentationPath = documentationPath;
        this.logLevel = logLevel;
        this.criticality = criticality;
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

    @JsonProperty("v1Compatible")
    public Boolean getV1Compatible() {
        return v1Compatible;
    }

    @JsonProperty("v1Compatible")
    public void setV1Compatible(Boolean v1Compatible) {
        this.v1Compatible = v1Compatible;
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
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("jobRequirements")
    public Requirements getJobRequirements() {
        return jobRequirements;
    }

    /**
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("jobRequirements")
    public void setJobRequirements(Requirements jobRequirements) {
        this.jobRequirements = jobRequirements;
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
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    public String getDocumentationPath() {
        return documentationPath;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("documentationPath")
    public void setDocumentationPath(String documentationPath) {
        this.documentationPath = documentationPath;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentId", agentId).append("executable", executable).append("returnCodeMeaning", returnCodeMeaning).append("taskLimit", taskLimit).append("v1Compatible", v1Compatible).append("timeout", timeout).append("graceTimeout", graceTimeout).append("jobClass", jobClass).append("jobRequirements", jobRequirements).append("defaultArguments", defaultArguments).append("title", title).append("documentationPath", documentationPath).append("logLevel", logLevel).append("criticality", criticality).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(documentationPath).append(taskLimit).append(agentId).append(jobRequirements).append(criticality).append(title).append(executable).append(v1Compatible).append(timeout).append(returnCodeMeaning).append(graceTimeout).append(defaultArguments).append(logLevel).append(jobClass).toHashCode();
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
        return new EqualsBuilder().append(documentationPath, rhs.documentationPath).append(taskLimit, rhs.taskLimit).append(agentId, rhs.agentId).append(jobRequirements, rhs.jobRequirements).append(criticality, rhs.criticality).append(title, rhs.title).append(executable, rhs.executable).append(v1Compatible, rhs.v1Compatible).append(timeout, rhs.timeout).append(returnCodeMeaning, rhs.returnCodeMeaning).append(graceTimeout, rhs.graceTimeout).append(defaultArguments, rhs.defaultArguments).append(logLevel, rhs.logLevel).append(jobClass, rhs.jobClass).isEquals();
    }

}
