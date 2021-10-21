
package com.sos.joc.model.order;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Obstacle
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type",
    "until"
})
public class Obstacle {

    /**
     * ObstacleType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ObstacleType type;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date until;

    /**
     * ObstacleType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ObstacleType getType() {
        return type;
    }

    /**
     * ObstacleType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(ObstacleType type) {
        this.type = type;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    public Date getUntil() {
        return until;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("until")
    public void setUntil(Date until) {
        this.until = until;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).append("until", until).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).append(until).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Obstacle) == false) {
            return false;
        }
        Obstacle rhs = ((Obstacle) other);
        return new EqualsBuilder().append(type, rhs.type).append(until, rhs.until).isEquals();
    }

}
