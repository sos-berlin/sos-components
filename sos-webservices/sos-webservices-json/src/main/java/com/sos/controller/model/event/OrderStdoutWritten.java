
package com.sos.controller.model.event;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * OrderStdoutWritten event
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "chunk"
})
public class OrderStdoutWritten
    extends Event
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("chunk")
    private String chunk;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderStdoutWritten() {
    }

    /**
     * 
     * @param eventId
     * @param chunk
     * 
     */
    public OrderStdoutWritten(String chunk, Long eventId) {
        super(eventId);
        this.chunk = chunk;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("chunk")
    public String getChunk() {
        return chunk;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("chunk")
    public void setChunk(String chunk) {
        this.chunk = chunk;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("chunk", chunk).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(chunk).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderStdoutWritten) == false) {
            return false;
        }
        OrderStdoutWritten rhs = ((OrderStdoutWritten) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(chunk, rhs.chunk).isEquals();
    }

}
