
package com.sos.controller.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderState
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "noticeId",
    "cycleState"
})
public class OrderState {

    @JsonProperty("TYPE")
    private String tYPE;
    /**
     * set if state == ExpectingNotice
     * 
     */
    @JsonProperty("noticeId")
    @JsonPropertyDescription("set if state == ExpectingNotice")
    private String noticeId;
    /**
     * OrderCycleState
     * <p>
     * set if state == BetweenCycles
     * 
     */
    @JsonProperty("cycleState")
    @JsonPropertyDescription("set if state == BetweenCycles")
    private OrderCycleState cycleState;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderState() {
    }

    /**
     * 
     * @param tYPE
     * @param noticeId
     * @param cycleState
     */
    public OrderState(String tYPE, String noticeId, OrderCycleState cycleState) {
        super();
        this.tYPE = tYPE;
        this.noticeId = noticeId;
        this.cycleState = cycleState;
    }

    @JsonProperty("TYPE")
    public String getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    public void setTYPE(String tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * set if state == ExpectingNotice
     * 
     */
    @JsonProperty("noticeId")
    public String getNoticeId() {
        return noticeId;
    }

    /**
     * set if state == ExpectingNotice
     * 
     */
    @JsonProperty("noticeId")
    public void setNoticeId(String noticeId) {
        this.noticeId = noticeId;
    }

    /**
     * OrderCycleState
     * <p>
     * set if state == BetweenCycles
     * 
     */
    @JsonProperty("cycleState")
    public OrderCycleState getCycleState() {
        return cycleState;
    }

    /**
     * OrderCycleState
     * <p>
     * set if state == BetweenCycles
     * 
     */
    @JsonProperty("cycleState")
    public void setCycleState(OrderCycleState cycleState) {
        this.cycleState = cycleState;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("noticeId", noticeId).append("cycleState", cycleState).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(noticeId).append(cycleState).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderState) == false) {
            return false;
        }
        OrderState rhs = ((OrderState) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(noticeId, rhs.noticeId).append(cycleState, rhs.cycleState).isEquals();
    }

}
