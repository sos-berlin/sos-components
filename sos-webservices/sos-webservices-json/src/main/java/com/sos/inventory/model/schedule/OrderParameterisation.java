
package com.sos.inventory.model.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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
    "positions"
})
public class OrderParameterisation {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderName")
    public String getOrderName() {
        return orderName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderName", orderName).append("variables", variables).append("positions", positions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variables).append(positions).append(orderName).toHashCode();
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
        return new EqualsBuilder().append(variables, rhs.variables).append(positions, rhs.positions).append(orderName, rhs.orderName).isEquals();
    }

}
