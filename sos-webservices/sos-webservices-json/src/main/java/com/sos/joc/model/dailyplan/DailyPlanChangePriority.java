
package com.sos.joc.model.dailyplan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Daily Plan change priority
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "priority"
})
public class DailyPlanChangePriority
    extends DailyPlanBaseOrder
{

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("priority")
    private Integer priority;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("priority")
    public Integer getPriority() {
        return priority;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("priority")
    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("priority", priority).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(priority).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanChangePriority) == false) {
            return false;
        }
        DailyPlanChangePriority rhs = ((DailyPlanChangePriority) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(priority, rhs.priority).isEquals();
    }

}
