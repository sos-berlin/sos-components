
package com.sos.joc.model.inventory.search;

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
 * Inventory search response
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "workflows",
    "fileOrderSources",
    "jobResources",
    "boards",
    "locks",
    "schedules"
})
public class ResponseSearch {

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
    @JsonProperty("workflows")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseSearchItem> workflows = new LinkedHashSet<ResponseSearchItem>();
    @JsonProperty("fileOrderSources")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseSearchItem> fileOrderSources = new LinkedHashSet<ResponseSearchItem>();
    @JsonProperty("jobResources")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseSearchItem> jobResources = new LinkedHashSet<ResponseSearchItem>();
    @JsonProperty("boards")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseSearchItem> boards = new LinkedHashSet<ResponseSearchItem>();
    @JsonProperty("locks")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseSearchItem> locks = new LinkedHashSet<ResponseSearchItem>();
    @JsonProperty("schedules")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseSearchItem> schedules = new LinkedHashSet<ResponseSearchItem>();

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

    @JsonProperty("workflows")
    public Set<ResponseSearchItem> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(Set<ResponseSearchItem> workflows) {
        this.workflows = workflows;
    }

    @JsonProperty("fileOrderSources")
    public Set<ResponseSearchItem> getFileOrderSources() {
        return fileOrderSources;
    }

    @JsonProperty("fileOrderSources")
    public void setFileOrderSources(Set<ResponseSearchItem> fileOrderSources) {
        this.fileOrderSources = fileOrderSources;
    }

    @JsonProperty("jobResources")
    public Set<ResponseSearchItem> getJobResources() {
        return jobResources;
    }

    @JsonProperty("jobResources")
    public void setJobResources(Set<ResponseSearchItem> jobResources) {
        this.jobResources = jobResources;
    }

    @JsonProperty("boards")
    public Set<ResponseSearchItem> getBoards() {
        return boards;
    }

    @JsonProperty("boards")
    public void setBoards(Set<ResponseSearchItem> boards) {
        this.boards = boards;
    }

    @JsonProperty("locks")
    public Set<ResponseSearchItem> getLocks() {
        return locks;
    }

    @JsonProperty("locks")
    public void setLocks(Set<ResponseSearchItem> locks) {
        this.locks = locks;
    }

    @JsonProperty("schedules")
    public Set<ResponseSearchItem> getSchedules() {
        return schedules;
    }

    @JsonProperty("schedules")
    public void setSchedules(Set<ResponseSearchItem> schedules) {
        this.schedules = schedules;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("workflows", workflows).append("fileOrderSources", fileOrderSources).append("jobResources", jobResources).append("boards", boards).append("locks", locks).append("schedules", schedules).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fileOrderSources).append(schedules).append(boards).append(workflows).append(deliveryDate).append(jobResources).append(locks).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseSearch) == false) {
            return false;
        }
        ResponseSearch rhs = ((ResponseSearch) other);
        return new EqualsBuilder().append(fileOrderSources, rhs.fileOrderSources).append(schedules, rhs.schedules).append(boards, rhs.boards).append(workflows, rhs.workflows).append(deliveryDate, rhs.deliveryDate).append(jobResources, rhs.jobResources).append(locks, rhs.locks).isEquals();
    }

}
