
package com.sos.webservices.order.initiator.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Order Template Filter
 * <p>
 * The filter for the list of order template for scheduling orders to JobScheduler
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "selector"
})
public class ScheduleSelector {

    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the selector to get schedules
     * 
     */
    @JsonProperty("selector")
    @JsonPropertyDescription("Define the selector to get schedules")
    private SchedulesSelector selector;

    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the selector to get schedules
     * 
     */
    @JsonProperty("selector")
    public SchedulesSelector getSelector() {
        return selector;
    }

    /**
     * Daily Plan  Order Filter Definition
     * <p>
     * Define the selector to get schedules
     * 
     */
    @JsonProperty("selector")
    public void setSelector(SchedulesSelector selector) {
        this.selector = selector;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("selector", selector).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(selector).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ScheduleSelector) == false) {
            return false;
        }
        ScheduleSelector rhs = ((ScheduleSelector) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(selector, rhs.selector).isEquals();
    }

}
