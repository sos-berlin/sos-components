
package com.sos.joc.model.event.custom;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("eventIds")
    @JacksonXmlProperty(localName = "eventId")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "eventIds")
    private List<String> eventIds = new ArrayList<String>();
    @JsonProperty("eventClasses")
    @JacksonXmlProperty(localName = "eventClass")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "eventClasses")
    private List<String> eventClasses = new ArrayList<String>();
    @JsonProperty("exitCodes")
    @JacksonXmlProperty(localName = "exitCode")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "exitCodes")
    private List<Integer> exitCodes = new ArrayList<Integer>();
    @JsonProperty("jobs")
    @JacksonXmlProperty(localName = "job")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "jobs")
    private List<JobPath> jobs = new ArrayList<JobPath>();
    @JsonProperty("orders")
    @JacksonXmlProperty(localName = "order")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "orders")
    private List<OrderPath> orders = new ArrayList<OrderPath>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    @JacksonXmlProperty(localName = "folder")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "folders")
    private List<Folder> folders = new ArrayList<Folder>();
    /**
     * filter with regex
     * <p>
     * regular expression to filter JobScheduler objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter JobScheduler objects by matching the path")
    @JacksonXmlProperty(localName = "regex")
    private String regex;
    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    private String dateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    @JacksonXmlProperty(localName = "timeZone")
    private String timeZone;
    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("restricts the number of responsed records; -1=unlimited")
    @JacksonXmlProperty(localName = "limit")
    private Integer limit = 10000;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    @JacksonXmlProperty(localName = "jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    @JsonProperty("eventIds")
    @JacksonXmlProperty(localName = "eventId")
    public List<String> getEventIds() {
        return eventIds;
    }

    @JsonProperty("eventIds")
    @JacksonXmlProperty(localName = "eventId")
    public void setEventIds(List<String> eventIds) {
        this.eventIds = eventIds;
    }

    @JsonProperty("eventClasses")
    @JacksonXmlProperty(localName = "eventClass")
    public List<String> getEventClasses() {
        return eventClasses;
    }

    @JsonProperty("eventClasses")
    @JacksonXmlProperty(localName = "eventClass")
    public void setEventClasses(List<String> eventClasses) {
        this.eventClasses = eventClasses;
    }

    @JsonProperty("exitCodes")
    @JacksonXmlProperty(localName = "exitCode")
    public List<Integer> getExitCodes() {
        return exitCodes;
    }

    @JsonProperty("exitCodes")
    @JacksonXmlProperty(localName = "exitCode")
    public void setExitCodes(List<Integer> exitCodes) {
        this.exitCodes = exitCodes;
    }

    @JsonProperty("jobs")
    @JacksonXmlProperty(localName = "job")
    public List<JobPath> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    @JacksonXmlProperty(localName = "job")
    public void setJobs(List<JobPath> jobs) {
        this.jobs = jobs;
    }

    @JsonProperty("orders")
    @JacksonXmlProperty(localName = "order")
    public List<OrderPath> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    @JacksonXmlProperty(localName = "order")
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
    @JacksonXmlProperty(localName = "folder")
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
    @JacksonXmlProperty(localName = "folder")
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
    @JacksonXmlProperty(localName = "regex")
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
    @JacksonXmlProperty(localName = "regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    @JsonProperty("dateFrom")
    @JacksonXmlProperty(localName = "dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    public String getDateTo() {
        return dateTo;
    }

    @JsonProperty("dateTo")
    @JacksonXmlProperty(localName = "dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JacksonXmlProperty(localName = "timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JacksonXmlProperty(localName = "limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * restricts the number of responsed records; -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JacksonXmlProperty(localName = "limit")
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
