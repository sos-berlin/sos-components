
package com.sos.joc.model.dailyplan;

import java.util.List;
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
    "dailyPlanSubmissionHistoryIds",
    "dateFrom",
    "dateTo",
    "timeZone"
})
public class DailyPlanSubmissionHistoryFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("dailyPlanSubmissionHistoryIds")
    private List<Long> dailyPlanSubmissionHistoryIds = null;
    @JsonProperty("dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    private String dateTo;
    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    @JsonPropertyDescription("see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones")
    private String timeZone;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("dailyPlanSubmissionHistoryIds")
    public List<Long> getDailyPlanSubmissionHistoryIds() {
        return dailyPlanSubmissionHistoryIds;
    }

    @JsonProperty("dailyPlanSubmissionHistoryIds")
    public void setDailyPlanSubmissionHistoryIds(List<Long> dailyPlanSubmissionHistoryIds) {
        this.dailyPlanSubmissionHistoryIds = dailyPlanSubmissionHistoryIds;
    }

    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public String getTimeZone() {
        return timeZone;
    }

    /**
     * see https://en.wikipedia.org/wiki/List_of_tz_database_time_zones
     * 
     */
    @JsonProperty("timeZone")
    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("dailyPlanSubmissionHistoryIds", dailyPlanSubmissionHistoryIds).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateTo).append(timeZone).append(dailyPlanSubmissionHistoryIds).append(controllerId).append(dateFrom).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanSubmissionHistoryFilter) == false) {
            return false;
        }
        DailyPlanSubmissionHistoryFilter rhs = ((DailyPlanSubmissionHistoryFilter) other);
        return new EqualsBuilder().append(dateTo, rhs.dateTo).append(timeZone, rhs.timeZone).append(dailyPlanSubmissionHistoryIds, rhs.dailyPlanSubmissionHistoryIds).append(controllerId, rhs.controllerId).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
