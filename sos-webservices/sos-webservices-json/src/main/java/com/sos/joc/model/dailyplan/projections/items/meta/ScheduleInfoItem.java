
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
    "workflows",
    "workflowPaths"
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
    @JsonPropertyDescription("this property is only used for a shorter response of ./projections/day API")
    private Set<String> workflowPaths;

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("totalOrders", totalOrders).append("workflows", workflows).append("workflowPaths", workflowPaths).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflowPaths).append(totalOrders).append(workflows).toHashCode();
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
        return new EqualsBuilder().append(workflowPaths, rhs.workflowPaths).append(totalOrders, rhs.totalOrders).append(workflows, rhs.workflows).isEquals();
    }

}
