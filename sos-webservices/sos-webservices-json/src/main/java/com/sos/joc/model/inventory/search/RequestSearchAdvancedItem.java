
package com.sos.joc.model.inventory.search;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.JobCriticality;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Inventory advanced search
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "agentName",
    "workflow",
    "fileOrderSource",
    "lock",
    "schedule",
    "includeScript",
    "calendar",
    "noticeBoard",
    "jobResource",
    "jobTemplate",
    "jobName",
    "jobNameExactMatch",
    "jobCriticality",
    "jobCountFrom",
    "jobCountTo",
    "jobScript",
    "argumentName",
    "argumentValue",
    "envName",
    "envValue"
})
public class RequestSearchAdvancedItem {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("agentName")
    private String agentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    private String workflow;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("fileOrderSource")
    private String fileOrderSource;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lock")
    private String lock;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schedule")
    private String schedule;
    @JsonProperty("includeScript")
    @JsonAlias({
        "includeScripts"
    })
    private String includeScript;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
    private String calendar;
    @JsonProperty("noticeBoard")
    @JsonAlias({
        "noticeBoards"
    })
    private String noticeBoard;
    @JsonProperty("jobResource")
    @JsonAlias({
        "jobResources"
    })
    private String jobResource;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    private String jobTemplate;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    private String jobName;
    @JsonProperty("jobNameExactMatch")
    private Boolean jobNameExactMatch = false;
    /**
     * criticalities
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCriticality")
    private JobCriticality jobCriticality;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCountFrom")
    private Integer jobCountFrom;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCountTo")
    private Integer jobCountTo;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobScript")
    private String jobScript;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("argumentName")
    private String argumentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("argumentValue")
    private String argumentValue;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("envName")
    private String envName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("envValue")
    private String envValue;

    /**
     * string without < and >
     * <p>
     * 
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
     * 
     */
    @JsonProperty("agentName")
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    public String getWorkflow() {
        return workflow;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("fileOrderSource")
    public String getFileOrderSource() {
        return fileOrderSource;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("fileOrderSource")
    public void setFileOrderSource(String fileOrderSource) {
        this.fileOrderSource = fileOrderSource;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lock")
    public String getLock() {
        return lock;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("lock")
    public void setLock(String lock) {
        this.lock = lock;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schedule")
    public String getSchedule() {
        return schedule;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("schedule")
    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    @JsonProperty("includeScript")
    public String getIncludeScript() {
        return includeScript;
    }

    @JsonProperty("includeScript")
    public void setIncludeScript(String includeScript) {
        this.includeScript = includeScript;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
    public String getCalendar() {
        return calendar;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("calendar")
    public void setCalendar(String calendar) {
        this.calendar = calendar;
    }

    @JsonProperty("noticeBoard")
    public String getNoticeBoard() {
        return noticeBoard;
    }

    @JsonProperty("noticeBoard")
    public void setNoticeBoard(String noticeBoard) {
        this.noticeBoard = noticeBoard;
    }

    @JsonProperty("jobResource")
    public String getJobResource() {
        return jobResource;
    }

    @JsonProperty("jobResource")
    public void setJobResource(String jobResource) {
        this.jobResource = jobResource;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    public String getJobTemplate() {
        return jobTemplate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobTemplate")
    public void setJobTemplate(String jobTemplate) {
        this.jobTemplate = jobTemplate;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    public String getJobName() {
        return jobName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    @JsonProperty("jobNameExactMatch")
    public Boolean getJobNameExactMatch() {
        return jobNameExactMatch;
    }

    @JsonProperty("jobNameExactMatch")
    public void setJobNameExactMatch(Boolean jobNameExactMatch) {
        this.jobNameExactMatch = jobNameExactMatch;
    }

    /**
     * criticalities
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCriticality")
    public JobCriticality getJobCriticality() {
        return jobCriticality;
    }

    /**
     * criticalities
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCriticality")
    public void setJobCriticality(JobCriticality jobCriticality) {
        this.jobCriticality = jobCriticality;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCountFrom")
    public Integer getJobCountFrom() {
        return jobCountFrom;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCountFrom")
    public void setJobCountFrom(Integer jobCountFrom) {
        this.jobCountFrom = jobCountFrom;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCountTo")
    public Integer getJobCountTo() {
        return jobCountTo;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("jobCountTo")
    public void setJobCountTo(Integer jobCountTo) {
        this.jobCountTo = jobCountTo;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobScript")
    public String getJobScript() {
        return jobScript;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobScript")
    public void setJobScript(String jobScript) {
        this.jobScript = jobScript;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("argumentName")
    public String getArgumentName() {
        return argumentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("argumentName")
    public void setArgumentName(String argumentName) {
        this.argumentName = argumentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("argumentValue")
    public String getArgumentValue() {
        return argumentValue;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("argumentValue")
    public void setArgumentValue(String argumentValue) {
        this.argumentValue = argumentValue;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("envName")
    public String getEnvName() {
        return envName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("envName")
    public void setEnvName(String envName) {
        this.envName = envName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("envValue")
    public String getEnvValue() {
        return envValue;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("envValue")
    public void setEnvValue(String envValue) {
        this.envValue = envValue;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("agentName", agentName).append("workflow", workflow).append("fileOrderSource", fileOrderSource).append("lock", lock).append("schedule", schedule).append("includeScript", includeScript).append("calendar", calendar).append("noticeBoard", noticeBoard).append("jobResource", jobResource).append("jobTemplate", jobTemplate).append("jobName", jobName).append("jobNameExactMatch", jobNameExactMatch).append("jobCriticality", jobCriticality).append("jobCountFrom", jobCountFrom).append("jobCountTo", jobCountTo).append("jobScript", jobScript).append("argumentName", argumentName).append("argumentValue", argumentValue).append("envName", envName).append("envValue", envValue).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendar).append(jobName).append(workflow).append(argumentName).append(jobCriticality).append(jobTemplate).append(includeScript).append(agentName).append(jobResource).append(jobNameExactMatch).append(argumentValue).append(jobScript).append(envValue).append(jobCountFrom).append(schedule).append(jobCountTo).append(envName).append(lock).append(fileOrderSource).append(noticeBoard).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestSearchAdvancedItem) == false) {
            return false;
        }
        RequestSearchAdvancedItem rhs = ((RequestSearchAdvancedItem) other);
        return new EqualsBuilder().append(calendar, rhs.calendar).append(jobName, rhs.jobName).append(workflow, rhs.workflow).append(argumentName, rhs.argumentName).append(jobCriticality, rhs.jobCriticality).append(jobTemplate, rhs.jobTemplate).append(includeScript, rhs.includeScript).append(agentName, rhs.agentName).append(jobResource, rhs.jobResource).append(jobNameExactMatch, rhs.jobNameExactMatch).append(argumentValue, rhs.argumentValue).append(jobScript, rhs.jobScript).append(envValue, rhs.envValue).append(jobCountFrom, rhs.jobCountFrom).append(schedule, rhs.schedule).append(jobCountTo, rhs.jobCountTo).append(envName, rhs.envName).append(lock, rhs.lock).append(fileOrderSource, rhs.fileOrderSource).append(noticeBoard, rhs.noticeBoard).isEquals();
    }

}
