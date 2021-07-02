
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "position",
    "positionString"
})
public class Positions {

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    @JsonPropertyDescription("Actually, each even item is a string, each odd item is an integer")
    private List<Object> position = new ArrayList<Object>();
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("positionString")
    private String positionString;

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    public List<Object> getPosition() {
        return position;
    }

    /**
     * position
     * <p>
     * Actually, each even item is a string, each odd item is an integer
     * 
     */
    @JsonProperty("position")
    public void setPosition(List<Object> position) {
        this.position = position;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("positionString")
    public String getPositionString() {
        return positionString;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("positionString")
    public void setPositionString(String positionString) {
        this.positionString = positionString;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("position", position).append("positionString", positionString).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(position).append(positionString).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Positions) == false) {
            return false;
        }
        Positions rhs = ((Positions) other);
        return new EqualsBuilder().append(position, rhs.position).append(positionString, rhs.positionString).isEquals();
    }

}
