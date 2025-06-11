
package com.sos.joc.model.dailyplan;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Daily Plan copy order
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "scheduledFor",
    "cycle",
    "timeZone",
    "forceJobAdmission",
    "stickDailyPlanDate"
})
public class DailyPlanCopyOrder
    extends DailyPlanBaseOrder
{

    /**
     * ISO format yyyy-mm-dd[ HH:MM[:SS]] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    @JsonPropertyDescription("ISO format yyyy-mm-dd[ HH:MM[:SS]] or now or now + HH:MM[:SS] or now + SECONDS or empty")
    private String scheduledFor;
    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    private Cycle cycle;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    private String timeZone;
    @JsonProperty("forceJobAdmission")
    private Boolean forceJobAdmission = false;
    @JsonProperty("stickDailyPlanDate")
    @JsonAlias({
        "stickToDailyPlanDate"
    })
    private Boolean stickDailyPlanDate = false;

    /**
     * ISO format yyyy-mm-dd[ HH:MM[:SS]] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    public String getScheduledFor() {
        return scheduledFor;
    }

    /**
     * ISO format yyyy-mm-dd[ HH:MM[:SS]] or now or now + HH:MM[:SS] or now + SECONDS or empty
     * 
     */
    @JsonProperty("scheduledFor")
    public void setScheduledFor(String scheduledFor) {
        this.scheduledFor = scheduledFor;
    }

    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    public Cycle getCycle() {
        return cycle;
    }

    /**
     * Cyclic Order
     * <p>
     * 
     * 
     */
    @JsonProperty("cycle")
    public void setCycle(Cycle cycle) {
        this.cycle = cycle;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty("forceJobAdmission")
    public Boolean getForceJobAdmission() {
        return forceJobAdmission;
    }

    @JsonProperty("forceJobAdmission")
    public void setForceJobAdmission(Boolean forceJobAdmission) {
        this.forceJobAdmission = forceJobAdmission;
    }

    @JsonProperty("stickDailyPlanDate")
    public Boolean getStickDailyPlanDate() {
        return stickDailyPlanDate;
    }

    @JsonProperty("stickDailyPlanDate")
    public void setStickDailyPlanDate(Boolean stickDailyPlanDate) {
        this.stickDailyPlanDate = stickDailyPlanDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("scheduledFor", scheduledFor).append("cycle", cycle).append("timeZone", timeZone).append("forceJobAdmission", forceJobAdmission).append("stickDailyPlanDate", stickDailyPlanDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(timeZone).append(forceJobAdmission).append(cycle).append(stickDailyPlanDate).append(scheduledFor).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanCopyOrder) == false) {
            return false;
        }
        DailyPlanCopyOrder rhs = ((DailyPlanCopyOrder) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(timeZone, rhs.timeZone).append(forceJobAdmission, rhs.forceJobAdmission).append(cycle, rhs.cycle).append(stickDailyPlanDate, rhs.stickDailyPlanDate).append(scheduledFor, rhs.scheduledFor).isEquals();
    }

}
