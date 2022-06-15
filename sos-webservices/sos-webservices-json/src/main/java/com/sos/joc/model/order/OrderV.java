
package com.sos.joc.model.order;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.common.Outcome;
import com.sos.controller.model.order.OrderAttachedState;
import com.sos.controller.model.order.OrderCycleState;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
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
    "marked",
    "attachedState",
    "agentId",
    "subagentId",
    "cycleState",
    "position",
    "positionString",
    "positionIsImplicitEnd",
    "endPosition",
    "endPositionString",
    "scheduledFor",
    "scheduledNever",
    "question",
    "lastOutcome",
    "historicOutcome",
    "requirements",
    "cyclicOrder"
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
     * order state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private OrderState state;
    /**
     * order state
     * <p>
     * 
     * 
     */
    @JsonProperty("marked")
    private OrderMark marked;
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
    @JsonProperty("subagentId")
    private String subagentId;
    /**
     * OrderCycleState
     * <p>
     * set if state == BetweenCycles or processing inside a cycle
     * 
     */
    @JsonProperty("cycleState")
    @JsonPropertyDescription("set if state == BetweenCycles or processing inside a cycle")
    private OrderCycleState cycleState;
    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> position = null;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("positionString")
    private String positionString;
    @JsonProperty("positionIsImplicitEnd")
    private Boolean positionIsImplicitEnd;
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
    @JsonProperty("endPositionString")
    private String endPositionString;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("scheduledFor")
    private Long scheduledFor;
    /**
     * deprecated -> is State.PENDING
     * 
     */
    @JsonProperty("scheduledNever")
    @JsonPropertyDescription("deprecated -> is State.PENDING")
    private Boolean scheduledNever = false;
    /**
     * only relevant for state PROMPTING
     * 
     */
    @JsonProperty("question")
    @JsonPropertyDescription("only relevant for state PROMPTING")
    private String question;
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
    private List<HistoricOutcome> historicOutcome = null;
    /**
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("requirements")
    @JsonAlias({
        "orderRequirements"
    })
    private Requirements requirements;
    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cyclicOrder")
    private CyclicOrderInfos cyclicOrder;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderV() {
    }

    /**
     * 
     * @param marked
     * @param attachedState
     * @param agentId
     * @param scheduledNever
     * @param requirements
     * @param surveyDate
     * @param question
     * @param orderId
     * @param lastOutcome
     * @param positionIsImplicitEnd
     * @param historicOutcome
     * @param positionString
     * @param endPosition
     * @param endPositionString
     * @param scheduledFor
     * @param arguments
     * @param subagentId
     * @param state
     * @param position
     * @param deliveryDate
     * @param workflowId
     * @param cycleState
     * @param cyclicOrder
     */
    public OrderV(Date deliveryDate, Date surveyDate, String orderId, Variables arguments, WorkflowId workflowId, OrderState state, OrderMark marked, OrderAttachedState attachedState, String agentId, String subagentId, OrderCycleState cycleState, List<Object> position, String positionString, Boolean positionIsImplicitEnd, List<Object> endPosition, String endPositionString, Long scheduledFor, Boolean scheduledNever, String question, Outcome lastOutcome, List<HistoricOutcome> historicOutcome, Requirements requirements, CyclicOrderInfos cyclicOrder) {
        super();
        this.deliveryDate = deliveryDate;
        this.surveyDate = surveyDate;
        this.orderId = orderId;
        this.arguments = arguments;
        this.workflowId = workflowId;
        this.state = state;
        this.marked = marked;
        this.attachedState = attachedState;
        this.agentId = agentId;
        this.subagentId = subagentId;
        this.cycleState = cycleState;
        this.position = position;
        this.positionString = positionString;
        this.positionIsImplicitEnd = positionIsImplicitEnd;
        this.endPosition = endPosition;
        this.endPositionString = endPositionString;
        this.scheduledFor = scheduledFor;
        this.scheduledNever = scheduledNever;
        this.question = question;
        this.lastOutcome = lastOutcome;
        this.historicOutcome = historicOutcome;
        this.requirements = requirements;
        this.cyclicOrder = cyclicOrder;
    }

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
     * order state
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
     * order state
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
     * order state
     * <p>
     * 
     * 
     */
    @JsonProperty("marked")
    public OrderMark getMarked() {
        return marked;
    }

    /**
     * order state
     * <p>
     * 
     * 
     */
    @JsonProperty("marked")
    public void setMarked(OrderMark marked) {
        this.marked = marked;
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

    @JsonProperty("subagentId")
    public String getSubagentId() {
        return subagentId;
    }

    @JsonProperty("subagentId")
    public void setSubagentId(String subagentId) {
        this.subagentId = subagentId;
    }

    /**
     * OrderCycleState
     * <p>
     * set if state == BetweenCycles or processing inside a cycle
     * 
     */
    @JsonProperty("cycleState")
    public OrderCycleState getCycleState() {
        return cycleState;
    }

    /**
     * OrderCycleState
     * <p>
     * set if state == BetweenCycles or processing inside a cycle
     * 
     */
    @JsonProperty("cycleState")
    public void setCycleState(OrderCycleState cycleState) {
        this.cycleState = cycleState;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("positionString")
    public String getPositionString() {
        return positionString;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("positionString")
    public void setPositionString(String positionString) {
        this.positionString = positionString;
    }

    @JsonProperty("positionIsImplicitEnd")
    public Boolean getPositionIsImplicitEnd() {
        return positionIsImplicitEnd;
    }

    @JsonProperty("positionIsImplicitEnd")
    public void setPositionIsImplicitEnd(Boolean positionIsImplicitEnd) {
        this.positionIsImplicitEnd = positionIsImplicitEnd;
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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("endPositionString")
    public String getEndPositionString() {
        return endPositionString;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("endPositionString")
    public void setEndPositionString(String endPositionString) {
        this.endPositionString = endPositionString;
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
     * deprecated -> is State.PENDING
     * 
     */
    @JsonProperty("scheduledNever")
    public Boolean getScheduledNever() {
        return scheduledNever;
    }

    /**
     * deprecated -> is State.PENDING
     * 
     */
    @JsonProperty("scheduledNever")
    public void setScheduledNever(Boolean scheduledNever) {
        this.scheduledNever = scheduledNever;
    }

    /**
     * only relevant for state PROMPTING
     * 
     */
    @JsonProperty("question")
    public String getQuestion() {
        return question;
    }

    /**
     * only relevant for state PROMPTING
     * 
     */
    @JsonProperty("question")
    public void setQuestion(String question) {
        this.question = question;
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

    /**
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("requirements")
    public Requirements getRequirements() {
        return requirements;
    }

    /**
     * order or job requirements
     * <p>
     * 
     * 
     */
    @JsonProperty("requirements")
    public void setRequirements(Requirements requirements) {
        this.requirements = requirements;
    }

    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cyclicOrder")
    public CyclicOrderInfos getCyclicOrder() {
        return cyclicOrder;
    }

    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cyclicOrder")
    public void setCyclicOrder(CyclicOrderInfos cyclicOrder) {
        this.cyclicOrder = cyclicOrder;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("orderId", orderId).append("arguments", arguments).append("workflowId", workflowId).append("state", state).append("marked", marked).append("attachedState", attachedState).append("agentId", agentId).append("subagentId", subagentId).append("cycleState", cycleState).append("position", position).append("positionString", positionString).append("positionIsImplicitEnd", positionIsImplicitEnd).append("endPosition", endPosition).append("endPositionString", endPositionString).append("scheduledFor", scheduledFor).append("scheduledNever", scheduledNever).append("question", question).append("lastOutcome", lastOutcome).append("historicOutcome", historicOutcome).append("requirements", requirements).append("cyclicOrder", cyclicOrder).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(marked).append(attachedState).append(agentId).append(scheduledNever).append(requirements).append(surveyDate).append(question).append(orderId).append(lastOutcome).append(positionIsImplicitEnd).append(historicOutcome).append(positionString).append(endPosition).append(endPositionString).append(scheduledFor).append(arguments).append(subagentId).append(state).append(position).append(deliveryDate).append(workflowId).append(cycleState).append(cyclicOrder).toHashCode();
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
        return new EqualsBuilder().append(marked, rhs.marked).append(attachedState, rhs.attachedState).append(agentId, rhs.agentId).append(scheduledNever, rhs.scheduledNever).append(requirements, rhs.requirements).append(surveyDate, rhs.surveyDate).append(question, rhs.question).append(orderId, rhs.orderId).append(lastOutcome, rhs.lastOutcome).append(positionIsImplicitEnd, rhs.positionIsImplicitEnd).append(historicOutcome, rhs.historicOutcome).append(positionString, rhs.positionString).append(endPosition, rhs.endPosition).append(endPositionString, rhs.endPositionString).append(scheduledFor, rhs.scheduledFor).append(arguments, rhs.arguments).append(subagentId, rhs.subagentId).append(state, rhs.state).append(position, rhs.position).append(deliveryDate, rhs.deliveryDate).append(workflowId, rhs.workflowId).append(cycleState, rhs.cycleState).append(cyclicOrder, rhs.cyclicOrder).isEquals();
    }

}
