
package com.sos.joc.model.dailyplan.projections.items.meta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * daily plan projection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "totalOrders",
    "workflows"
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("totalOrders", totalOrders).append("workflows", workflows).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(totalOrders).append(workflows).toHashCode();
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
        return new EqualsBuilder().append(totalOrders, rhs.totalOrders).append(workflows, rhs.workflows).isEquals();
    }

}
