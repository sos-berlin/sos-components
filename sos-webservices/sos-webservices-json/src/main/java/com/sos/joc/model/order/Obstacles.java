
package com.sos.joc.model.order;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "orderId",
    "obstacles"
})
public class Obstacles {

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    private String orderId;
    @JsonProperty("obstacles")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<Obstacle> obstacles = new LinkedHashSet<Obstacle>();

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    public String getOrderId() {
        return orderId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("orderId")
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    @JsonProperty("obstacles")
    public Set<Obstacle> getObstacles() {
        return obstacles;
    }

    @JsonProperty("obstacles")
    public void setObstacles(Set<Obstacle> obstacles) {
        this.obstacles = obstacles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("orderId", orderId).append("obstacles", obstacles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(orderId).append(obstacles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Obstacles) == false) {
            return false;
        }
        Obstacles rhs = ((Obstacles) other);
        return new EqualsBuilder().append(orderId, rhs.orderId).append(obstacles, rhs.obstacles).isEquals();
    }

}
