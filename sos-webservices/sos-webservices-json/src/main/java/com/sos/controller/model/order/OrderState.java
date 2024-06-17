
package com.sos.controller.model.order;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * OrderState
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "expected",
    "cycleState",
    "until",
    "subagentId"
})
public class OrderState {

    @JsonProperty("TYPE")
    private String tYPE;
    /**
     * set if state == ExpectingNotices
     * 
     */
    @JsonProperty("expected")
    @JsonPropertyDescription("set if state == ExpectingNotices")
    private List<ExpectedNotice> expected = null;
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
     * set if state == DelayedAfterError
     * 
     */
    @JsonProperty("until")
    @JsonPropertyDescription("set if state == DelayedAfterError")
    private Date until = null;
    
    @JsonProperty("subagentId")
    private String subagentId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderState() {
    }

    /**
     * 
     * @param expected
     * @param subagentId
     * @param tYPE
     * @param cycleState
     * @param until
     */
    public OrderState(String tYPE, List<ExpectedNotice> expected, OrderCycleState cycleState, Date until, String subagentId) {
        super();
        this.tYPE = tYPE;
        this.expected = expected;
        this.cycleState = cycleState;
        this.until = until;
        this.subagentId = subagentId;
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
     * set if state == ExpectingNotices
     * 
     */
    @JsonProperty("expected")
    public List<ExpectedNotice> getExpected() {
        return expected;
    }

    /**
     * set if state == ExpectingNotices
     * 
     */
    @JsonProperty("expected")
    public void setExpected(List<ExpectedNotice> expected) {
        this.expected = expected;
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
     * set if state == DelayedAfterError
     * 
     */
    @JsonProperty("until")
    public Date getUntil() {
        return until;
    }

    /**
     * set if state == DelayedAfterError
     * 
     */
    @JsonProperty("until")
    public void setUntil(Date until) {
        this.until = until;
    }

    @JsonProperty("subagentId")
    public String getSubagentId() {
        return subagentId;
    }

    @JsonProperty("subagentId")
    public void setSubagentId(String subagentId) {
        this.subagentId = subagentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("expected", expected).append("cycleState", cycleState).append("until", until).append("subagentId", subagentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(subagentId).append(tYPE).append(cycleState).append(until).append(expected).toHashCode();
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
        return new EqualsBuilder().append(subagentId, rhs.subagentId).append(tYPE, rhs.tYPE).append(cycleState, rhs.cycleState).append(until, rhs.until).append(expected, rhs.expected).isEquals();
    }

}
