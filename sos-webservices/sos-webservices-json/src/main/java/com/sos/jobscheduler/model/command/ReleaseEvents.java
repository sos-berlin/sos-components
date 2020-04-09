
package com.sos.jobscheduler.model.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ReleaseEvents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "untilEventId"
})
public class ReleaseEvents
    extends Command
{

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("untilEventId")
    private Long untilEventId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ReleaseEvents() {
    }

    /**
     * 
     * @param untilEventId
     */
    public ReleaseEvents(Long untilEventId) {
        super();
        this.untilEventId = untilEventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("untilEventId")
    public Long getUntilEventId() {
        return untilEventId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("untilEventId")
    public void setUntilEventId(Long untilEventId) {
        this.untilEventId = untilEventId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("untilEventId", untilEventId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(untilEventId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleaseEvents) == false) {
            return false;
        }
        ReleaseEvents rhs = ((ReleaseEvents) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(untilEventId, rhs.untilEventId).isEquals();
    }

}
