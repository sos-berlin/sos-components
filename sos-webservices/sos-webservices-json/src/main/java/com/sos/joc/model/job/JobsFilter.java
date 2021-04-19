
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.List;
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
    "orders",
    "regex",
    "dateFrom",
    "dateTo",
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
    @JsonProperty("orders")
    private List<OrderPath> orders = new ArrayList<OrderPath>();
    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter Controller objects by matching the path")
    private String regex;
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

    @JsonProperty("orders")
    public List<OrderPath> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(List<OrderPath> orders) {
        this.orders = orders;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("jobs", jobs).append("excludeJobs", excludeJobs).append("orders", orders).append("regex", regex).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("folders", folders).append("limit", limit).append("historyStates", historyStates).append("criticalities", criticalities).append("taskIds", taskIds).append("historyIds", historyIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folders).append(controllerId).append(jobs).append(historyStates).append(timeZone).append(criticalities).append(dateFrom).append(taskIds).append(historyIds).append(regex).append(dateTo).append(limit).append(orders).append(excludeJobs).toHashCode();
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
        return new EqualsBuilder().append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(jobs, rhs.jobs).append(historyStates, rhs.historyStates).append(timeZone, rhs.timeZone).append(criticalities, rhs.criticalities).append(dateFrom, rhs.dateFrom).append(taskIds, rhs.taskIds).append(historyIds, rhs.historyIds).append(regex, rhs.regex).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(orders, rhs.orders).append(excludeJobs, rhs.excludeJobs).isEquals();
    }

}
