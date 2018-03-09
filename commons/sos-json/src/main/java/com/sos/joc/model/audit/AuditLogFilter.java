
package com.sos.joc.model.audit;

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
    @JacksonXmlProperty(localName = "jobschedulerId")
    private String jobschedulerId;
    @JsonProperty("jobs")
    @JacksonXmlProperty(localName = "job")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "jobs")
    private List<JobPath> jobs = new ArrayList<JobPath>();
    @JsonProperty("orders")
    @JacksonXmlProperty(localName = "order")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "orders")
    private List<OrderPath> orders = new ArrayList<OrderPath>();
    @JsonProperty("calendars")
    @JacksonXmlProperty(localName = "calendar")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "calendars")
    private List<String> calendars = new ArrayList<String>();
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
    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
    private String account;
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
    @JsonProperty("ticketLink")
    @JacksonXmlProperty(localName = "ticketLink")
    private String ticketLink;

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

    @JsonProperty("calendars")
    @JacksonXmlProperty(localName = "calendar")
    public List<String> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    @JacksonXmlProperty(localName = "calendar")
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

    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
    public String getAccount() {
        return account;
    }

    @JsonProperty("account")
    @JacksonXmlProperty(localName = "account")
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

    @JsonProperty("ticketLink")
    @JacksonXmlProperty(localName = "ticketLink")
    public String getTicketLink() {
        return ticketLink;
    }

    @JsonProperty("ticketLink")
    @JacksonXmlProperty(localName = "ticketLink")
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
