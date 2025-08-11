
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.joc.model.common.IConfigurationObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    "subagentClusterIdExpr",
    "withSubagentClusterIdExpr",
    "executable",
    "admissionTimeScheme",
    "skipIfNoAdmissionForOrderDay",
    "killAtEndOfAdmissionPeriod",
    "returnCodeMeaning",
    "parallelism",
    "timeout",
    "graceTimeout",
    "failOnErrWritten",
    "warnOnErrWritten",
    "jobTemplate",
    "defaultArguments",
    "jobResourceNames",
    "title",
    "documentationName",
    "criticality",
    "warnIfShorter",
    "warnIfLonger",
    "notification",
    "isNotRestartable"
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
    @JsonProperty("subagentClusterIdExpr")
    @JsonAlias({
        "subagentSelectionIdExpr"
    })
    private String subagentClusterIdExpr;
    @JsonProperty("withSubagentClusterIdExpr")
    private Boolean withSubagentClusterIdExpr = false;
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
    @JsonAlias({
        "skipIfNoAdmissionStartForOrderDay"
    })
    private Boolean skipIfNoAdmissionForOrderDay = false;
    @JsonProperty("killAtEndOfAdmissionPeriod")
    private Boolean killAtEndOfAdmissionPeriod = false;
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
    private Integer graceTimeout = 1;
    @JsonProperty("failOnErrWritten")
    private Boolean failOnErrWritten = false;
    @JsonProperty("warnOnErrWritten")
    private Boolean warnOnErrWritten = false;
    /**
     * JobTemplateRef
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    private JobTemplateRef jobTemplate;
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
    @JsonProperty("isNotRestartable")
    private Boolean isNotRestartable = false;

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
     * @param subagentClusterIdExpr
     * @param failOnErrWritten
     * @param title
     * @param withSubagentClusterIdExpr
     * @param executable
     * @param timeout
     * @param warnIfShorter
     * @param admissionTimeScheme
     * @param returnCodeMeaning
     * @param notification
     * @param graceTimeout
     * @param defaultArguments
     * @param killAtEndOfAdmissionPeriod
     * @param skipIfNoAdmissionForOrderDay
     * @param subagentClusterId
     * @param documentationName
     * @param isNotRestartable
     * @param warnOnErrWritten
     */
    public Job(String agentName, String subagentClusterId, String subagentClusterIdExpr, Boolean withSubagentClusterIdExpr, Executable executable, AdmissionTimeScheme admissionTimeScheme, Boolean skipIfNoAdmissionForOrderDay, Boolean killAtEndOfAdmissionPeriod, JobReturnCode returnCodeMeaning, Integer parallelism, Integer timeout, Integer graceTimeout, Boolean failOnErrWritten, Boolean warnOnErrWritten, JobTemplateRef jobTemplate, Environment defaultArguments, List<String> jobResourceNames, String title, String documentationName, JobCriticality criticality, String warnIfShorter, String warnIfLonger, JobNotification notification, Boolean isNotRestartable) {
        super();
        this.agentName = agentName;
        this.subagentClusterId = subagentClusterId;
        this.subagentClusterIdExpr = subagentClusterIdExpr;
        this.withSubagentClusterIdExpr = withSubagentClusterIdExpr;
        this.executable = executable;
        this.admissionTimeScheme = admissionTimeScheme;
        this.skipIfNoAdmissionForOrderDay = skipIfNoAdmissionForOrderDay;
        this.killAtEndOfAdmissionPeriod = killAtEndOfAdmissionPeriod;
        this.returnCodeMeaning = returnCodeMeaning;
        this.parallelism = parallelism;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.failOnErrWritten = failOnErrWritten;
        this.warnOnErrWritten = warnOnErrWritten;
        this.jobTemplate = jobTemplate;
        this.defaultArguments = defaultArguments;
        this.jobResourceNames = jobResourceNames;
        this.title = title;
        this.documentationName = documentationName;
        this.criticality = criticality;
        this.warnIfShorter = warnIfShorter;
        this.warnIfLonger = warnIfLonger;
        this.notification = notification;
        this.isNotRestartable = isNotRestartable;
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

    @JsonProperty("subagentClusterIdExpr")
    public String getSubagentClusterIdExpr() {
        return subagentClusterIdExpr;
    }

    @JsonProperty("subagentClusterIdExpr")
    public void setSubagentClusterIdExpr(String subagentClusterIdExpr) {
        this.subagentClusterIdExpr = subagentClusterIdExpr;
    }

    @JsonProperty("withSubagentClusterIdExpr")
    public Boolean getWithSubagentClusterIdExpr() {
        return withSubagentClusterIdExpr;
    }

    @JsonProperty("withSubagentClusterIdExpr")
    public void setWithSubagentClusterIdExpr(Boolean withSubagentClusterIdExpr) {
        this.withSubagentClusterIdExpr = withSubagentClusterIdExpr;
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

    @JsonProperty("killAtEndOfAdmissionPeriod")
    public Boolean getKillAtEndOfAdmissionPeriod() {
        return killAtEndOfAdmissionPeriod;
    }

    @JsonProperty("killAtEndOfAdmissionPeriod")
    public void setKillAtEndOfAdmissionPeriod(Boolean killAtEndOfAdmissionPeriod) {
        this.killAtEndOfAdmissionPeriod = killAtEndOfAdmissionPeriod;
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

    @JsonProperty("warnOnErrWritten")
    public Boolean getWarnOnErrWritten() {
        return warnOnErrWritten;
    }

    @JsonProperty("warnOnErrWritten")
    public void setWarnOnErrWritten(Boolean warnOnErrWritten) {
        this.warnOnErrWritten = warnOnErrWritten;
    }

    /**
     * JobTemplateRef
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    public JobTemplateRef getJobTemplate() {
        return jobTemplate;
    }

    /**
     * JobTemplateRef
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    public void setJobTemplate(JobTemplateRef jobTemplate) {
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

    @JsonProperty("isNotRestartable")
    public Boolean getIsNotRestartable() {
        return isNotRestartable;
    }

    @JsonProperty("isNotRestartable")
    public void setIsNotRestartable(Boolean isNotRestartable) {
        this.isNotRestartable = isNotRestartable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentName", agentName).append("subagentClusterId", subagentClusterId).append("subagentClusterIdExpr", subagentClusterIdExpr).append("withSubagentClusterIdExpr", withSubagentClusterIdExpr).append("executable", executable).append("admissionTimeScheme", admissionTimeScheme).append("skipIfNoAdmissionForOrderDay", skipIfNoAdmissionForOrderDay).append("killAtEndOfAdmissionPeriod", killAtEndOfAdmissionPeriod).append("returnCodeMeaning", returnCodeMeaning).append("parallelism", parallelism).append("timeout", timeout).append("graceTimeout", graceTimeout).append("failOnErrWritten", failOnErrWritten).append("warnOnErrWritten", warnOnErrWritten).append("jobTemplate", jobTemplate).append("defaultArguments", defaultArguments).append("jobResourceNames", jobResourceNames).append("title", title).append("documentationName", documentationName).append("criticality", criticality).append("warnIfShorter", warnIfShorter).append("warnIfLonger", warnIfLonger).append("notification", notification).append("isNotRestartable", isNotRestartable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(warnIfLonger).append(jobTemplate).append(parallelism).append(criticality).append(failOnErrWritten).append(title).append(timeout).append(returnCodeMeaning).append(notification).append(graceTimeout).append(documentationName).append(jobResourceNames).append(agentName).append(subagentClusterIdExpr).append(withSubagentClusterIdExpr).append(executable).append(warnIfShorter).append(admissionTimeScheme).append(defaultArguments).append(killAtEndOfAdmissionPeriod).append(skipIfNoAdmissionForOrderDay).append(subagentClusterId).append(isNotRestartable).append(warnOnErrWritten).toHashCode();
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
        return new EqualsBuilder().append(warnIfLonger, rhs.warnIfLonger).append(jobTemplate, rhs.jobTemplate).append(parallelism, rhs.parallelism).append(criticality, rhs.criticality).append(failOnErrWritten, rhs.failOnErrWritten).append(title, rhs.title).append(timeout, rhs.timeout).append(returnCodeMeaning, rhs.returnCodeMeaning).append(notification, rhs.notification).append(graceTimeout, rhs.graceTimeout).append(documentationName, rhs.documentationName).append(jobResourceNames, rhs.jobResourceNames).append(agentName, rhs.agentName).append(subagentClusterIdExpr, rhs.subagentClusterIdExpr).append(withSubagentClusterIdExpr, rhs.withSubagentClusterIdExpr).append(executable, rhs.executable).append(warnIfShorter, rhs.warnIfShorter).append(admissionTimeScheme, rhs.admissionTimeScheme).append(defaultArguments, rhs.defaultArguments).append(killAtEndOfAdmissionPeriod, rhs.killAtEndOfAdmissionPeriod).append(skipIfNoAdmissionForOrderDay, rhs.skipIfNoAdmissionForOrderDay).append(subagentClusterId, rhs.subagentClusterId).append(isNotRestartable, rhs.isNotRestartable).append(warnOnErrWritten, rhs.warnOnErrWritten).isEquals();
    }

}
