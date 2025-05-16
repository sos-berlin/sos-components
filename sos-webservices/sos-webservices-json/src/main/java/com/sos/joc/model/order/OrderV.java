
package com.sos.joc.model.order;

import java.util.Date;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.common.Outcome;
import com.sos.controller.model.order.ExpectedNotice;
import com.sos.controller.model.order.OrderAttachedState;
import com.sos.controller.model.order.OrderCycleState;
import com.sos.controller.model.order.OrderRetryState;
import com.sos.controller.model.order.OrderSleepState;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.workflow.Requirements;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.plan.PlanId;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    "planId",
    "cycleState",
    "retryState",
    "sleepState",
    "expectedNotices",
    "label",
    "tags",
    "position",
    "positionString",
    "positionIsImplicitEnd",
    "endPositions",
    "scheduledFor",
    "scheduledNever",
    "question",
    "lastOutcome",
    "historicOutcome",
    "requirements",
    "cyclicOrder",
    "hasChildOrders",
    "canLeave",
    "isContinuable",
    "isResumable",
    "isSuspendible",
    "priority"
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
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    private PlanId planId;
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
     * OrderRetryState
     * <p>
     * set if state == DelayedAfterError
     * 
     */
    @JsonProperty("retryState")
    @JsonPropertyDescription("set if state == DelayedAfterError")
    private OrderRetryState retryState;
    /**
     * OrderSleepState
     * <p>
     * set if state == OrderSleeping
     * 
     */
    @JsonProperty("sleepState")
    @JsonPropertyDescription("set if state == OrderSleeping")
    private OrderSleepState sleepState;
    /**
     * if state._reason == EXPECTING_NOTICES
     * 
     */
    @JsonProperty("expectedNotices")
    @JsonPropertyDescription("if state._reason == EXPECTING_NOTICES")
    private List<ExpectedNotice> expectedNotices = null;
    /**
     * a label is only in the response if the request restricts the orders to one workflow
     * 
     */
    @JsonProperty("label")
    @JsonPropertyDescription("a label is only in the response if the request restricts the orders to one workflow")
    private String label;
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> tags = null;
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
     * positions or labels
     * <p>
     * 
     * 
     */
    @JsonProperty("endPositions")
    private List<Object> endPositions = null;
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
     * OrderPreparation
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
    @JsonProperty("hasChildOrders")
    private Boolean hasChildOrders = false;
    /**
     * only relevant for state COMPLETED
     * 
     */
    @JsonProperty("canLeave")
    @JsonPropertyDescription("only relevant for state COMPLETED")
    private Boolean canLeave;
    @JsonProperty("isContinuable")
    private Boolean isContinuable;
    @JsonProperty("isResumable")
    private Boolean isResumable;
    @JsonProperty("isSuspendible")
    private Boolean isSuspendible;
    @JsonProperty("priority")
    private Integer priority = 0;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderV() {
    }

    /**
     * 
     * @param attachedState
     * @param expectedNotices
     * @param agentId
     * @param scheduledNever
     * @param orderId
     * @param lastOutcome
     * @param isResumable
     * @param retryState
     * @param historicOutcome
     * @param positionString
     * @param sleepState
     * @param scheduledFor
     * @param planId
     * @param state
     * @param deliveryDate
     * @param cycleState
     * @param cyclicOrder
     * @param marked
     * @param isContinuable
     * @param requirements
     * @param canLeave
     * @param surveyDate
     * @param question
     * @param hasChildOrders
     * @param positionIsImplicitEnd
     * @param label
     * @param priority
     * @param tags
     * @param endPositions
     * @param arguments
     * @param subagentId
     * @param position
     * @param isSuspendible
     * @param workflowId
     */
    public OrderV(Date deliveryDate, Date surveyDate, String orderId, Variables arguments, WorkflowId workflowId, OrderState state, OrderMark marked, OrderAttachedState attachedState, String agentId, String subagentId, PlanId planId, OrderCycleState cycleState, OrderRetryState retryState, OrderSleepState sleepState, List<ExpectedNotice> expectedNotices, String label, Set<String> tags, List<Object> position, String positionString, Boolean positionIsImplicitEnd, List<Object> endPositions, Long scheduledFor, Boolean scheduledNever, String question, Outcome lastOutcome, List<HistoricOutcome> historicOutcome, Requirements requirements, CyclicOrderInfos cyclicOrder, Boolean hasChildOrders, Boolean canLeave, Boolean isContinuable, Boolean isResumable, Boolean isSuspendible, Integer priority) {
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
        this.planId = planId;
        this.cycleState = cycleState;
        this.retryState = retryState;
        this.sleepState = sleepState;
        this.expectedNotices = expectedNotices;
        this.label = label;
        this.tags = tags;
        this.position = position;
        this.positionString = positionString;
        this.positionIsImplicitEnd = positionIsImplicitEnd;
        this.endPositions = endPositions;
        this.scheduledFor = scheduledFor;
        this.scheduledNever = scheduledNever;
        this.question = question;
        this.lastOutcome = lastOutcome;
        this.historicOutcome = historicOutcome;
        this.requirements = requirements;
        this.cyclicOrder = cyclicOrder;
        this.hasChildOrders = hasChildOrders;
        this.canLeave = canLeave;
        this.isContinuable = isContinuable;
        this.isResumable = isResumable;
        this.isSuspendible = isSuspendible;
        this.priority = priority;
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
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    public PlanId getPlanId() {
        return planId;
    }

    /**
     * PlanId
     * <p>
     * 
     * 
     */
    @JsonProperty("planId")
    public void setPlanId(PlanId planId) {
        this.planId = planId;
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
     * OrderRetryState
     * <p>
     * set if state == DelayedAfterError
     * 
     */
    @JsonProperty("retryState")
    public OrderRetryState getRetryState() {
        return retryState;
    }

    /**
     * OrderRetryState
     * <p>
     * set if state == DelayedAfterError
     * 
     */
    @JsonProperty("retryState")
    public void setRetryState(OrderRetryState retryState) {
        this.retryState = retryState;
    }

    /**
     * OrderSleepState
     * <p>
     * set if state == OrderSleeping
     * 
     */
    @JsonProperty("sleepState")
    public OrderSleepState getSleepState() {
        return sleepState;
    }

    /**
     * OrderSleepState
     * <p>
     * set if state == OrderSleeping
     * 
     */
    @JsonProperty("sleepState")
    public void setSleepState(OrderSleepState sleepState) {
        this.sleepState = sleepState;
    }

    /**
     * if state._reason == EXPECTING_NOTICES
     * 
     */
    @JsonProperty("expectedNotices")
    public List<ExpectedNotice> getExpectedNotices() {
        return expectedNotices;
    }

    /**
     * if state._reason == EXPECTING_NOTICES
     * 
     */
    @JsonProperty("expectedNotices")
    public void setExpectedNotices(List<ExpectedNotice> expectedNotices) {
        this.expectedNotices = expectedNotices;
    }

    /**
     * a label is only in the response if the request restricts the orders to one workflow
     * 
     */
    @JsonProperty("label")
    public String getLabel() {
        return label;
    }

    /**
     * a label is only in the response if the request restricts the orders to one workflow
     * 
     */
    @JsonProperty("label")
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("tags")
    public Set<String> getTags() {
        return tags;
    }

    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("tags")
    public void setTags(Set<String> tags) {
        this.tags = tags;
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
     * positions or labels
     * <p>
     * 
     * 
     */
    @JsonProperty("endPositions")
    public List<Object> getEndPositions() {
        return endPositions;
    }

    /**
     * positions or labels
     * <p>
     * 
     * 
     */
    @JsonProperty("endPositions")
    public void setEndPositions(List<Object> endPositions) {
        this.endPositions = endPositions;
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
     * OrderPreparation
     * <p>
     * 
     * 
     */
    @JsonProperty("requirements")
    public Requirements getRequirements() {
        return requirements;
    }

    /**
     * OrderPreparation
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

    @JsonProperty("hasChildOrders")
    public Boolean getHasChildOrders() {
        return hasChildOrders;
    }

    @JsonProperty("hasChildOrders")
    public void setHasChildOrders(Boolean hasChildOrders) {
        this.hasChildOrders = hasChildOrders;
    }

    /**
     * only relevant for state COMPLETED
     * 
     */
    @JsonProperty("canLeave")
    public Boolean getCanLeave() {
        return canLeave;
    }

    /**
     * only relevant for state COMPLETED
     * 
     */
    @JsonProperty("canLeave")
    public void setCanLeave(Boolean canLeave) {
        this.canLeave = canLeave;
    }

    @JsonProperty("isContinuable")
    public Boolean getIsContinuable() {
        return isContinuable;
    }

    @JsonProperty("isContinuable")
    public void setIsContinuable(Boolean isContinuable) {
        this.isContinuable = isContinuable;
    }

    @JsonProperty("isResumable")
    public Boolean getIsResumable() {
        return isResumable;
    }

    @JsonProperty("isResumable")
    public void setIsResumable(Boolean isResumable) {
        this.isResumable = isResumable;
    }

    @JsonProperty("isSuspendible")
    public Boolean getIsSuspendible() {
        return isSuspendible;
    }

    @JsonProperty("isSuspendible")
    public void setIsSuspendible(Boolean isSuspendible) {
        this.isSuspendible = isSuspendible;
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
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("orderId", orderId).append("arguments", arguments).append("workflowId", workflowId).append("state", state).append("marked", marked).append("attachedState", attachedState).append("agentId", agentId).append("subagentId", subagentId).append("planId", planId).append("cycleState", cycleState).append("retryState", retryState).append("sleepState", sleepState).append("expectedNotices", expectedNotices).append("label", label).append("tags", tags).append("position", position).append("positionString", positionString).append("positionIsImplicitEnd", positionIsImplicitEnd).append("endPositions", endPositions).append("scheduledFor", scheduledFor).append("scheduledNever", scheduledNever).append("question", question).append("lastOutcome", lastOutcome).append("historicOutcome", historicOutcome).append("requirements", requirements).append("cyclicOrder", cyclicOrder).append("hasChildOrders", hasChildOrders).append("canLeave", canLeave).append("isContinuable", isContinuable).append("isResumable", isResumable).append("isSuspendible", isSuspendible).append("priority", priority).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(attachedState).append(expectedNotices).append(agentId).append(scheduledNever).append(orderId).append(lastOutcome).append(isResumable).append(retryState).append(historicOutcome).append(positionString).append(sleepState).append(scheduledFor).append(planId).append(state).append(deliveryDate).append(cycleState).append(cyclicOrder).append(marked).append(isContinuable).append(requirements).append(canLeave).append(surveyDate).append(question).append(hasChildOrders).append(positionIsImplicitEnd).append(label).append(priority).append(tags).append(endPositions).append(arguments).append(subagentId).append(position).append(isSuspendible).append(workflowId).toHashCode();
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
        return new EqualsBuilder().append(attachedState, rhs.attachedState).append(expectedNotices, rhs.expectedNotices).append(agentId, rhs.agentId).append(scheduledNever, rhs.scheduledNever).append(orderId, rhs.orderId).append(lastOutcome, rhs.lastOutcome).append(isResumable, rhs.isResumable).append(retryState, rhs.retryState).append(historicOutcome, rhs.historicOutcome).append(positionString, rhs.positionString).append(sleepState, rhs.sleepState).append(scheduledFor, rhs.scheduledFor).append(planId, rhs.planId).append(state, rhs.state).append(deliveryDate, rhs.deliveryDate).append(cycleState, rhs.cycleState).append(cyclicOrder, rhs.cyclicOrder).append(marked, rhs.marked).append(isContinuable, rhs.isContinuable).append(requirements, rhs.requirements).append(canLeave, rhs.canLeave).append(surveyDate, rhs.surveyDate).append(question, rhs.question).append(hasChildOrders, rhs.hasChildOrders).append(positionIsImplicitEnd, rhs.positionIsImplicitEnd).append(label, rhs.label).append(priority, rhs.priority).append(tags, rhs.tags).append(endPositions, rhs.endPositions).append(arguments, rhs.arguments).append(subagentId, rhs.subagentId).append(position, rhs.position).append(isSuspendible, rhs.isSuspendible).append(workflowId, rhs.workflowId).isEquals();
    }

}
