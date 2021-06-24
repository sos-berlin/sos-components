
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
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
    "logEvents"
})
public class RunningOrderLogEvents
    extends OrderRunningLogFilter
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("complete")
    private Boolean complete = false;
    @JsonProperty("logEvents")
    private List<OrderLogEntry> logEvents = new ArrayList<OrderLogEntry>();

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

    @JsonProperty("logEvents")
    public List<OrderLogEntry> getLogEvents() {
        return logEvents;
    }

    @JsonProperty("logEvents")
    public void setLogEvents(List<OrderLogEntry> logEvents) {
        this.logEvents = logEvents;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("complete", complete).append("logEvents", logEvents).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(complete).append(logEvents).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RunningOrderLogEvents) == false) {
            return false;
        }
        RunningOrderLogEvents rhs = ((RunningOrderLogEvents) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(complete, rhs.complete).append(logEvents, rhs.logEvents).isEquals();
    }

}
