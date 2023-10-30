
package com.sos.joc.model.dailyplan.projections.items.meta;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * daily plan projection
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "avg"
})
public class WorkflowItem {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("avg")
    private Long avg;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("avg")
    public Long getAvg() {
        return avg;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("avg")
    public void setAvg(Long avg) {
        this.avg = avg;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("avg", avg).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(avg).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowItem) == false) {
            return false;
        }
        WorkflowItem rhs = ((WorkflowItem) other);
        return new EqualsBuilder().append(avg, rhs.avg).isEquals();
    }

}
