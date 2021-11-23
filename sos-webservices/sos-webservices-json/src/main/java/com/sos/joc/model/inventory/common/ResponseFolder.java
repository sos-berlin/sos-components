
package com.sos.joc.model.inventory.common;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * folder content
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "path",
    "workflows",
    "jobs",
    "jobClasses",
    "jobResources",
    "locks",
    "noticeBoards",
    "fileOrderSources",
    "schedules",
    "includeScripts",
    "calendars"
})
public class ResponseFolder {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of an object.")
    private String path;
    @JsonProperty("workflows")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> workflows = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("jobs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> jobs = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("jobClasses")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> jobClasses = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("jobResources")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> jobResources = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("locks")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> locks = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("noticeBoards")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> noticeBoards = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("fileOrderSources")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> fileOrderSources = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("schedules")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> schedules = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("includeScripts")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> includeScripts = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("calendars")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> calendars = new LinkedHashSet<ResponseFolderItem>();

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty("workflows")
    public Set<ResponseFolderItem> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(Set<ResponseFolderItem> workflows) {
        this.workflows = workflows;
    }

    @JsonProperty("jobs")
    public Set<ResponseFolderItem> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(Set<ResponseFolderItem> jobs) {
        this.jobs = jobs;
    }

    @JsonProperty("jobClasses")
    public Set<ResponseFolderItem> getJobClasses() {
        return jobClasses;
    }

    @JsonProperty("jobClasses")
    public void setJobClasses(Set<ResponseFolderItem> jobClasses) {
        this.jobClasses = jobClasses;
    }

    @JsonProperty("jobResources")
    public Set<ResponseFolderItem> getJobResources() {
        return jobResources;
    }

    @JsonProperty("jobResources")
    public void setJobResources(Set<ResponseFolderItem> jobResources) {
        this.jobResources = jobResources;
    }

    @JsonProperty("locks")
    public Set<ResponseFolderItem> getLocks() {
        return locks;
    }

    @JsonProperty("locks")
    public void setLocks(Set<ResponseFolderItem> locks) {
        this.locks = locks;
    }

    @JsonProperty("noticeBoards")
    public Set<ResponseFolderItem> getNoticeBoards() {
        return noticeBoards;
    }

    @JsonProperty("noticeBoards")
    public void setNoticeBoards(Set<ResponseFolderItem> noticeBoards) {
        this.noticeBoards = noticeBoards;
    }

    @JsonProperty("fileOrderSources")
    public Set<ResponseFolderItem> getFileOrderSources() {
        return fileOrderSources;
    }

    @JsonProperty("fileOrderSources")
    public void setFileOrderSources(Set<ResponseFolderItem> fileOrderSources) {
        this.fileOrderSources = fileOrderSources;
    }

    @JsonProperty("schedules")
    public Set<ResponseFolderItem> getSchedules() {
        return schedules;
    }

    @JsonProperty("schedules")
    public void setSchedules(Set<ResponseFolderItem> schedules) {
        this.schedules = schedules;
    }

    @JsonProperty("includeScripts")
    public Set<ResponseFolderItem> getIncludeScripts() {
        return includeScripts;
    }

    @JsonProperty("includeScripts")
    public void setIncludeScripts(Set<ResponseFolderItem> includeScripts) {
        this.includeScripts = includeScripts;
    }

    @JsonProperty("calendars")
    public Set<ResponseFolderItem> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(Set<ResponseFolderItem> calendars) {
        this.calendars = calendars;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("path", path).append("workflows", workflows).append("jobs", jobs).append("jobClasses", jobClasses).append("jobResources", jobResources).append("locks", locks).append("noticeBoards", noticeBoards).append("fileOrderSources", fileOrderSources).append("schedules", schedules).append("includeScripts", includeScripts).append("calendars", calendars).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jobs).append(jobClasses).append(workflows).append(locks).append(noticeBoards).append(path).append(fileOrderSources).append(calendars).append(schedules).append(deliveryDate).append(jobResources).append(includeScripts).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseFolder) == false) {
            return false;
        }
        ResponseFolder rhs = ((ResponseFolder) other);
        return new EqualsBuilder().append(jobs, rhs.jobs).append(jobClasses, rhs.jobClasses).append(workflows, rhs.workflows).append(locks, rhs.locks).append(noticeBoards, rhs.noticeBoards).append(path, rhs.path).append(fileOrderSources, rhs.fileOrderSources).append(calendars, rhs.calendars).append(schedules, rhs.schedules).append(deliveryDate, rhs.deliveryDate).append(jobResources, rhs.jobResources).append(includeScripts, rhs.includeScripts).isEquals();
    }

}
