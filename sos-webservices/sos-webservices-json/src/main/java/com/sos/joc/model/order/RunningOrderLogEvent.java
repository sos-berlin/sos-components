
package com.sos.joc.model.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.history.order.OrderLogEntry;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * running order log
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "complete",
    "logEvent"
})
public class RunningOrderLogEvent
    extends OrderRunningLogFilter
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    private Boolean complete = false;
    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("logEvent")
    private OrderLogEntry logEvent;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    public Boolean getComplete() {
        return complete;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    public void setComplete(Boolean complete) {
        this.complete = complete;
    }

    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("logEvent")
    public OrderLogEntry getLogEvent() {
        return logEvent;
    }

    /**
     * order history log entry
     * <p>
     * 
     * 
     */
    @JsonProperty("logEvent")
    public void setLogEvent(OrderLogEntry logEvent) {
        this.logEvent = logEvent;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("complete", complete).append("logEvent", logEvent).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(complete).append(logEvent).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunningOrderLogEvent) == false) {
            return false;
        }
        RunningOrderLogEvent rhs = ((RunningOrderLogEvent) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(complete, rhs.complete).append(logEvent, rhs.logEvent).isEquals();
    }

}
