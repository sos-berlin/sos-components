
package com.sos.joc.model.plan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.controller.model.board.Board;
import com.sos.joc.model.order.OrderV;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Plan
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "planId",
    "numOfOrders",
    "orders",
    "closed",
    "numOfNoticeBoards",
    "noticeBoards"
})
public class Plan {

    /**
     * PlanId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("planId")
    private PlanId planId;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    private Integer numOfOrders;
    @JsonProperty("orders")
    @JsonDeserialize(as = java.util.HashSet.class)
    private Collection<OrderV> orders = null;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("closed")
    private Boolean closed = false;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNoticeBoards")
    private Integer numOfNoticeBoards;
    @JsonProperty("noticeBoards")
    private List<Board> noticeBoards = new ArrayList<Board>();

    /**
     * PlanId
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("planId")
    public void setPlanId(PlanId planId) {
        this.planId = planId;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    public Integer getNumOfOrders() {
        return numOfOrders;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfOrders")
    public void setNumOfOrders(Integer numOfOrders) {
        this.numOfOrders = numOfOrders;
    }

    @JsonProperty("orders")
    public Collection<OrderV> getOrders() {
        return orders;
    }

    @JsonProperty("orders")
    public void setOrders(Collection<OrderV> orders) {
        this.orders = orders;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("closed")
    public Boolean getClosed() {
        return closed;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("closed")
    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNoticeBoards")
    public Integer getNumOfNoticeBoards() {
        return numOfNoticeBoards;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("numOfNoticeBoards")
    public void setNumOfNoticeBoards(Integer numOfNoticeBoards) {
        this.numOfNoticeBoards = numOfNoticeBoards;
    }

    @JsonProperty("noticeBoards")
    public List<Board> getNoticeBoards() {
        return noticeBoards;
    }

    @JsonProperty("noticeBoards")
    public void setNoticeBoards(List<Board> noticeBoards) {
        this.noticeBoards = noticeBoards;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("planId", planId).append("numOfOrders", numOfOrders).append("orders", orders).append("closed", closed).append("numOfNoticeBoards", numOfNoticeBoards).append("noticeBoards", noticeBoards).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(numOfNoticeBoards).append(numOfOrders).append(closed).append(planId).append(orders).append(noticeBoards).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Plan) == false) {
            return false;
        }
        Plan rhs = ((Plan) other);
        return new EqualsBuilder().append(numOfNoticeBoards, rhs.numOfNoticeBoards).append(numOfOrders, rhs.numOfOrders).append(closed, rhs.closed).append(planId, rhs.planId).append(orders, rhs.orders).append(noticeBoards, rhs.noticeBoards).isEquals();
    }

}
