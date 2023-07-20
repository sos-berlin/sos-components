
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
    "workflowId",
    "positions",
    "blockPositions"
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
    private Set<Position> positions = new LinkedHashSet<Position>();
    @JsonProperty("blockPositions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Position> blockPositions = new LinkedHashSet<Position>();

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
    public Set<Position> getPositions() {
        return positions;
    }

    @JsonProperty("positions")
    public void setPositions(Set<Position> positions) {
        this.positions = positions;
    }

    @JsonProperty("blockPositions")
    public Set<Position> getBlockPositions() {
        return blockPositions;
    }

    @JsonProperty("blockPositions")
    public void setBlockPositions(Set<Position> blockPositions) {
        this.blockPositions = blockPositions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("workflowId", workflowId).append("positions", positions).append("blockPositions", blockPositions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(positions).append(deliveryDate).append(surveyDate).append(workflowId).append(blockPositions).toHashCode();
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
        return new EqualsBuilder().append(positions, rhs.positions).append(deliveryDate, rhs.deliveryDate).append(surveyDate, rhs.surveyDate).append(workflowId, rhs.workflowId).append(blockPositions, rhs.blockPositions).isEquals();
    }

}
