
package com.sos.joc.model.history.order.moved;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.history.order.OrderLogEntryInstruction;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Moved Skipped
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "instruction",
    "reason"
})
public class MovedSkipped {

    /**
     * order history log entry
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("instruction")
    private OrderLogEntryInstruction instruction;
    /**
     * Moved Skip Reason
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("reason")
    private MovedSkippedReason reason;

    /**
     * order history log entry
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("instruction")
    public OrderLogEntryInstruction getInstruction() {
        return instruction;
    }

    /**
     * order history log entry
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("instruction")
    public void setInstruction(OrderLogEntryInstruction instruction) {
        this.instruction = instruction;
    }

    /**
     * Moved Skip Reason
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("reason")
    public MovedSkippedReason getReason() {
        return reason;
    }

    /**
     * Moved Skip Reason
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("reason")
    public void setReason(MovedSkippedReason reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("instruction", instruction).append("reason", reason).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(reason).append(instruction).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MovedSkipped) == false) {
            return false;
        }
        MovedSkipped rhs = ((MovedSkipped) other);
        return new EqualsBuilder().append(reason, rhs.reason).append(instruction, rhs.instruction).isEquals();
    }

}
