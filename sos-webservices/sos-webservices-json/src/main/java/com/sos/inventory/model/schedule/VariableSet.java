
package com.sos.inventory.model.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Variable-Set
 * <p>
 * A variable set for a schedule incl. optional start and end position
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderName",
    "variables",
    "startPosition",
    "endPosition"
})
public class VariableSet {

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
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> startPosition = null;
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> endPosition = null;

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
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    public List<Object> getStartPosition() {
        return startPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    public void setStartPosition(List<Object> startPosition) {
        this.startPosition = startPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    public List<Object> getEndPosition() {
        return endPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("endPosition")
    public void setEndPosition(List<Object> endPosition) {
        this.endPosition = endPosition;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderName", orderName).append("variables", variables).append("startPosition", startPosition).append("endPosition", endPosition).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variables).append(startPosition).append(endPosition).append(orderName).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof VariableSet) == false) {
            return false;
        }
        VariableSet rhs = ((VariableSet) other);
        return new EqualsBuilder().append(variables, rhs.variables).append(startPosition, rhs.startPosition).append(endPosition, rhs.endPosition).append(orderName, rhs.orderName).isEquals();
    }

}
