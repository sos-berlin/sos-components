
package com.sos.joc.model.workflow;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.board.PlannedBoards;
import com.sos.controller.model.workflow.WorkflowBoardsV;
import com.sos.joc.model.order.OrderIdToOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * workflows with boards
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "surveyDate",
    "postingWorkflows",
    "expectingWorkflows",
    "consumingWorkflows",
    "noticeBoards",
    "orders"
})
public class WorkflowsBoardsV {

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
    @JsonProperty("postingWorkflows")
    private List<WorkflowBoardsV> postingWorkflows = new ArrayList<WorkflowBoardsV>();
    @JsonProperty("expectingWorkflows")
    private List<WorkflowBoardsV> expectingWorkflows = new ArrayList<WorkflowBoardsV>();
    @JsonProperty("consumingWorkflows")
    private List<WorkflowBoardsV> consumingWorkflows = new ArrayList<WorkflowBoardsV>();
    @JsonProperty("noticeBoards")
    private PlannedBoards noticeBoards;
    @JsonProperty("orders")
    private OrderIdToOrder orders;

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

    @JsonProperty("postingWorkflows")
    public List<WorkflowBoardsV> getPostingWorkflows() {
        return postingWorkflows;
    }

    @JsonProperty("postingWorkflows")
    public void setPostingWorkflows(List<WorkflowBoardsV> postingWorkflows) {
        this.postingWorkflows = postingWorkflows;
    }

    @JsonProperty("expectingWorkflows")
    public List<WorkflowBoardsV> getExpectingWorkflows() {
        return expectingWorkflows;
    }

    @JsonProperty("expectingWorkflows")
    public void setExpectingWorkflows(List<WorkflowBoardsV> expectingWorkflows) {
        this.expectingWorkflows = expectingWorkflows;
    }

    @JsonProperty("consumingWorkflows")
    public List<WorkflowBoardsV> getConsumingWorkflows() {
        return consumingWorkflows;
    }

    @JsonProperty("consumingWorkflows")
    public void setConsumingWorkflows(List<WorkflowBoardsV> consumingWorkflows) {
        this.consumingWorkflows = consumingWorkflows;
    }

    @JsonProperty("noticeBoards")
    public PlannedBoards getNoticeBoards() {
        return noticeBoards;
    }

    @JsonProperty("noticeBoards")
    public void setNoticeBoards(PlannedBoards noticeBoards) {
        this.noticeBoards = noticeBoards;
    }

    @JsonProperty("orders")
    public OrderIdToOrder getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(OrderIdToOrder orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("surveyDate", surveyDate).append("postingWorkflows", postingWorkflows).append("expectingWorkflows", expectingWorkflows).append("consumingWorkflows", consumingWorkflows).append("noticeBoards", noticeBoards).append("orders", orders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(surveyDate).append(postingWorkflows).append(expectingWorkflows).append(orders).append(deliveryDate).append(consumingWorkflows).append(noticeBoards).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowsBoardsV) == false) {
            return false;
        }
        WorkflowsBoardsV rhs = ((WorkflowsBoardsV) other);
        return new EqualsBuilder().append(surveyDate, rhs.surveyDate).append(postingWorkflows, rhs.postingWorkflows).append(expectingWorkflows, rhs.expectingWorkflows).append(orders, rhs.orders).append(deliveryDate, rhs.deliveryDate).append(consumingWorkflows, rhs.consumingWorkflows).append(noticeBoards, rhs.noticeBoards).isEquals();
    }

}
