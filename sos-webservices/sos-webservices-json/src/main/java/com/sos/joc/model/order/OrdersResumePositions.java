
package com.sos.joc.model.order;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.inventory.model.common.Variables;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * available positions for a resume
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderIds",
    "disabledPositionChange",
    "variables",
    "variablesNotSettable"
})
public class OrdersResumePositions
    extends OrdersPositions
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> orderIds = new LinkedHashSet<String>();
    /**
     * error
     * <p>
     * reasons that disallow the position change
     * 
     */
    @JsonProperty("disabledPositionChange")
    @JsonPropertyDescription("reasons that disallow the position change")
    private PositionChange disabledPositionChange;
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
     * only relevant for resuming a single order. Occurs if order starts from the beginning in its scope
     * 
     */
    @JsonProperty("variablesNotSettable")
    @JsonPropertyDescription("only relevant for resuming a single order. Occurs if order starts from the beginning in its scope")
    private Boolean variablesNotSettable;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIds")
    public Set<String> getOrderIds() {
        return orderIds;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderIds")
    public void setOrderIds(Set<String> orderIds) {
        this.orderIds = orderIds;
    }

    /**
     * error
     * <p>
     * reasons that disallow the position change
     * 
     */
    @JsonProperty("disabledPositionChange")
    public PositionChange getDisabledPositionChange() {
        return disabledPositionChange;
    }

    /**
     * error
     * <p>
     * reasons that disallow the position change
     * 
     */
    @JsonProperty("disabledPositionChange")
    public void setDisabledPositionChange(PositionChange disabledPositionChange) {
        this.disabledPositionChange = disabledPositionChange;
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
     * only relevant for resuming a single order. Occurs if order starts from the beginning in its scope
     * 
     */
    @JsonProperty("variablesNotSettable")
    public Boolean getVariablesNotSettable() {
        return variablesNotSettable;
    }

    /**
     * only relevant for resuming a single order. Occurs if order starts from the beginning in its scope
     * 
     */
    @JsonProperty("variablesNotSettable")
    public void setVariablesNotSettable(Boolean variablesNotSettable) {
        this.variablesNotSettable = variablesNotSettable;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("orderIds", orderIds).append("disabledPositionChange", disabledPositionChange).append("variables", variables).append("variablesNotSettable", variablesNotSettable).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(variablesNotSettable).append(variables).append(orderIds).append(disabledPositionChange).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrdersResumePositions) == false) {
            return false;
        }
        OrdersResumePositions rhs = ((OrdersResumePositions) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(variablesNotSettable, rhs.variablesNotSettable).append(variables, rhs.variables).append(orderIds, rhs.orderIds).append(disabledPositionChange, rhs.disabledPositionChange).isEquals();
    }

}
