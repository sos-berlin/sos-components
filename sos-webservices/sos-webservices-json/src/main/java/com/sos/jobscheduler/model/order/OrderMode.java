
package com.sos.jobscheduler.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "kill"
})
public class OrderMode {

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("TYPE")
    @JsonPropertyDescription("relevant for cancel or suspend order")
    private OrderModeType tYPE = OrderModeType.fromValue("FreshOrStarted");
    @JsonProperty("kill")
    private Kill kill;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderMode() {
    }

    /**
     * 
     * @param tYPE
     * @param kill
     */
    public OrderMode(OrderModeType tYPE, Kill kill) {
        super();
        this.tYPE = tYPE;
        this.kill = kill;
    }

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("TYPE")
    public OrderModeType getTYPE() {
        return tYPE;
    }

    /**
     * orderModeType
     * <p>
     * relevant for cancel or suspend order
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(OrderModeType tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("kill")
    public Kill getKill() {
        return kill;
    }

    @JsonProperty("kill")
    public void setKill(Kill kill) {
        this.kill = kill;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("kill", kill).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(tYPE).append(kill).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderMode) == false) {
            return false;
        }
        OrderMode rhs = ((OrderMode) other);
        return new EqualsBuilder().append(tYPE, rhs.tYPE).append(kill, rhs.kill).isEquals();
    }

}
