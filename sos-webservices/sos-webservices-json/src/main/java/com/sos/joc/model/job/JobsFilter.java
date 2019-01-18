
package com.sos.joc.model.job;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
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
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({
    "jobschedulerId",
    "jobs",
    "excludeJobs",
    "orders",
    "compact",
    "compactView",
    "regex",
    "isOrderJob",
    "dateFrom",
    "dateTo",
    "timeZone",
    "folders",
    "states",
    "limit",
    "historyStates",
    "taskIds",
    "historyIds",
    "runTimeIsTemporary"
})
public class JobsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("jobs")
    private List<JobPath> jobs = new ArrayList<JobPath>();
    @JsonProperty("excludeJobs")
    private List<JobPath> excludeJobs = new ArrayList<JobPath>();
    @JsonProperty("orders")
    private List<OrderPath> orders = new ArrayList<OrderPath>();
    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     */
    @JsonProperty("compact")
    private Boolean compact = false;
    @JsonProperty("compactView")
    private Boolean compactView = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    private String regex;
    @JsonProperty("isOrderJob")
    private Boolean isOrderJob;
    @JsonProperty("dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    private String dateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    private String timeZone;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("states")
    private List<JobStateFilter> states = new ArrayList<JobStateFilter>();
    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    private Integer limit = 10000;
    @JsonProperty("historyStates")
    private List<HistoryStateText> historyStates = new ArrayList<HistoryStateText>();
    @JsonProperty("taskIds")
    private List<Long> taskIds = new ArrayList<Long>();
    @JsonProperty("historyIds")
    private List<TaskIdOfOrder> historyIds = new ArrayList<TaskIdOfOrder>();
    @JsonProperty("runTimeIsTemporary")
    private Boolean runTimeIsTemporary;

    /**
     * 
     * (Required)
     * 
     * @return
     *     The jobschedulerId
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     * @param jobschedulerId
     *     The jobschedulerId
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * 
     * @return
     *     The jobs
     */
    @JsonProperty("jobs")
    public List<JobPath> getJobs() {
        return jobs;
    }

    /**
     * 
     * @param jobs
     *     The jobs
     */
    @JsonProperty("jobs")
    public void setJobs(List<JobPath> jobs) {
        this.jobs = jobs;
    }

    /**
     * 
     * @return
     *     The excludeJobs
     */
    @JsonProperty("excludeJobs")
    public List<JobPath> getExcludeJobs() {
        return excludeJobs;
    }

    /**
     * 
     * @param excludeJobs
     *     The excludeJobs
     */
    @JsonProperty("excludeJobs")
    public void setExcludeJobs(List<JobPath> excludeJobs) {
        this.excludeJobs = excludeJobs;
    }

    /**
     * 
     * @return
     *     The orders
     */
    @JsonProperty("orders")
    public List<OrderPath> getOrders() {
        return orders;
    }

    /**
     * 
     * @param orders
     *     The orders
     */
    @JsonProperty("orders")
    public void setOrders(List<OrderPath> orders) {
        this.orders = orders;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     * @return
     *     The compact
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object view is compact or detailed
     * 
     * @param compact
     *     The compact
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * 
     * @return
     *     The compactView
     */
    @JsonProperty("compactView")
    public Boolean getCompactView() {
        return compactView;
    }

    /**
     * 
     * @param compactView
     *     The compactView
     */
    @JsonProperty("compactView")
    public void setCompactView(Boolean compactView) {
        this.compactView = compactView;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     * @return
     *     The regex
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     * @param regex
     *     The regex
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    /**
     * 
     * @return
     *     The isOrderJob
     */
    @JsonProperty("isOrderJob")
    public Boolean getIsOrderJob() {
        return isOrderJob;
    }

    /**
     * 
     * @param isOrderJob
     *     The isOrderJob
     */
    @JsonProperty("isOrderJob")
    public void setIsOrderJob(Boolean isOrderJob) {
        this.isOrderJob = isOrderJob;
    }

    /**
     * 
     * @return
     *     The dateFrom
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * 
     * @param dateFrom
     *     The dateFrom
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * 
     * @return
     *     The dateTo
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * 
     * @param dateTo
     *     The dateTo
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     * @return
     *     The timeZone
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     * @param timeZone
     *     The timeZone
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
     * @return
     *     The folders
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
     * @param folders
     *     The folders
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    /**
     * 
     * @return
     *     The states
     */
    @JsonProperty("states")
    public List<JobStateFilter> getStates() {
        return states;
    }

    /**
     * 
     * @param states
     *     The states
     */
    @JsonProperty("states")
    public void setStates(List<JobStateFilter> states) {
        this.states = states;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     * @return
     *     The limit
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * only for db history urls to restrict the number of responsed records; -1=unlimited
     * 
     * @param limit
     *     The limit
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * 
     * @return
     *     The historyStates
     */
    @JsonProperty("historyStates")
    public List<HistoryStateText> getHistoryStates() {
        return historyStates;
    }

    /**
     * 
     * @param historyStates
     *     The historyStates
     */
    @JsonProperty("historyStates")
    public void setHistoryStates(List<HistoryStateText> historyStates) {
        this.historyStates = historyStates;
    }

    /**
     * 
     * @return
     *     The taskIds
     */
    @JsonProperty("taskIds")
    public List<Long> getTaskIds() {
        return taskIds;
    }

    /**
     * 
     * @param taskIds
     *     The taskIds
     */
    @JsonProperty("taskIds")
    public void setTaskIds(List<Long> taskIds) {
        this.taskIds = taskIds;
    }

    /**
     * 
     * @return
     *     The historyIds
     */
    @JsonProperty("historyIds")
    public List<TaskIdOfOrder> getHistoryIds() {
        return historyIds;
    }

    /**
     * 
     * @param historyIds
     *     The historyIds
     */
    @JsonProperty("historyIds")
    public void setHistoryIds(List<TaskIdOfOrder> historyIds) {
        this.historyIds = historyIds;
    }

    /**
     * 
     * @return
     *     The runTimeIsTemporary
     */
    @JsonProperty("runTimeIsTemporary")
    public Boolean getRunTimeIsTemporary() {
        return runTimeIsTemporary;
    }

    /**
     * 
     * @param runTimeIsTemporary
     *     The runTimeIsTemporary
     */
    @JsonProperty("runTimeIsTemporary")
    public void setRunTimeIsTemporary(Boolean runTimeIsTemporary) {
        this.runTimeIsTemporary = runTimeIsTemporary;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(jobs).append(excludeJobs).append(orders).append(compact).append(compactView).append(regex).append(isOrderJob).append(dateFrom).append(dateTo).append(timeZone).append(folders).append(states).append(limit).append(historyStates).append(taskIds).append(historyIds).append(runTimeIsTemporary).toHashCode();
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
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(jobs, rhs.jobs).append(excludeJobs, rhs.excludeJobs).append(orders, rhs.orders).append(compact, rhs.compact).append(compactView, rhs.compactView).append(regex, rhs.regex).append(isOrderJob, rhs.isOrderJob).append(dateFrom, rhs.dateFrom).append(dateTo, rhs.dateTo).append(timeZone, rhs.timeZone).append(folders, rhs.folders).append(states, rhs.states).append(limit, rhs.limit).append(historyStates, rhs.historyStates).append(taskIds, rhs.taskIds).append(historyIds, rhs.historyIds).append(runTimeIsTemporary, rhs.runTimeIsTemporary).isEquals();
    }

}
