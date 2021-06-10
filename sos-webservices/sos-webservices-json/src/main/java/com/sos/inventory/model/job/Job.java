
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
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
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "agentName",
    "executable",
    "returnCodeMeaning",
    "taskLimit",
    "timeout",
    "graceTimeout",
    "failOnErrWritten",
    "jobClass",
    "defaultArguments",
    "jobResourceNames",
    "title",
    "documentationName",
    "logLevel",
    "criticality",
    "warnIfShorter",
    "warnIfLonger"
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    private String documentationName;
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
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfShorter")
    private Integer warnIfShorter;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfLonger")
    private Integer warnIfLonger;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param taskLimit
     * @param warnIfLonger
     * @param jobResourceNames
     * @param criticality
     * @param agentName
     * @param failOnErrWritten
     * @param title
     * @param executable
     * @param timeout
     * @param warnIfShorter
     * @param returnCodeMeaning
     * @param graceTimeout
     * @param defaultArguments
     * @param logLevel
     * @param jobClass
     * @param documentationName
     */
    public Job(String agentName, Executable executable, JobReturnCode returnCodeMeaning, Integer taskLimit, Integer timeout, Integer graceTimeout, Boolean failOnErrWritten, String jobClass, Variables defaultArguments, List<String> jobResourceNames, String title, String documentationName, JobLogLevel logLevel, JobCriticality criticality, Integer warnIfShorter, Integer warnIfLonger) {
        super();
        this.agentName = agentName;
        this.executable = executable;
        this.returnCodeMeaning = returnCodeMeaning;
        this.taskLimit = taskLimit;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.failOnErrWritten = failOnErrWritten;
        this.jobClass = jobClass;
        this.defaultArguments = defaultArguments;
        this.jobResourceNames = jobResourceNames;
        this.title = title;
        this.documentationName = documentationName;
        this.logLevel = logLevel;
        this.criticality = criticality;
        this.warnIfShorter = warnIfShorter;
        this.warnIfLonger = warnIfLonger;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public String getDocumentationName() {
        return documentationName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("documentationName")
    public void setDocumentationName(String documentationName) {
        this.documentationName = documentationName;
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
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfShorter")
    public Integer getWarnIfShorter() {
        return warnIfShorter;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfShorter")
    public void setWarnIfShorter(Integer warnIfShorter) {
        this.warnIfShorter = warnIfShorter;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfLonger")
    public Integer getWarnIfLonger() {
        return warnIfLonger;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfLonger")
    public void setWarnIfLonger(Integer warnIfLonger) {
        this.warnIfLonger = warnIfLonger;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentName", agentName).append("executable", executable).append("returnCodeMeaning", returnCodeMeaning).append("taskLimit", taskLimit).append("timeout", timeout).append("graceTimeout", graceTimeout).append("failOnErrWritten", failOnErrWritten).append("jobClass", jobClass).append("defaultArguments", defaultArguments).append("jobResourceNames", jobResourceNames).append("title", title).append("documentationName", documentationName).append("logLevel", logLevel).append("criticality", criticality).append("warnIfShorter", warnIfShorter).append("warnIfLonger", warnIfLonger).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(taskLimit).append(warnIfLonger).append(jobResourceNames).append(criticality).append(agentName).append(failOnErrWritten).append(title).append(executable).append(timeout).append(warnIfShorter).append(returnCodeMeaning).append(graceTimeout).append(defaultArguments).append(logLevel).append(jobClass).append(documentationName).toHashCode();
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
        return new EqualsBuilder().append(taskLimit, rhs.taskLimit).append(warnIfLonger, rhs.warnIfLonger).append(jobResourceNames, rhs.jobResourceNames).append(criticality, rhs.criticality).append(agentName, rhs.agentName).append(failOnErrWritten, rhs.failOnErrWritten).append(title, rhs.title).append(executable, rhs.executable).append(timeout, rhs.timeout).append(warnIfShorter, rhs.warnIfShorter).append(returnCodeMeaning, rhs.returnCodeMeaning).append(graceTimeout, rhs.graceTimeout).append(defaultArguments, rhs.defaultArguments).append(logLevel, rhs.logLevel).append(jobClass, rhs.jobClass).append(documentationName, rhs.documentationName).isEquals();
    }

}
