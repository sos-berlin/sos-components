
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.model.common.HistoryState;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * order object in history collection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "surveyDate",
    "controllerId",
    "orderId",
    "tags",
    "workflow",
    "startTime",
    "plannedTime",
    "endTime",
    "position",
    "sequence",
    "state",
    "orderState",
    "historyId",
    "children",
    "arguments"
})
public class OrderHistoryItem {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date surveyDate;
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    /**
     * tags
     * <p>
     * 
     * 
     */
    @JsonProperty("tags")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<String> tags = new LinkedHashSet<String>();
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    @JsonPropertyDescription("absolute path of an object.")
    private String workflow;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date startTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("plannedTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date plannedTime;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date endTime;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    private String position;
    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequence")
    private Integer sequence;
    /**
     * orderHistory status
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private HistoryState state;
    /**
     * order state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderState")
    private OrderState orderState;
    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    private Long historyId;
    @JsonProperty("children")
    private List<OrderHistoryItem> children = new ArrayList<OrderHistoryItem>();
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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("surveyDate")
    public void setSurveyDate(Date surveyDate) {
        this.surveyDate = surveyDate;
    }

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
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
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public String getWorkflow() {
        return workflow;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("workflow")
    public void setWorkflow(String workflow) {
        this.workflow = workflow;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    public Date getStartTime() {
        return startTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("startTime")
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("plannedTime")
    public Date getPlannedTime() {
        return plannedTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * (Required)
     * 
     */
    @JsonProperty("plannedTime")
    public void setPlannedTime(Date plannedTime) {
        this.plannedTime = plannedTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    public Date getEndTime() {
        return endTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("endTime")
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public String getPosition() {
        return position;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("position")
    public void setPosition(String position) {
        this.position = position;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequence")
    public Integer getSequence() {
        return sequence;
    }

    /**
     * non negative integer
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("sequence")
    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    /**
     * orderHistory status
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public HistoryState getState() {
        return state;
    }

    /**
     * orderHistory status
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(HistoryState state) {
        this.state = state;
    }

    /**
     * order state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderState")
    public OrderState getOrderState() {
        return orderState;
    }

    /**
     * order state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("orderState")
    public void setOrderState(OrderState orderState) {
        this.orderState = orderState;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    public Long getHistoryId() {
        return historyId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    @JsonProperty("children")
    public List<OrderHistoryItem> getChildren() {
        return children;
    }

    @JsonProperty("children")
    public void setChildren(List<OrderHistoryItem> children) {
        this.children = children;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("surveyDate", surveyDate).append("controllerId", controllerId).append("orderId", orderId).append("tags", tags).append("workflow", workflow).append("startTime", startTime).append("plannedTime", plannedTime).append("endTime", endTime).append("position", position).append("sequence", sequence).append("state", state).append("orderState", orderState).append("historyId", historyId).append("children", children).append("arguments", arguments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(surveyDate).append(controllerId).append(workflow).append(orderId).append(tags).append(orderState).append(sequence).append(children).append(historyId).append(plannedTime).append(startTime).append(arguments).append(endTime).append(position).append(state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderHistoryItem) == false) {
            return false;
        }
        OrderHistoryItem rhs = ((OrderHistoryItem) other);
        return new EqualsBuilder().append(surveyDate, rhs.surveyDate).append(controllerId, rhs.controllerId).append(workflow, rhs.workflow).append(orderId, rhs.orderId).append(tags, rhs.tags).append(orderState, rhs.orderState).append(sequence, rhs.sequence).append(children, rhs.children).append(historyId, rhs.historyId).append(plannedTime, rhs.plannedTime).append(startTime, rhs.startTime).append(arguments, rhs.arguments).append(endTime, rhs.endTime).append(position, rhs.position).append(state, rhs.state).isEquals();
    }

}
