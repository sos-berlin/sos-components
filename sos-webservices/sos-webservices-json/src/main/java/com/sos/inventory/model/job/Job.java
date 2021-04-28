
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.Variables;
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
    "agentName",
    "executable",
    "returnCodeMeaning",
    "taskLimit",
    "timeout",
    "graceTimeout",
    "jobClass",
    "defaultArguments",
    "jobResourceNames",
    "title",
    "documentationPath",
    "logLevel",
    "criticality"
})
public class Job implements IConfigurationObject
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    @JsonAlias({
        "agentId",
        "agentPath"
    })
    private String agentName;
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
    @JsonProperty("jobResourceNames")
    @JsonAlias({
        "jobResourcePaths"
    })
    private List<String> jobResourceNames = null;
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
     * @param jobResourceNames
     * @param criticality
     * @param agentName
     * @param title
     * @param executable
     * @param timeout
     * @param returnCodeMeaning
     * @param graceTimeout
     * @param defaultArguments
     * @param logLevel
     * @param jobClass
     */
    public Job(String agentName, Executable executable, JobReturnCode returnCodeMeaning, Integer taskLimit, Integer timeout, Integer graceTimeout, String jobClass, Variables defaultArguments, List<String> jobResourceNames, String title, String documentationPath, JobLogLevel logLevel, JobCriticality criticality) {
        super();
        this.agentName = agentName;
        this.executable = executable;
        this.returnCodeMeaning = returnCodeMeaning;
        this.taskLimit = taskLimit;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.jobClass = jobClass;
        this.defaultArguments = defaultArguments;
        this.jobResourceNames = jobResourceNames;
        this.title = title;
        this.documentationPath = documentationPath;
        this.logLevel = logLevel;
        this.criticality = criticality;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public String getAgentName() {
        return agentName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
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

    @JsonProperty("jobResourceNames")
    public List<String> getJobResourceNames() {
        return jobResourceNames;
    }

    @JsonProperty("jobResourceNames")
    public void setJobResourceNames(List<String> jobResourceNames) {
        this.jobResourceNames = jobResourceNames;
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
        return new ToStringBuilder(this).append("agentName", agentName).append("executable", executable).append("returnCodeMeaning", returnCodeMeaning).append("taskLimit", taskLimit).append("timeout", timeout).append("graceTimeout", graceTimeout).append("jobClass", jobClass).append("defaultArguments", defaultArguments).append("jobResourceNames", jobResourceNames).append("title", title).append("documentationPath", documentationPath).append("logLevel", logLevel).append("criticality", criticality).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(documentationPath).append(taskLimit).append(jobResourceNames).append(criticality).append(agentName).append(title).append(executable).append(timeout).append(returnCodeMeaning).append(graceTimeout).append(defaultArguments).append(logLevel).append(jobClass).toHashCode();
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
        return new EqualsBuilder().append(documentationPath, rhs.documentationPath).append(taskLimit, rhs.taskLimit).append(jobResourceNames, rhs.jobResourceNames).append(criticality, rhs.criticality).append(agentName, rhs.agentName).append(title, rhs.title).append(executable, rhs.executable).append(timeout, rhs.timeout).append(returnCodeMeaning, rhs.returnCodeMeaning).append(graceTimeout, rhs.graceTimeout).append(defaultArguments, rhs.defaultArguments).append(logLevel, rhs.logLevel).append(jobClass, rhs.jobClass).isEquals();
    }

}
