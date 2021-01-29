
package com.sos.joc.model.dailyplan;

import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * DailyPlanHistoryItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "dailyPlanDate",
    "submissions"
})
public class DailyPlanHistoryItem {

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("controllerId")
    @JsonPropertyDescription("absolute path of an object.")
    private String controllerId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("dailyPlanDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date dailyPlanDate;
    @JsonProperty("submissions")
    private List<DailyPlanSubmissionTimes> submissions = null;

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("dailyPlanDate")
    public Date getDailyPlanDate() {
        return dailyPlanDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("dailyPlanDate")
    public void setDailyPlanDate(Date dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    @JsonProperty("submissions")
    public List<DailyPlanSubmissionTimes> getSubmissions() {
        return submissions;
    }

    @JsonProperty("submissions")
    public void setSubmissions(List<DailyPlanSubmissionTimes> submissions) {
        this.submissions = submissions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("dailyPlanDate", dailyPlanDate).append("submissions", submissions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dailyPlanDate).append(controllerId).append(submissions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanHistoryItem) == false) {
            return false;
        }
        DailyPlanHistoryItem rhs = ((DailyPlanHistoryItem) other);
        return new EqualsBuilder().append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(submissions, rhs.submissions).isEquals();
    }

}
