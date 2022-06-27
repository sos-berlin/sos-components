
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
    "expected",
    "cycleState",
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
    private ExpectedNotices expected;
    /**
     * OrderCycleState
     * <p>
     * set if state == BetweenCycles or processing inside a cycle
     * 
     */
    @JsonProperty("cycleState")
    @JsonPropertyDescription("set if state == BetweenCycles or processing inside a cycle")
    private OrderCycleState cycleState;
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
     */
    public OrderState(String tYPE, ExpectedNotices expected, OrderCycleState cycleState, String subagentId) {
        super();
        this.tYPE = tYPE;
        this.expected = expected;
        this.cycleState = cycleState;
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
    public ExpectedNotices getExpected() {
        return expected;
    }

    /**
     * set if state == ExpectingNotices
     * 
     */
    @JsonProperty("expected")
    public void setExpected(ExpectedNotices expected) {
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
        return new ToStringBuilder(this).append("tYPE", tYPE).append("expected", expected).append("cycleState", cycleState).append("subagentId", subagentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(subagentId).append(tYPE).append(cycleState).append(expected).toHashCode();
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
        return new EqualsBuilder().append(subagentId, rhs.subagentId).append(tYPE, rhs.tYPE).append(cycleState, rhs.cycleState).append(expected, rhs.expected).isEquals();
    }

}
