
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
    "dailyPlanSubmissionHistoryId",
    "userAccount",
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
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("dailyPlanSubmissionHistoryId")
    private Long dailyPlanSubmissionHistoryId;
    @JsonProperty("userAccount")
    private String userAccount;
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

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("dailyPlanSubmissionHistoryId")
    public Long getDailyPlanSubmissionHistoryId() {
        return dailyPlanSubmissionHistoryId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("dailyPlanSubmissionHistoryId")
    public void setDailyPlanSubmissionHistoryId(Long dailyPlanSubmissionHistoryId) {
        this.dailyPlanSubmissionHistoryId = dailyPlanSubmissionHistoryId;
    }

    @JsonProperty("userAccount")
    public String getUserAccount() {
        return userAccount;
    }

    @JsonProperty("userAccount")
    public void setUserAccount(String userAccount) {
        this.userAccount = userAccount;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("dailyPlanSubmissionHistoryId", dailyPlanSubmissionHistoryId).append("userAccount", userAccount).append("dateFrom", dateFrom).append("dateTo", dateTo).append("timeZone", timeZone).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dailyPlanSubmissionHistoryId).append(controllerId).append(userAccount).append(dateTo).append(timeZone).append(dateFrom).toHashCode();
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
        return new EqualsBuilder().append(dailyPlanSubmissionHistoryId, rhs.dailyPlanSubmissionHistoryId).append(controllerId, rhs.controllerId).append(userAccount, rhs.userAccount).append(dateTo, rhs.dateTo).append(timeZone, rhs.timeZone).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
