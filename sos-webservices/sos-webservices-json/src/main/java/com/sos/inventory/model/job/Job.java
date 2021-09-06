
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
    "executable",
    "admissionTimeScheme",
    "returnCodeMeaning",
    "parallelism",
    "timeout",
    "graceTimeout",
    "failOnErrWritten",
    "jobClass",
    "defaultArguments",
    "jobResourceNames",
    "title",
    "documentationName",
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
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    private AdmissionTimeScheme admissionTimeScheme;
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
    @JsonProperty("graceTimeout")
    @JsonPropertyDescription("in seconds")
    @JsonAlias({
        "sigkillDelay"
    })
    private Integer graceTimeout = 15;
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
     * @param warnIfLonger
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
     * @param graceTimeout
     * @param defaultArguments
     * @param jobClass
     * @param documentationName
     */
    public Job(String agentName, Executable executable, AdmissionTimeScheme admissionTimeScheme, JobReturnCode returnCodeMeaning, Integer parallelism, Integer timeout, Integer graceTimeout, Boolean failOnErrWritten, String jobClass, Environment defaultArguments, List<String> jobResourceNames, String title, String documentationName, JobCriticality criticality, Integer warnIfShorter, Integer warnIfLonger) {
        super();
        this.agentName = agentName;
        this.executable = executable;
        this.admissionTimeScheme = admissionTimeScheme;
        this.returnCodeMeaning = returnCodeMeaning;
        this.parallelism = parallelism;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.failOnErrWritten = failOnErrWritten;
        this.jobClass = jobClass;
        this.defaultArguments = defaultArguments;
        this.jobResourceNames = jobResourceNames;
        this.title = title;
        this.documentationName = documentationName;
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
        return new ToStringBuilder(this).append("agentName", agentName).append("executable", executable).append("admissionTimeScheme", admissionTimeScheme).append("returnCodeMeaning", returnCodeMeaning).append("parallelism", parallelism).append("timeout", timeout).append("graceTimeout", graceTimeout).append("failOnErrWritten", failOnErrWritten).append("jobClass", jobClass).append("defaultArguments", defaultArguments).append("jobResourceNames", jobResourceNames).append("title", title).append("documentationName", documentationName).append("criticality", criticality).append("warnIfShorter", warnIfShorter).append("warnIfLonger", warnIfLonger).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(warnIfLonger).append(parallelism).append(jobResourceNames).append(criticality).append(agentName).append(failOnErrWritten).append(title).append(executable).append(timeout).append(warnIfShorter).append(admissionTimeScheme).append(returnCodeMeaning).append(graceTimeout).append(defaultArguments).append(jobClass).append(documentationName).toHashCode();
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
        return new EqualsBuilder().append(warnIfLonger, rhs.warnIfLonger).append(parallelism, rhs.parallelism).append(jobResourceNames, rhs.jobResourceNames).append(criticality, rhs.criticality).append(agentName, rhs.agentName).append(failOnErrWritten, rhs.failOnErrWritten).append(title, rhs.title).append(executable, rhs.executable).append(timeout, rhs.timeout).append(warnIfShorter, rhs.warnIfShorter).append(admissionTimeScheme, rhs.admissionTimeScheme).append(returnCodeMeaning, rhs.returnCodeMeaning).append(graceTimeout, rhs.graceTimeout).append(defaultArguments, rhs.defaultArguments).append(jobClass, rhs.jobClass).append(documentationName, rhs.documentationName).isEquals();
    }

}
