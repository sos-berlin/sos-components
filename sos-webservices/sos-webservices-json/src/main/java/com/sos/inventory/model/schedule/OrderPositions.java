
package com.sos.inventory.model.schedule;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> startPosition = null;
    @JsonProperty("endPositions")
    private List<List<Object>> endPositions = null;

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    public List<Object> getStartPosition() {
        return startPosition;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("startPosition")
    public void setStartPosition(List<Object> startPosition) {
        this.startPosition = startPosition;
    }

    @JsonProperty("endPositions")
    public List<List<Object>> getEndPositions() {
        return endPositions;
    }

    @JsonProperty("endPositions")
    public void setEndPositions(List<List<Object>> endPositions) {
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
