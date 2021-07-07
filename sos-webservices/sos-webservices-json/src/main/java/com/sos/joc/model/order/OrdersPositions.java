
package com.sos.joc.model.order;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.workflow.WorkflowId;
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
    "deliveryDate",
    "surveyDate",
    "orderIds",
    "workflowId",
    "positions",
    "disabledPositionChange",
    "variables"
})
public class OrdersPositions {

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
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    @JsonProperty("orderIds")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> orderIds = new LinkedHashSet<String>();
    /**
     * workflowId
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowId")
    private WorkflowId workflowId;
    @JsonProperty("positions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Positions> positions = new LinkedHashSet<Positions>();
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

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    public Date getSurveyDate() {
        return surveyDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    @JsonProperty("orderIds")
    public Set<String> getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(Set<String> orderIds) {
        this.orderIds = orderIds;
    }

    /**
     * workflowId
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowId")
    public WorkflowId getWorkflowId() {
        return workflowId;
    }

    /**
     * workflowId
     * <p>
     * 
     * 
     */
    @JsonProperty("workflowId")
    public void setWorkflowId(WorkflowId workflowId) {
        this.workflowId = workflowId;
    }

    @JsonProperty("positions")
    public Set<Positions> getPositions() {
        return positions;
    }

    @JsonProperty("positions")
    public void setPositions(Set<Positions> positions) {
        this.positions = positions;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("orderIds", orderIds).append("workflowId", workflowId).append("positions", positions).append("disabledPositionChange", disabledPositionChange).append("variables", variables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(variables).append(surveyDate).append(positions).append(orderIds).append(disabledPositionChange).append(deliveryDate).append(workflowId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrdersPositions) == false) {
            return false;
        }
        OrdersPositions rhs = ((OrdersPositions) other);
        return new EqualsBuilder().append(variables, rhs.variables).append(surveyDate, rhs.surveyDate).append(positions, rhs.positions).append(orderIds, rhs.orderIds).append(disabledPositionChange, rhs.disabledPositionChange).append(deliveryDate, rhs.deliveryDate).append(workflowId, rhs.workflowId).isEquals();
    }

}
