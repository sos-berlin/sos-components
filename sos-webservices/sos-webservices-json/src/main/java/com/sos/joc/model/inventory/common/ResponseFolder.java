
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
    "locks",
    "junctions",
    "orderTemplates",
    "calendars",
    "folders"
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
     * absolute path of a JobScheduler object.
     * (Required)
     * 
     */
    @JsonProperty("path")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
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
    @JsonProperty("locks")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> locks = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("junctions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> junctions = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("orderTemplates")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> orderTemplates = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("calendars")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> calendars = new LinkedHashSet<ResponseFolderItem>();
    @JsonProperty("folders")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseFolderItem> folders = new LinkedHashSet<ResponseFolderItem>();

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
     * absolute path of a JobScheduler object.
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
     * absolute path of a JobScheduler object.
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

    @JsonProperty("locks")
    public Set<ResponseFolderItem> getLocks() {
        return locks;
    }

    @JsonProperty("locks")
    public void setLocks(Set<ResponseFolderItem> locks) {
        this.locks = locks;
    }

    @JsonProperty("junctions")
    public Set<ResponseFolderItem> getJunctions() {
        return junctions;
    }

    @JsonProperty("junctions")
    public void setJunctions(Set<ResponseFolderItem> junctions) {
        this.junctions = junctions;
    }

    @JsonProperty("orderTemplates")
    public Set<ResponseFolderItem> getOrderTemplates() {
        return orderTemplates;
    }

    @JsonProperty("orderTemplates")
    public void setOrderTemplates(Set<ResponseFolderItem> orderTemplates) {
        this.orderTemplates = orderTemplates;
    }

    @JsonProperty("calendars")
    public Set<ResponseFolderItem> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(Set<ResponseFolderItem> calendars) {
        this.calendars = calendars;
    }

    @JsonProperty("folders")
    public Set<ResponseFolderItem> getFolders() {
        return folders;
    }

    @JsonProperty("folders")
    public void setFolders(Set<ResponseFolderItem> folders) {
        this.folders = folders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("path", path).append("workflows", workflows).append("jobs", jobs).append("jobClasses", jobClasses).append("locks", locks).append("junctions", junctions).append("orderTemplates", orderTemplates).append("calendars", calendars).append("folders", folders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(folders).append(calendars).append(jobs).append(jobClasses).append(workflows).append(deliveryDate).append(orderTemplates).append(locks).append(junctions).toHashCode();
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
        return new EqualsBuilder().append(path, rhs.path).append(folders, rhs.folders).append(calendars, rhs.calendars).append(jobs, rhs.jobs).append(jobClasses, rhs.jobClasses).append(workflows, rhs.workflows).append(deliveryDate, rhs.deliveryDate).append(orderTemplates, rhs.orderTemplates).append(locks, rhs.locks).append(junctions, rhs.junctions).isEquals();
    }

}
