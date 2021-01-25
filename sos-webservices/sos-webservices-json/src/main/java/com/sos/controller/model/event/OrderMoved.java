
package com.sos.controller.model.event;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderMoved event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "to"
})
public class OrderMoved
    extends Event
{

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * (Required)
     * 
     */
    @JsonProperty("to")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> to = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderMoved() {
    }

    /**
     * 
     * @param eventId
     * @param to
     * 
     */
    public OrderMoved(List<Object> to, Long eventId) {
        super(eventId);
        this.to = to;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * (Required)
     * 
     */
    @JsonProperty("to")
    public List<Object> getTo() {
        return to;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * (Required)
     * 
     */
    @JsonProperty("to")
    public void setTo(List<Object> to) {
        this.to = to;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("to", to).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(to).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderMoved) == false) {
            return false;
        }
        OrderMoved rhs = ((OrderMoved) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(to, rhs.to).isEquals();
    }

}
