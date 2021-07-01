
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
    "submitted",
    "submittedLate",
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
    @JsonProperty("submitted")
    private Integer submitted;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("submittedLate")
    private Integer submittedLate;
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
    @JsonProperty("submitted")
    public Integer getSubmitted() {
        return submitted;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("submitted")
    public void setSubmitted(Integer submitted) {
        this.submitted = submitted;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("submittedLate")
    public Integer getSubmittedLate() {
        return submittedLate;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("submittedLate")
    public void setSubmittedLate(Integer submittedLate) {
        this.submittedLate = submittedLate;
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
        return new ToStringBuilder(this).append("submitted", submitted).append("submittedLate", submittedLate).append("planned", planned).append("plannedLate", plannedLate).append("finished", finished).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(submittedLate).append(finished).append(submitted).append(planned).append(plannedLate).toHashCode();
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
        return new EqualsBuilder().append(submittedLate, rhs.submittedLate).append(finished, rhs.finished).append(submitted, rhs.submitted).append(planned, rhs.planned).append(plannedLate, rhs.plannedLate).isEquals();
    }

}
