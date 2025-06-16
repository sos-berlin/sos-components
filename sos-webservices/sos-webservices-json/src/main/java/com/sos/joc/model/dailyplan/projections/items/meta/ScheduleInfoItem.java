
package com.sos.joc.model.dailyplan.projections.items.meta;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * daily plan projection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "totalOrders",
    "orderNames",
    "workflows",
    "workflowPaths",
    "excludedFromProjection"
})
public class ScheduleInfoItem {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalOrders")
    private Long totalOrders;
    /**
     * this property is only set if the schedule defines orders
     * 
     */
    @JsonProperty("orderNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("this property is only set if the schedule defines orders")
    private Set<String> orderNames = new LinkedHashSet<String>();
    /**
     * daily plan projection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflows")
    private WorkflowsItem workflows;
    /**
     * this property is only used for a shorter response of ./projections/day API
     * 
     */
    @JsonProperty("workflowPaths")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("this property is only used for a shorter response of ./projections/day API")
    private Set<String> workflowPaths = new LinkedHashSet<String>();
    @JsonProperty("excludedFromProjection")
    private Boolean excludedFromProjection;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalOrders")
    public Long getTotalOrders() {
        return totalOrders;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("totalOrders")
    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    /**
     * this property is only set if the schedule defines orders
     * 
     */
    @JsonProperty("orderNames")
    public Set<String> getOrderNames() {
        return orderNames;
    }

    /**
     * this property is only set if the schedule defines orders
     * 
     */
    @JsonProperty("orderNames")
    public void setOrderNames(Set<String> orderNames) {
        this.orderNames = orderNames;
    }

    /**
     * daily plan projection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflows")
    public WorkflowsItem getWorkflows() {
        return workflows;
    }

    /**
     * daily plan projection
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflows")
    public void setWorkflows(WorkflowsItem workflows) {
        this.workflows = workflows;
    }

    /**
     * this property is only used for a shorter response of ./projections/day API
     * 
     */
    @JsonProperty("workflowPaths")
    public Set<String> getWorkflowPaths() {
        return workflowPaths;
    }

    /**
     * this property is only used for a shorter response of ./projections/day API
     * 
     */
    @JsonProperty("workflowPaths")
    public void setWorkflowPaths(Set<String> workflowPaths) {
        this.workflowPaths = workflowPaths;
    }

    @JsonProperty("excludedFromProjection")
    public Boolean getExcludedFromProjection() {
        return excludedFromProjection;
    }

    @JsonProperty("excludedFromProjection")
    public void setExcludedFromProjection(Boolean excludedFromProjection) {
        this.excludedFromProjection = excludedFromProjection;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("totalOrders", totalOrders).append("orderNames", orderNames).append("workflows", workflows).append("workflowPaths", workflowPaths).append("excludedFromProjection", excludedFromProjection).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowPaths).append(excludedFromProjection).append(totalOrders).append(workflows).append(orderNames).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleInfoItem) == false) {
            return false;
        }
        ScheduleInfoItem rhs = ((ScheduleInfoItem) other);
        return new EqualsBuilder().append(workflowPaths, rhs.workflowPaths).append(excludedFromProjection, rhs.excludedFromProjection).append(totalOrders, rhs.totalOrders).append(workflows, rhs.workflows).append(orderNames, rhs.orderNames).isEquals();
    }

}
