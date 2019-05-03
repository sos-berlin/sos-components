
package com.sos.jobscheduler.model.order;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * orderIdsList
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "eventId",
    "array"
})
public class OrderIdsList {

    @JsonProperty("eventId")
    private String eventId;
    @JsonProperty("array")
    private List<String> array = null;

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @JsonProperty("array")
    public List<String> getArray() {
        return array;
    }

    @JsonProperty("array")
    public void setArray(List<String> array) {
        this.array = array;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("eventId", eventId).append("array", array).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(eventId).append(array).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderIdsList) == false) {
            return false;
        }
        OrderIdsList rhs = ((OrderIdsList) other);
        return new EqualsBuilder().append(eventId, rhs.eventId).append(array, rhs.array).isEquals();
    }

}
