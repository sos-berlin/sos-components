
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.joc.model.common.IConfigurationObject;
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
    "agentName",
    "subagentClusterId",
    "executable",
    "admissionTimeScheme",
    "skipIfNoAdmissionForOrderDay",
    "returnCodeMeaning",
    "parallelism",
    "timeout",
    "graceTimeout",
    "failOnErrWritten",
    "jobClassName",
    "jobTemplate",
    "defaultArguments",
    "jobResourceNames",
    "title",
    "documentationName",
    "criticality",
    "warnIfShorter",
    "warnIfLonger",
    "notification"
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
    @JsonProperty("subagentClusterId")
    @JsonAlias({
        "subagentSelectionId"
    })
    private String subagentClusterId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("executable")
    private Executable executable;
    /**
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    private AdmissionTimeScheme admissionTimeScheme;
    @JsonProperty("skipIfNoAdmissionForOrderDay")
    private Boolean skipIfNoAdmissionForOrderDay = false;
    /**
     * job return code meaning
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
    @JsonProperty("graceTimeout")
    @JsonPropertyDescription("in seconds")
    @JsonAlias({
        "sigkillDelay"
    })
    private Integer graceTimeout = 15;
    @JsonProperty("failOnErrWritten")
    private Boolean failOnErrWritten = false;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobClassName")
    private String jobClassName;
    @JsonProperty("jobTemplate")
    private JobTemplate jobTemplate;
    /**
     * key-value pairs particularly to assign parameters to environment
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("defaultArguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Environment defaultArguments;
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
     * criticalities
     * <p>
     * 
     * 
     */
    @JsonProperty("criticality")
    private JobCriticality criticality;
    /**
     * HH:MM:SS|seconds|percentage%
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfShorter")
    private String warnIfShorter;
    /**
     * HH:MM:SS|seconds|percentage%
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfLonger")
    private String warnIfLonger;
    /**
     * job notification
     * <p>
     * 
     * 
     */
    @JsonProperty("notification")
    private JobNotification notification;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Job() {
    }

    /**
     * 
     * @param warnIfLonger
     * @param jobTemplate
     * @param parallelism
     * @param jobResourceNames
     * @param criticality
     * @param agentName
     * @param failOnErrWritten
     * @param title
     * @param executable
     * @param timeout
     * @param warnIfShorter
     * @param admissionTimeScheme
     * @param returnCodeMeaning
     * @param notification
     * @param graceTimeout
     * @param defaultArguments
     * @param jobClassName
     * @param skipIfNoAdmissionForOrderDay
     * @param subagentClusterId
     * @param documentationName
     */
    public Job(String agentName, String subagentClusterId, Executable executable, AdmissionTimeScheme admissionTimeScheme, Boolean skipIfNoAdmissionForOrderDay, JobReturnCode returnCodeMeaning, Integer parallelism, Integer timeout, Integer graceTimeout, Boolean failOnErrWritten, String jobClassName, JobTemplate jobTemplate, Environment defaultArguments, List<String> jobResourceNames, String title, String documentationName, JobCriticality criticality, String warnIfShorter, String warnIfLonger, JobNotification notification) {
        super();
        this.agentName = agentName;
        this.subagentClusterId = subagentClusterId;
        this.executable = executable;
        this.admissionTimeScheme = admissionTimeScheme;
        this.skipIfNoAdmissionForOrderDay = skipIfNoAdmissionForOrderDay;
        this.returnCodeMeaning = returnCodeMeaning;
        this.parallelism = parallelism;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.failOnErrWritten = failOnErrWritten;
        this.jobClassName = jobClassName;
        this.jobTemplate = jobTemplate;
        this.defaultArguments = defaultArguments;
        this.jobResourceNames = jobResourceNames;
        this.title = title;
        this.documentationName = documentationName;
        this.criticality = criticality;
        this.warnIfShorter = warnIfShorter;
        this.warnIfLonger = warnIfLonger;
        this.notification = notification;
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

    @JsonProperty("subagentClusterId")
    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    @JsonProperty("subagentClusterId")
    public void setSubagentClusterId(String subagentClusterId) {
        this.subagentClusterId = subagentClusterId;
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
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    public AdmissionTimeScheme getAdmissionTimeScheme() {
        return admissionTimeScheme;
    }

    /**
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    public void setAdmissionTimeScheme(AdmissionTimeScheme admissionTimeScheme) {
        this.admissionTimeScheme = admissionTimeScheme;
    }

    @JsonProperty("skipIfNoAdmissionForOrderDay")
    public Boolean getSkipIfNoAdmissionForOrderDay() {
        return skipIfNoAdmissionForOrderDay;
    }

    @JsonProperty("skipIfNoAdmissionForOrderDay")
    public void setSkipIfNoAdmissionForOrderDay(Boolean skipIfNoAdmissionForOrderDay) {
        this.skipIfNoAdmissionForOrderDay = skipIfNoAdmissionForOrderDay;
    }

    /**
     * job return code meaning
     * <p>
     * 
     * 
     */
    @JsonProperty("returnCodeMeaning")
    public JobReturnCode getReturnCodeMeaning() {
        return returnCodeMeaning;
    }

    /**
     * job return code meaning
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
    @JsonProperty("graceTimeout")
    public Integer getGraceTimeout() {
        return graceTimeout;
    }

    /**
     * in seconds
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobClassName")
    public String getJobClassName() {
        return jobClassName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobClassName")
    public void setJobClassName(String jobClassName) {
        this.jobClassName = jobClassName;
    }

    @JsonProperty("jobTemplate")
    public JobTemplate getJobTemplate() {
        return jobTemplate;
    }

    @JsonProperty("jobTemplate")
    public void setJobTemplate(JobTemplate jobTemplate) {
        this.jobTemplate = jobTemplate;
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
     * HH:MM:SS|seconds|percentage%
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfShorter")
    public String getWarnIfShorter() {
        return warnIfShorter;
    }

    /**
     * HH:MM:SS|seconds|percentage%
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfShorter")
    public void setWarnIfShorter(String warnIfShorter) {
        this.warnIfShorter = warnIfShorter;
    }

    /**
     * HH:MM:SS|seconds|percentage%
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfLonger")
    public String getWarnIfLonger() {
        return warnIfLonger;
    }

    /**
     * HH:MM:SS|seconds|percentage%
     * <p>
     * 
     * 
     */
    @JsonProperty("warnIfLonger")
    public void setWarnIfLonger(String warnIfLonger) {
        this.warnIfLonger = warnIfLonger;
    }

    /**
     * job notification
     * <p>
     * 
     * 
     */
    @JsonProperty("notification")
    public JobNotification getNotification() {
        return notification;
    }

    /**
     * job notification
     * <p>
     * 
     * 
     */
    @JsonProperty("notification")
    public void setNotification(JobNotification notification) {
        this.notification = notification;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentName", agentName).append("subagentClusterId", subagentClusterId).append("executable", executable).append("admissionTimeScheme", admissionTimeScheme).append("skipIfNoAdmissionForOrderDay", skipIfNoAdmissionForOrderDay).append("returnCodeMeaning", returnCodeMeaning).append("parallelism", parallelism).append("timeout", timeout).append("graceTimeout", graceTimeout).append("failOnErrWritten", failOnErrWritten).append("jobClassName", jobClassName).append("jobTemplate", jobTemplate).append("defaultArguments", defaultArguments).append("jobResourceNames", jobResourceNames).append("title", title).append("documentationName", documentationName).append("criticality", criticality).append("warnIfShorter", warnIfShorter).append("warnIfLonger", warnIfLonger).append("notification", notification).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(warnIfLonger).append(jobTemplate).append(parallelism).append(jobResourceNames).append(criticality).append(agentName).append(failOnErrWritten).append(title).append(executable).append(timeout).append(warnIfShorter).append(admissionTimeScheme).append(returnCodeMeaning).append(notification).append(graceTimeout).append(defaultArguments).append(jobClassName).append(skipIfNoAdmissionForOrderDay).append(subagentClusterId).append(documentationName).toHashCode();
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
        return new EqualsBuilder().append(warnIfLonger, rhs.warnIfLonger).append(jobTemplate, rhs.jobTemplate).append(parallelism, rhs.parallelism).append(jobResourceNames, rhs.jobResourceNames).append(criticality, rhs.criticality).append(agentName, rhs.agentName).append(failOnErrWritten, rhs.failOnErrWritten).append(title, rhs.title).append(executable, rhs.executable).append(timeout, rhs.timeout).append(warnIfShorter, rhs.warnIfShorter).append(admissionTimeScheme, rhs.admissionTimeScheme).append(returnCodeMeaning, rhs.returnCodeMeaning).append(notification, rhs.notification).append(graceTimeout, rhs.graceTimeout).append(defaultArguments, rhs.defaultArguments).append(jobClassName, rhs.jobClassName).append(skipIfNoAdmissionForOrderDay, rhs.skipIfNoAdmissionForOrderDay).append(subagentClusterId, rhs.subagentClusterId).append(documentationName, rhs.documentationName).isEquals();
    }

}
