
package com.sos.inventory.model.jobtemplate;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.IInventoryObject;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import com.sos.inventory.model.job.Executable;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.joc.model.common.IConfigurationObject;
import com.sos.joc.model.common.IReleaseObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobTemplate
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "version",
    "executable",
    "admissionTimeScheme",
    "skipIfNoAdmissionForOrderDay",
    "parallelism",
    "timeout",
    "graceTimeout",
    "failOnErrWritten",
    "parameters",
    "jobResourceNames",
    "title",
    "description",
    "documentationName",
    "criticality",
    "warnIfShorter",
    "warnIfLonger",
    "notification"
})
public class JobTemplate implements IInventoryObject, IConfigurationObject, IReleaseObject
{

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    @JsonPropertyDescription("inventory repository version")
    private String version = "1.5.0";
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
    @JsonProperty("parallelism")
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
    private Integer graceTimeout = 15;
    @JsonProperty("failOnErrWritten")
    private Boolean failOnErrWritten = false;
    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("parameters")
    private Parameters parameters;
    @JsonProperty("jobResourceNames")
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
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("description")
    private String description;
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
    public JobTemplate() {
    }

    /**
     * 
     * @param warnIfLonger
     * @param parallelism
     * @param jobResourceNames
     * @param criticality
     * @param failOnErrWritten
     * @param description
     * @param title
     * @param version
     * @param executable
     * @param timeout
     * @param warnIfShorter
     * @param admissionTimeScheme
     * @param notification
     * @param graceTimeout
     * @param skipIfNoAdmissionForOrderDay
     * @param documentationName
     * @param parameters
     */
    public JobTemplate(String version, Executable executable, AdmissionTimeScheme admissionTimeScheme, Boolean skipIfNoAdmissionForOrderDay, Integer parallelism, Integer timeout, Integer graceTimeout, Boolean failOnErrWritten, Parameters parameters, List<String> jobResourceNames, String title, String description, String documentationName, JobCriticality criticality, String warnIfShorter, String warnIfLonger, JobNotification notification) {
        super();
        this.version = version;
        this.executable = executable;
        this.admissionTimeScheme = admissionTimeScheme;
        this.skipIfNoAdmissionForOrderDay = skipIfNoAdmissionForOrderDay;
        this.parallelism = parallelism;
        this.timeout = timeout;
        this.graceTimeout = graceTimeout;
        this.failOnErrWritten = failOnErrWritten;
        this.parameters = parameters;
        this.jobResourceNames = jobResourceNames;
        this.title = title;
        this.description = description;
        this.documentationName = documentationName;
        this.criticality = criticality;
        this.warnIfShorter = warnIfShorter;
        this.warnIfLonger = warnIfLonger;
        this.notification = notification;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * inventory repository version
     * <p>
     * inventory repository version
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
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
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("parameters")
    public Parameters getParameters() {
        return parameters;
    }

    /**
     * parameters
     * <p>
     * 
     * 
     */
    @JsonProperty("parameters")
    public void setParameters(Parameters parameters) {
        this.parameters = parameters;
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
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    /**
     * disallow <script and <svg/on
     * <p>
     * 
     * 
     */
    @JsonProperty("description")
    public void setDescription(String description) {
        this.description = description;
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
        return new ToStringBuilder(this).append("version", version).append("executable", executable).append("admissionTimeScheme", admissionTimeScheme).append("skipIfNoAdmissionForOrderDay", skipIfNoAdmissionForOrderDay).append("parallelism", parallelism).append("timeout", timeout).append("graceTimeout", graceTimeout).append("failOnErrWritten", failOnErrWritten).append("parameters", parameters).append("jobResourceNames", jobResourceNames).append("title", title).append("description", description).append("documentationName", documentationName).append("criticality", criticality).append("warnIfShorter", warnIfShorter).append("warnIfLonger", warnIfLonger).append("notification", notification).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(warnIfLonger).append(parallelism).append(jobResourceNames).append(criticality).append(failOnErrWritten).append(description).append(title).append(version).append(executable).append(timeout).append(warnIfShorter).append(admissionTimeScheme).append(notification).append(graceTimeout).append(skipIfNoAdmissionForOrderDay).append(documentationName).append(parameters).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobTemplate) == false) {
            return false;
        }
        JobTemplate rhs = ((JobTemplate) other);
        return new EqualsBuilder().append(warnIfLonger, rhs.warnIfLonger).append(parallelism, rhs.parallelism).append(jobResourceNames, rhs.jobResourceNames).append(criticality, rhs.criticality).append(failOnErrWritten, rhs.failOnErrWritten).append(description, rhs.description).append(title, rhs.title).append(version, rhs.version).append(executable, rhs.executable).append(timeout, rhs.timeout).append(warnIfShorter, rhs.warnIfShorter).append(admissionTimeScheme, rhs.admissionTimeScheme).append(notification, rhs.notification).append(graceTimeout, rhs.graceTimeout).append(skipIfNoAdmissionForOrderDay, rhs.skipIfNoAdmissionForOrderDay).append(documentationName, rhs.documentationName).append(parameters, rhs.parameters).isEquals();
    }

}
