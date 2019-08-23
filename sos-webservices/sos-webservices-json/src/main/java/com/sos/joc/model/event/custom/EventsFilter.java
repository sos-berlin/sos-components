
package com.sos.joc.model.event.custom;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.job.JobPath;
import com.sos.joc.model.order.OrderPath;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * customEventsFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "eventIds",
    "eventClasses",
    "exitCodes",
    "jobs",
    "orders",
    "folders",
    "regex",
    "dateFrom",
    "dateTo",
    "timeZone",
    "limit"
})
public class EventsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("eventIds")
    private List<String> eventIds = new ArrayList<String>();
    @JsonProperty("eventClasses")
    private List<String> eventClasses = new ArrayList<String>();
    @JsonProperty("exitCodes")
    private List<Integer> exitCodes = new ArrayList<Integer>();
    @JsonProperty("jobs")
    private List<JobPath> jobs = new ArrayList<JobPath>();
    @JsonProperty("orders")
    private List<OrderPath> orders = new ArrayList<OrderPath>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    private String regex;
    @JsonProperty("dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    private String dateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;
    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("restricts the number of responsed records; -1=unlimited")
    private Integer limit = 10000;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("eventIds")
    public List<String> getEventIds() {
        return eventIds;
    }

    @JsonProperty("eventIds")
    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    @JsonProperty("eventClasses")
    public List<String> getEventClasses() {
        return eventClasses;
    }

    @JsonProperty("eventClasses")
    public void setEventClasses(List<String> eventClasses) {
        this.eventClasses = eventClasses;
    }

    @JsonProperty("exitCodes")
    public List<Integer> getExitCodes() {
        return exitCodes;
    }

    @JsonProperty("exitCodes")
    public void setExitCodes(List<Integer> exitCodes) {
        this.exitCodes = exitCodes;
    }

    @JsonProperty("jobs")
    public List<JobPath> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(List<JobPath> jobs) {
        this.jobs = jobs;
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
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
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
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

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
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("eventIds", eventIds).append("eventClasses", eventClasses).append("exitCodes", exitCodes).append("jobs", jobs).append("orders", orders).append("folders", folders).append("regex", regex).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folders).append(jobs).append(eventClasses).append(timeZone).append(dateFrom).append(eventIds).append(regex).append(dateTo).append(limit).append(exitCodes).append(orders).append(jobschedulerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EventsFilter) == false) {
            return false;
        }
        EventsFilter rhs = ((EventsFilter) other);
        return new EqualsBuilder().append(folders, rhs.folders).append(jobs, rhs.jobs).append(eventClasses, rhs.eventClasses).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).append(eventIds, rhs.eventIds).append(regex, rhs.regex).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(exitCodes, rhs.exitCodes).append(orders, rhs.orders).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
