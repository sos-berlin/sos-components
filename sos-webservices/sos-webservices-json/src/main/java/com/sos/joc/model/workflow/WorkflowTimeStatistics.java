
package com.sos.joc.model.workflow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Unit of max, min, average is seconds
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "count",
    "max",
    "min",
    "average"
})
public class WorkflowTimeStatistics {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("count")
    private Long count;
    @JsonProperty("max")
    private Double max;
    @JsonProperty("min")
    private Double min;
    @JsonProperty("average")
    private Double average;

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("count")
    public Long getCount() {
        return count;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("count")
    public void setCount(Long count) {
        this.count = count;
    }

    @JsonProperty("max")
    public Double getMax() {
        return max;
    }

    @JsonProperty("max")
    public void setMax(Double max) {
        this.max = max;
    }

    @JsonProperty("min")
    public Double getMin() {
        return min;
    }

    @JsonProperty("min")
    public void setMin(Double min) {
        this.min = min;
    }

    @JsonProperty("average")
    public Double getAverage() {
        return average;
    }

    @JsonProperty("average")
    public void setAverage(Double average) {
        this.average = average;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("count", count).append("max", max).append("min", min).append("average", average).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(count).append(average).append(min).append(max).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof WorkflowTimeStatistics) == false) {
            return false;
        }
        WorkflowTimeStatistics rhs = ((WorkflowTimeStatistics) other);
        return new EqualsBuilder().append(count, rhs.count).append(average, rhs.average).append(min, rhs.min).append(max, rhs.max).isEquals();
    }

}
