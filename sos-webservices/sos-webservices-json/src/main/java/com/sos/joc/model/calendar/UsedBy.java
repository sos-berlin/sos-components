
package com.sos.joc.model.calendar;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * collections of objects which use calendar
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "jobs",
    "orders",
    "schedules"
})
public class UsedBy {

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
    @JsonProperty("jobs")
    private List<String> jobs = null;
    @JsonProperty("orders")
    private List<String> orders = null;
    @JsonProperty("schedules")
    private List<String> schedules = null;

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

    @JsonProperty("jobs")
    public List<String> getJobs() {
        return jobs;
    }

    @JsonProperty("jobs")
    public void setJobs(List<String> jobs) {
        this.jobs = jobs;
    }

    @JsonProperty("orders")
    public List<String> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(List<String> orders) {
        this.orders = orders;
    }

    @JsonProperty("schedules")
    public List<String> getSchedules() {
        return schedules;
    }

    @JsonProperty("schedules")
    public void setSchedules(List<String> schedules) {
        this.schedules = schedules;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("jobs", jobs).append("orders", orders).append("schedules", schedules).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orders).append(deliveryDate).append(jobs).append(schedules).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof UsedBy) == false) {
            return false;
        }
        UsedBy rhs = ((UsedBy) other);
        return new EqualsBuilder().append(orders, rhs.orders).append(deliveryDate, rhs.deliveryDate).append(jobs, rhs.jobs).append(schedules, rhs.schedules).isEquals();
    }

}
