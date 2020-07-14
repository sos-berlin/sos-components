
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
    "agentClusters",
    "locks",
    "junctions",
    "orders",
    "calendars"
})
public class Folder {

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
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
    private Set<FolderItem> workflows = new LinkedHashSet<FolderItem>();
    @JsonProperty("jobs")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<FolderItem> jobs = new LinkedHashSet<FolderItem>();
    @JsonProperty("jobClasses")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<FolderItem> jobClasses = new LinkedHashSet<FolderItem>();
    @JsonProperty("agentClusters")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<FolderItem> agentClusters = new LinkedHashSet<FolderItem>();
    @JsonProperty("locks")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<FolderItem> locks = new LinkedHashSet<FolderItem>();
    @JsonProperty("junctions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<FolderItem> junctions = new LinkedHashSet<FolderItem>();
    @JsonProperty("orders")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<FolderItem> orders = new LinkedHashSet<FolderItem>();
    @JsonProperty("calendars")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<FolderItem> calendars = new LinkedHashSet<FolderItem>();

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * (Required)
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
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
    public Set<FolderItem> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(Set<FolderItem> workflows) {
        this.workflows = workflows;
    }

    @JsonProperty("jobs")
    public Set<FolderItem> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(Set<FolderItem> jobs) {
        this.jobs = jobs;
    }

    @JsonProperty("jobClasses")
    public Set<FolderItem> getJobClasses() {
        return jobClasses;
    }

    @JsonProperty("jobClasses")
    public void setJobClasses(Set<FolderItem> jobClasses) {
        this.jobClasses = jobClasses;
    }

    @JsonProperty("agentClusters")
    public Set<FolderItem> getAgentClusters() {
        return agentClusters;
    }

    @JsonProperty("agentClusters")
    public void setAgentClusters(Set<FolderItem> agentClusters) {
        this.agentClusters = agentClusters;
    }

    @JsonProperty("locks")
    public Set<FolderItem> getLocks() {
        return locks;
    }

    @JsonProperty("locks")
    public void setLocks(Set<FolderItem> locks) {
        this.locks = locks;
    }

    @JsonProperty("junctions")
    public Set<FolderItem> getJunctions() {
        return junctions;
    }

    @JsonProperty("junctions")
    public void setJunctions(Set<FolderItem> junctions) {
        this.junctions = junctions;
    }

    @JsonProperty("orders")
    public Set<FolderItem> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(Set<FolderItem> orders) {
        this.orders = orders;
    }

    @JsonProperty("calendars")
    public Set<FolderItem> getCalendars() {
        return calendars;
    }

    @JsonProperty("calendars")
    public void setCalendars(Set<FolderItem> calendars) {
        this.calendars = calendars;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("path", path).append("workflows", workflows).append("jobs", jobs).append("jobClasses", jobClasses).append("agentClusters", agentClusters).append("locks", locks).append("junctions", junctions).append("orders", orders).append("calendars", calendars).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(agentClusters).append(calendars).append(jobs).append(jobClasses).append(orders).append(workflows).append(deliveryDate).append(locks).append(junctions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Folder) == false) {
            return false;
        }
        Folder rhs = ((Folder) other);
        return new EqualsBuilder().append(path, rhs.path).append(agentClusters, rhs.agentClusters).append(calendars, rhs.calendars).append(jobs, rhs.jobs).append(jobClasses, rhs.jobClasses).append(orders, rhs.orders).append(workflows, rhs.workflows).append(deliveryDate, rhs.deliveryDate).append(locks, rhs.locks).append(junctions, rhs.junctions).isEquals();
    }

}
