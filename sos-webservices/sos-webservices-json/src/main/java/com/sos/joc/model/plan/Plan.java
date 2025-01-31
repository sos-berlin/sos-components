
package com.sos.joc.model.plan;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.board.Board;
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
    "orderIds",
    "closed",
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
    @JsonProperty("orderIds")
    private List<String> orderIds = new ArrayList<String>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("closed")
    private Boolean closed = false;
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

    @JsonProperty("orderIds")
    public List<String> getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(List<String> orderIds) {
        this.orderIds = orderIds;
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
        return new ToStringBuilder(this).append("planId", planId).append("orderIds", orderIds).append("closed", closed).append("noticeBoards", noticeBoards).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(closed).append(planId).append(orderIds).append(noticeBoards).toHashCode();
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
        return new EqualsBuilder().append(closed, rhs.closed).append(planId, rhs.planId).append(orderIds, rhs.orderIds).append(noticeBoards, rhs.noticeBoards).isEquals();
    }

}
