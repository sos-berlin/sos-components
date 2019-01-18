
package com.sos.joc.model.event.custom;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@Generated("org.jsonschema2pojo")
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
    private String timeZone;
    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    private Integer limit = 10000;

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
     *     The eventIds
     */
    @JsonProperty("eventIds")
    public List<String> getEventIds() {
        return eventIds;
    }

    /**
     * 
     * @param eventIds
     *     The eventIds
     */
    @JsonProperty("eventIds")
    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    /**
     * 
     * @return
     *     The eventClasses
     */
    @JsonProperty("eventClasses")
    public List<String> getEventClasses() {
        return eventClasses;
    }

    /**
     * 
     * @param eventClasses
     *     The eventClasses
     */
    @JsonProperty("eventClasses")
    public void setEventClasses(List<String> eventClasses) {
        this.eventClasses = eventClasses;
    }

    /**
     * 
     * @return
     *     The exitCodes
     */
    @JsonProperty("exitCodes")
    public List<Integer> getExitCodes() {
        return exitCodes;
    }

    /**
     * 
     * @param exitCodes
     *     The exitCodes
     */
    @JsonProperty("exitCodes")
    public void setExitCodes(List<Integer> exitCodes) {
        this.exitCodes = exitCodes;
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
     * restricts the number of responsed records; -1=unlimited
     * 
     * @return
     *     The limit
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     * @param limit
     *     The limit
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobschedulerId).append(eventIds).append(eventClasses).append(exitCodes).append(jobs).append(orders).append(folders).append(regex).append(dateFrom).append(dateTo).append(timeZone).append(limit).toHashCode();
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
        return new EqualsBuilder().append(jobschedulerId, rhs.jobschedulerId).append(eventIds, rhs.eventIds).append(eventClasses, rhs.eventClasses).append(exitCodes, rhs.exitCodes).append(jobs, rhs.jobs).append(orders, rhs.orders).append(folders, rhs.folders).append(regex, rhs.regex).append(dateFrom, rhs.dateFrom).append(dateTo, rhs.dateTo).append(timeZone, rhs.timeZone).append(limit, rhs.limit).isEquals();
    }

}
