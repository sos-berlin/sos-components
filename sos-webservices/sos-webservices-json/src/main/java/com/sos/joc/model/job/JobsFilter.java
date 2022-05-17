
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.HistoryStateText;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * jobsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "jobs",
    "excludeJobs",
    "jobName",
    "workflowPath",
    "workflowName",
    "dateFrom",
    "dateTo",
    "completedDateFrom",
    "completedDateTo",
    "timeZone",
    "folders",
    "limit",
    "historyStates",
    "criticalities",
    "taskIds",
    "historyIds"
})
public class JobsFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("jobs")
    private List<JobPath> jobs = new ArrayList<JobPath>();
    @JsonProperty("excludeJobs")
    private List<JobPath> excludeJobs = new ArrayList<JobPath>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jobName")
    private String jobName;
    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("workflowPath")
    @JsonPropertyDescription("pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character")
    private String workflowPath;
    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("workflowName")
    @JsonPropertyDescription("pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character")
    private String workflowName;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateTo;
    /**
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("completedDateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    @JsonAlias({
        "endDateFrom"
    })
    private String completedDateFrom;
    /**
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("completedDateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    @JsonAlias({
        "endDateTo"
    })
    private String completedDateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("only for db history urls to restrict the number of responsed records; -1=unlimited")
    private Integer limit = 10000;
    @JsonProperty("historyStates")
    private List<HistoryStateText> historyStates = new ArrayList<HistoryStateText>();
    @JsonProperty("criticalities")
    private List<JobCriticality> criticalities = new ArrayList<JobCriticality>();
    @JsonProperty("taskIds")
    private List<Long> taskIds = new ArrayList<Long>();
    @JsonProperty("historyIds")
    private List<TaskIdOfOrder> historyIds = new ArrayList<TaskIdOfOrder>();

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("jobs")
    public List<JobPath> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(List<JobPath> jobs) {
        this.jobs = jobs;
    }

    @JsonProperty("excludeJobs")
    public List<JobPath> getExcludeJobs() {
        return excludeJobs;
    }

    @JsonProperty("excludeJobs")
    public void setExcludeJobs(List<JobPath> excludeJobs) {
        this.excludeJobs = excludeJobs;
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

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("workflowPath")
    public String getWorkflowPath() {
        return workflowPath;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("workflowPath")
    public void setWorkflowPath(String workflowPath) {
        this.workflowPath = workflowPath;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("workflowName")
    public String getWorkflowName() {
        return workflowName;
    }

    /**
     * glob pattern
     * <p>
     * pattern with wildcards '*' and '?' where '*' match zero or more characters and '?' match any single character
     * 
     */
    @JsonProperty("workflowName")
    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("completedDateFrom")
    public String getCompletedDateFrom() {
        return completedDateFrom;
    }

    /**
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("completedDateFrom")
    public void setCompletedDateFrom(String completedDateFrom) {
        this.completedDateFrom = completedDateFrom;
    }

    /**
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("completedDateTo")
    public String getCompletedDateTo() {
        return completedDateTo;
    }

    /**
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("completedDateTo")
    public void setCompletedDateTo(String completedDateTo) {
        this.completedDateTo = completedDateTo;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @JsonProperty("historyStates")
    public List<HistoryStateText> getHistoryStates() {
        return historyStates;
    }

    @JsonProperty("historyStates")
    public void setHistoryStates(List<HistoryStateText> historyStates) {
        this.historyStates = historyStates;
    }

    @JsonProperty("criticalities")
    public List<JobCriticality> getCriticalities() {
        return criticalities;
    }

    @JsonProperty("criticalities")
    public void setCriticalities(List<JobCriticality> criticalities) {
        this.criticalities = criticalities;
    }

    @JsonProperty("taskIds")
    public List<Long> getTaskIds() {
        return taskIds;
    }

    @JsonProperty("taskIds")
    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }

    @JsonProperty("historyIds")
    public List<TaskIdOfOrder> getHistoryIds() {
        return historyIds;
    }

    @JsonProperty("historyIds")
    public void setHistoryIds(List<TaskIdOfOrder> historyIds) {
        this.historyIds = historyIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("jobs", jobs).append("excludeJobs", excludeJobs).append("jobName", jobName).append("workflowPath", workflowPath).append("workflowName", workflowName).append("dateFrom", dateFrom).append("dateTo", dateTo).append("completedDateFrom", completedDateFrom).append("completedDateTo", completedDateTo).append("timeZone", timeZone).append("folders", folders).append("limit", limit).append("historyStates", historyStates).append("criticalities", criticalities).append("taskIds", taskIds).append("historyIds", historyIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobName).append(folders).append(controllerId).append(workflowPath).append(completedDateFrom).append(jobs).append(historyStates).append(timeZone).append(workflowName).append(criticalities).append(dateFrom).append(taskIds).append(completedDateTo).append(historyIds).append(dateTo).append(limit).append(excludeJobs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobsFilter) == false) {
            return false;
        }
        JobsFilter rhs = ((JobsFilter) other);
        return new EqualsBuilder().append(jobName, rhs.jobName).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(workflowPath, rhs.workflowPath).append(completedDateFrom, rhs.completedDateFrom).append(jobs, rhs.jobs).append(historyStates, rhs.historyStates).append(timeZone, rhs.timeZone).append(workflowName, rhs.workflowName).append(criticalities, rhs.criticalities).append(dateFrom, rhs.dateFrom).append(taskIds, rhs.taskIds).append(completedDateTo, rhs.completedDateTo).append(historyIds, rhs.historyIds).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(excludeJobs, rhs.excludeJobs).isEquals();
    }

}
