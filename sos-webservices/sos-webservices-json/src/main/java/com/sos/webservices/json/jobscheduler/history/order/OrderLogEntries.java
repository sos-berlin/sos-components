
package com.sos.webservices.json.jobscheduler.history.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order history log entries
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "entries"
})
public class OrderLogEntries {

    @JsonProperty("entries")
    private List<OrderLogEntry> entries = new ArrayList<OrderLogEntry>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public OrderLogEntries() {
    }

    /**
     * 
     * @param entries
     */
    public OrderLogEntries(List<OrderLogEntry> entries) {
        super();
        this.entries = entries;
    }

    @JsonProperty("entries")
    public List<OrderLogEntry> getEntries() {
        return entries;
    }

    @JsonProperty("entries")
    public void setEntries(List<OrderLogEntry> entries) {
        this.entries = entries;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("entries", entries).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(entries).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderLogEntries) == false) {
            return false;
        }
        OrderLogEntries rhs = ((OrderLogEntries) other);
        return new EqualsBuilder().append(entries, rhs.entries).isEquals();
    }

}
