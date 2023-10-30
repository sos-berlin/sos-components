
package com.sos.joc.model.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.order.OrdersSummary;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * workflows order count
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "surveyDate",
    "workflows",
    "numOfAllOrders"
})
public class WorkflowsOrderCount {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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
    @JsonProperty("workflows")
    private List<WorkflowOrderCount> workflows = new ArrayList<WorkflowOrderCount>();
    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAllOrders")
    private OrdersSummary numOfAllOrders;

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
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

    @JsonProperty("workflows")
    public List<WorkflowOrderCount> getWorkflows() {
        return workflows;
    }

    @JsonProperty("workflows")
    public void setWorkflows(List<WorkflowOrderCount> workflows) {
        this.workflows = workflows;
    }

    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAllOrders")
    public OrdersSummary getNumOfAllOrders() {
        return numOfAllOrders;
    }

    /**
     * order summary
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfAllOrders")
    public void setNumOfAllOrders(OrdersSummary numOfAllOrders) {
        this.numOfAllOrders = numOfAllOrders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("workflows", workflows).append("numOfAllOrders", numOfAllOrders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(workflows).append(deliveryDate).append(surveyDate).append(numOfAllOrders).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowsOrderCount) == false) {
            return false;
        }
        WorkflowsOrderCount rhs = ((WorkflowsOrderCount) other);
        return new EqualsBuilder().append(workflows, rhs.workflows).append(deliveryDate, rhs.deliveryDate).append(surveyDate, rhs.surveyDate).append(numOfAllOrders, rhs.numOfAllOrders).isEquals();
    }

}
