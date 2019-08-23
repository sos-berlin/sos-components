
package com.sos.joc.model.audit;

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
 * auditLogFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "jobs",
    "orders",
    "calendars",
    "folders",
    "account",
    "regex",
    "dateFrom",
    "dateTo",
    "timeZone",
    "limit",
    "ticketLink"
})
public class AuditLogFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("jobs")
    private List<JobPath> jobs = new ArrayList<JobPath>();
    @JsonProperty("orders")
    private List<OrderPath> orders = new ArrayList<OrderPath>();
    @JsonProperty("calendars")
    private List<String> calendars = new ArrayList<String>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("account")
    private String account;
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
    @JsonProperty("ticketLink")
    private String ticketLink;

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

    @JsonProperty("calendars")
    public List<String> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(List<String> calendars) {
        this.calendars = calendars;
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

    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
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

    @JsonProperty("ticketLink")
    public String getTicketLink() {
        return ticketLink;
    }

    @JsonProperty("ticketLink")
    public void setTicketLink(String ticketLink) {
        this.ticketLink = ticketLink;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("jobs", jobs).append("orders", orders).append("calendars", calendars).append("folders", folders).append("account", account).append("regex", regex).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).append("limit", limit).append("ticketLink", ticketLink).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folders).append(jobs).append(timeZone).append(dateFrom).append(ticketLink).append(regex).append(calendars).append(dateTo).append(limit).append(orders).append(jobschedulerId).append(account).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AuditLogFilter) == false) {
            return false;
        }
        AuditLogFilter rhs = ((AuditLogFilter) other);
        return new EqualsBuilder().append(folders, rhs.folders).append(jobs, rhs.jobs).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).append(ticketLink, rhs.ticketLink).append(regex, rhs.regex).append(calendars, rhs.calendars).append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(orders, rhs.orders).append(jobschedulerId, rhs.jobschedulerId).append(account, rhs.account).isEquals();
    }

}
