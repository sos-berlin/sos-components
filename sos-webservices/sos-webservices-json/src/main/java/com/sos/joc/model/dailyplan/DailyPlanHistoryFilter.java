
package com.sos.joc.model.dailyplan;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * plans submission history filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "filter",
    "timeZone"
})
public class DailyPlanHistoryFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * Daily Plan  Submissions Filter Definition
     * <p>
     * Define the filter To get the daily plan history
     * 
     */
    @JsonProperty("filter")
    @JsonPropertyDescription("Define the filter To get the daily plan history")
    private DailyPlanHistoryFilterDef filter;
    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * Daily Plan  Submissions Filter Definition
     * <p>
     * Define the filter To get the daily plan history
     * 
     */
    @JsonProperty("filter")
    public DailyPlanHistoryFilterDef getFilter() {
        return filter;
    }

    /**
     * Daily Plan  Submissions Filter Definition
     * <p>
     * Define the filter To get the daily plan history
     * 
     */
    @JsonProperty("filter")
    public void setFilter(DailyPlanHistoryFilterDef filter) {
        this.filter = filter;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * string without < and >
     * <p>
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("filter", filter).append("timeZone", timeZone).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(filter).append(timeZone).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanHistoryFilter) == false) {
            return false;
        }
        DailyPlanHistoryFilter rhs = ((DailyPlanHistoryFilter) other);
        return new EqualsBuilder().append(filter, rhs.filter).append(timeZone, rhs.timeZone).append(controllerId, rhs.controllerId).isEquals();
    }

}
