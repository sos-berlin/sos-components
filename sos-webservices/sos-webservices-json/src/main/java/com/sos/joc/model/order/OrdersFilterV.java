
package com.sos.joc.model.order;

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
 * orders filter for volatile information
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderIds",
    "compact",
    "regex",
    "states",
    "agentNames",
    "orderTags",
    "workflowTags",
    "stateDateFrom",
    "stateDateTo",
    "limit",
    "withoutWorkflowTags"
})
public class OrdersFilterV
    extends OrdersFilterVBase
{

    @JsonProperty("orderIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> orderIds = new LinkedHashSet<String>();
    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    @JsonPropertyDescription("controls if the object's data is compact or detailed")
    private Boolean compact = false;
    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    @JsonPropertyDescription("regular expression to filter Controller objects by matching the path")
    private String regex;
    @JsonProperty("states")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<OrderStateText> states = new LinkedHashSet<OrderStateText>();
    @JsonProperty("agentNames")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> agentNames = new LinkedHashSet<String>();
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> orderTags = new LinkedHashSet<String>();
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> workflowTags = new LinkedHashSet<String>();
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String stateDateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String stateDateTo;
    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    @JsonPropertyDescription("-1=unlimited")
    private Integer limit = 10000;
    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    @JsonPropertyDescription("if true then response doesn't contain 'workflowsTagPerWorkflow'")
    private Boolean withoutWorkflowTags = false;

    @JsonProperty("orderIds")
    public Set<String> getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(Set<String> orderIds) {
        this.orderIds = orderIds;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public Boolean getCompact() {
        return compact;
    }

    /**
     * compact parameter
     * <p>
     * controls if the object's data is compact or detailed
     * 
     */
    @JsonProperty("compact")
    public void setCompact(Boolean compact) {
        this.compact = compact;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public String getRegex() {
        return regex;
    }

    /**
     * filter with regex
     * <p>
     * regular expression to filter Controller objects by matching the path
     * 
     */
    @JsonProperty("regex")
    public void setRegex(String regex) {
        this.regex = regex;
    }

    @JsonProperty("states")
    public Set<OrderStateText> getStates() {
        return states;
    }

    @JsonProperty("states")
    public void setStates(Set<OrderStateText> states) {
        this.states = states;
    }

    @JsonProperty("agentNames")
    public Set<String> getAgentNames() {
        return agentNames;
    }

    @JsonProperty("agentNames")
    public void setAgentNames(Set<String> agentNames) {
        this.agentNames = agentNames;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    public Set<String> getOrderTags() {
        return orderTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("orderTags")
    public void setOrderTags(Set<String> orderTags) {
        this.orderTags = orderTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public Set<String> getWorkflowTags() {
        return workflowTags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowTags")
    public void setWorkflowTags(Set<String> workflowTags) {
        this.workflowTags = workflowTags;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateFrom")
    public String getStateDateFrom() {
        return stateDateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateFrom")
    public void setStateDateFrom(String stateDateFrom) {
        this.stateDateFrom = stateDateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateTo")
    public String getStateDateTo() {
        return stateDateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("stateDateTo")
    public void setStateDateTo(String stateDateTo) {
        this.stateDateTo = stateDateTo;
    }

    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    /**
     * -1=unlimited
     * 
     */
    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    public Boolean getWithoutWorkflowTags() {
        return withoutWorkflowTags;
    }

    /**
     * if true then response doesn't contain 'workflowsTagPerWorkflow'
     * 
     */
    @JsonProperty("withoutWorkflowTags")
    public void setWithoutWorkflowTags(Boolean withoutWorkflowTags) {
        this.withoutWorkflowTags = withoutWorkflowTags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderIds", orderIds).append("compact", compact).append("regex", regex).append("states", states).append("agentNames", agentNames).append("orderTags", orderTags).append("workflowTags", workflowTags).append("stateDateFrom", stateDateFrom).append("stateDateTo", stateDateTo).append("limit", limit).append("withoutWorkflowTags", withoutWorkflowTags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(stateDateFrom).append(withoutWorkflowTags).append(regex).append(agentNames).append(compact).append(limit).append(orderIds).append(workflowTags).append(stateDateTo).append(states).append(orderTags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrdersFilterV) == false) {
            return false;
        }
        OrdersFilterV rhs = ((OrdersFilterV) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(stateDateFrom, rhs.stateDateFrom).append(withoutWorkflowTags, rhs.withoutWorkflowTags).append(regex, rhs.regex).append(agentNames, rhs.agentNames).append(compact, rhs.compact).append(limit, rhs.limit).append(orderIds, rhs.orderIds).append(workflowTags, rhs.workflowTags).append(stateDateTo, rhs.stateDateTo).append(states, rhs.states).append(orderTags, rhs.orderTags).isEquals();
    }

}
