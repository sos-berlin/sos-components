
package com.sos.joc.model.dailyplan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * order summary
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "pending",
    "pendingLate",
    "planned",
    "plannedLate",
    "finished"
})
public class DailyPlanOrdersSummary {

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pending")
    private Integer pending;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pendingLate")
    private Integer pendingLate;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("planned")
    private Integer planned;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("plannedLate")
    private Integer plannedLate;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("finished")
    private Integer finished;

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pending")
    public Integer getPending() {
        return pending;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pending")
    public void setPending(Integer pending) {
        this.pending = pending;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pendingLate")
    public Integer getPendingLate() {
        return pendingLate;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("pendingLate")
    public void setPendingLate(Integer pendingLate) {
        this.pendingLate = pendingLate;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("planned")
    public Integer getPlanned() {
        return planned;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("planned")
    public void setPlanned(Integer planned) {
        this.planned = planned;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("plannedLate")
    public Integer getPlannedLate() {
        return plannedLate;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("plannedLate")
    public void setPlannedLate(Integer plannedLate) {
        this.plannedLate = plannedLate;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("finished")
    public Integer getFinished() {
        return finished;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("finished")
    public void setFinished(Integer finished) {
        this.finished = finished;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("pending", pending).append("pendingLate", pendingLate).append("planned", planned).append("plannedLate", plannedLate).append("finished", finished).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(pendingLate).append(finished).append(planned).append(pending).append(plannedLate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanOrdersSummary) == false) {
            return false;
        }
        DailyPlanOrdersSummary rhs = ((DailyPlanOrdersSummary) other);
        return new EqualsBuilder().append(pendingLate, rhs.pendingLate).append(finished, rhs.finished).append(planned, rhs.planned).append(pending, rhs.pending).append(plannedLate, rhs.plannedLate).isEquals();
    }

}
