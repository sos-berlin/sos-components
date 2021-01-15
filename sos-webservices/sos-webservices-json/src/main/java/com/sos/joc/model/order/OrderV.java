
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.jobscheduler.model.common.Outcome;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.order.OrderAttachedState;
import com.sos.jobscheduler.model.workflow.HistoricOutcome;
import com.sos.jobscheduler.model.workflow.WorkflowId;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order with delivery date (volatile part)
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "surveyDate",
    "orderId",
    "arguments",
    "workflowId",
    "state",
    "attachedState",
    "agentId",
    "position",
    "scheduledFor",
    "lastOutcome",
    "historicOutcome"
})
public class OrderV {

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
    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    @JsonPropertyDescription("a map for arbitrary key-value pairs")
    private Variables arguments;
    /**
     * workflowId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("workflowId")
    private WorkflowId workflowId;
    /**
     * jobChain state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private OrderState state;
    /**
     * OrderAttachedState
     * <p>
     * 
     * 
     */
    @JsonProperty("attachedState")
    private OrderAttachedState attachedState;
    @JsonProperty("agentId")
    private String agentId;
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> position = new ArrayList<Object>();
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledFor")
    private Long scheduledFor;
    /**
     * outcome
     * <p>
     * 
     * 
     */
    @JsonProperty("lastOutcome")
    private Outcome lastOutcome;
    /**
     * only for compact parameter is false
     * 
     */
    @JsonProperty("historicOutcome")
    @JsonPropertyDescription("only for compact parameter is false")
    private List<HistoricOutcome> historicOutcome = new ArrayList<HistoricOutcome>();

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

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public Variables getArguments() {
        return arguments;
    }

    /**
     * key-value pairs
     * <p>
     * a map for arbitrary key-value pairs
     * 
     */
    @JsonProperty("arguments")
    public void setArguments(Variables arguments) {
        this.arguments = arguments;
    }

    /**
     * workflowId
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("workflowId")
    public void setWorkflowId(WorkflowId workflowId) {
        this.workflowId = workflowId;
    }

    /**
     * jobChain state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public OrderState getState() {
        return state;
    }

    /**
     * jobChain state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(OrderState state) {
        this.state = state;
    }

    /**
     * OrderAttachedState
     * <p>
     * 
     * 
     */
    @JsonProperty("attachedState")
    public OrderAttachedState getAttachedState() {
        return attachedState;
    }

    /**
     * OrderAttachedState
     * <p>
     * 
     * 
     */
    @JsonProperty("attachedState")
    public void setAttachedState(OrderAttachedState attachedState) {
        this.attachedState = attachedState;
    }

    @JsonProperty("agentId")
    public String getAgentId() {
        return agentId;
    }

    @JsonProperty("agentId")
    public void setAgentId(String agentId) {
        this.agentId = agentId;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    public List<Object> getPosition() {
        return position;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    public void setPosition(List<Object> position) {
        this.position = position;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledFor")
    public Long getScheduledFor() {
        return scheduledFor;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledFor")
    public void setScheduledFor(Long scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    /**
     * outcome
     * <p>
     * 
     * 
     */
    @JsonProperty("lastOutcome")
    public Outcome getLastOutcome() {
        return lastOutcome;
    }

    /**
     * outcome
     * <p>
     * 
     * 
     */
    @JsonProperty("lastOutcome")
    public void setLastOutcome(Outcome lastOutcome) {
        this.lastOutcome = lastOutcome;
    }

    /**
     * only for compact parameter is false
     * 
     */
    @JsonProperty("historicOutcome")
    public List<HistoricOutcome> getHistoricOutcome() {
        return historicOutcome;
    }

    /**
     * only for compact parameter is false
     * 
     */
    @JsonProperty("historicOutcome")
    public void setHistoricOutcome(List<HistoricOutcome> historicOutcome) {
        this.historicOutcome = historicOutcome;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("orderId", orderId).append("arguments", arguments).append("workflowId", workflowId).append("state", state).append("attachedState", attachedState).append("agentId", agentId).append("position", position).append("scheduledFor", scheduledFor).append("lastOutcome", lastOutcome).append("historicOutcome", historicOutcome).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(attachedState).append(agentId).append(surveyDate).append(orderId).append(lastOutcome).append(historicOutcome).append(scheduledFor).append(arguments).append(state).append(position).append(deliveryDate).append(workflowId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderV) == false) {
            return false;
        }
        OrderV rhs = ((OrderV) other);
        return new EqualsBuilder().append(attachedState, rhs.attachedState).append(agentId, rhs.agentId).append(surveyDate, rhs.surveyDate).append(orderId, rhs.orderId).append(lastOutcome, rhs.lastOutcome).append(historicOutcome, rhs.historicOutcome).append(scheduledFor, rhs.scheduledFor).append(arguments, rhs.arguments).append(state, rhs.state).append(position, rhs.position).append(deliveryDate, rhs.deliveryDate).append(workflowId, rhs.workflowId).isEquals();
    }

}
