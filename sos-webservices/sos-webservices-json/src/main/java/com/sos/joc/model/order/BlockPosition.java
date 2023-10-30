
package com.sos.joc.model.order;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * available positions for an add order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "positions"
})
public class BlockPosition
    extends Position
{

    @JsonProperty("positions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Position> positions = new LinkedHashSet<Position>();

    @JsonProperty("positions")
    public Set<Position> getPositions() {
        return positions;
    }

    @JsonProperty("positions")
    public void setPositions(Set<Position> positions) {
        this.positions = positions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("positions", positions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(positions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof BlockPosition) == false) {
            return false;
        }
        BlockPosition rhs = ((BlockPosition) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(positions, rhs.positions).isEquals();
    }

}
