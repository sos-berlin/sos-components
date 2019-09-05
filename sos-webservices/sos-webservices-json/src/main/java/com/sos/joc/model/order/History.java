
package com.sos.joc.model.order;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "historyId",
    "steps"
})
public class History {

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    private Long historyId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("steps")
    private List<OrderStepHistoryItem> steps = new ArrayList<OrderStepHistoryItem>();

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    public Long getHistoryId() {
        return historyId;
    }

    /**
     * non negative long
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("historyId")
    public void setHistoryId(Long historyId) {
        this.historyId = historyId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("steps")
    public List<OrderStepHistoryItem> getSteps() {
        return steps;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("steps")
    public void setSteps(List<OrderStepHistoryItem> steps) {
        this.steps = steps;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("historyId", historyId).append("steps", steps).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(steps).append(historyId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof History) == false) {
            return false;
        }
        History rhs = ((History) other);
        return new EqualsBuilder().append(steps, rhs.steps).append(historyId, rhs.historyId).isEquals();
    }

}
