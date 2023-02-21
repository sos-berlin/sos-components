
package com.sos.inventory.model.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * positions
 * <p>
 * start and end position
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "startPosition",
    "endPositions"
})
public class OrderPositions {

    @JsonProperty("startPosition")
    private Object startPosition;
    @JsonProperty("endPositions")
    private List<Object> endPositions = null;

    @SuppressWarnings("unchecked")
    @JsonProperty("startPosition")
    public Object getStartPosition() {
        if (startPosition != null) {
            if (startPosition instanceof String && ((String) startPosition).isEmpty()) {
                return null;
            } else if (startPosition instanceof List<?> && ((List<Object>) startPosition).isEmpty()) {
                return null;
            }
        }
        return startPosition;
    }

    @JsonProperty("startPosition")
    public void setStartPosition(Object startPosition) {
        this.startPosition = startPosition;
    }

    @JsonProperty("endPositions")
    public List<Object> getEndPositions() {
        return endPositions;
    }

    @JsonProperty("endPositions")
    public void setEndPositions(List<Object> endPositions) {
        this.endPositions = endPositions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("startPosition", startPosition).append("endPositions", endPositions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(startPosition).append(endPositions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OrderPositions) == false) {
            return false;
        }
        OrderPositions rhs = ((OrderPositions) other);
        return new EqualsBuilder().append(startPosition, rhs.startPosition).append(endPositions, rhs.endPositions).isEquals();
    }

}
