
package com.sos.inventory.model.schedule;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * orderParameterisation
 * <p>
 * Parameterisation with variable set for a schedule and optional start and end position
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderName",
    "variables",
    "positions",
    "tags",
    "forceJobAdmission",
    "priority"
})
public class OrderParameterisation {

    @JsonProperty("orderName")
    private String orderName;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables variables;
    /**
     * positions
     * <p>
     * start and end position
     * 
     */
    @JsonProperty("positions")
    @JsonPropertyDescription("start and end position")
    private OrderPositions positions;
    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> tags = null;
    @JsonProperty("forceJobAdmission")
    private Boolean forceJobAdmission;
    @JsonProperty("priority")
    private Integer priority = 0;

    @JsonProperty("orderName")
    public String getOrderName() {
        return orderName;
    }

    @JsonProperty("orderName")
    public void setOrderName(String orderName) {
        this.orderName = orderName;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    public Variables getVariables() {
        return variables;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("variables")
    public void setVariables(Variables variables) {
        this.variables = variables;
    }

    /**
     * positions
     * <p>
     * start and end position
     * 
     */
    @JsonProperty("positions")
    public OrderPositions getPositions() {
        return positions;
    }

    /**
     * positions
     * <p>
     * start and end position
     * 
     */
    @JsonProperty("positions")
    public void setPositions(OrderPositions positions) {
        this.positions = positions;
    }

    @JsonProperty("tags")
    public Set<String> getTags() {
        return tags;
    }

    @JsonProperty("tags")
    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    @JsonProperty("forceJobAdmission")
    public Boolean getForceJobAdmission() {
        return forceJobAdmission;
    }

    @JsonProperty("forceJobAdmission")
    public void setForceJobAdmission(Boolean forceJobAdmission) {
        this.forceJobAdmission = forceJobAdmission;
    }

    @JsonProperty("priority")
    public Integer getPriority() {
        return priority;
    }

    @JsonProperty("priority")
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderName", orderName).append("variables", variables).append("positions", positions).append("tags", tags).append("forceJobAdmission", forceJobAdmission).append("priority", priority).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variables).append(forceJobAdmission).append(positions).append(priority).append(orderName).append(tags).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderParameterisation) == false) {
            return false;
        }
        OrderParameterisation rhs = ((OrderParameterisation) other);
        return new EqualsBuilder().append(variables, rhs.variables).append(forceJobAdmission, rhs.forceJobAdmission).append(positions, rhs.positions).append(priority, rhs.priority).append(orderName, rhs.orderName).append(tags, rhs.tags).isEquals();
    }

}
