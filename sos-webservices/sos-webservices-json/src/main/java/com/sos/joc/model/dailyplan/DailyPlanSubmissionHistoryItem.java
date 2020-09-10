
package com.sos.joc.model.dailyplan;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * dailyPlanSubmissionHistoryItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "jobschedulerId",
    "submissionHistoryId",
    "dailyPlanDate"
})
public class DailyPlanSubmissionHistoryItem {

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobschedulerId")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String jobschedulerId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    private Long submissionHistoryId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("dailyPlanDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date dailyPlanDate;

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    public Long getSubmissionHistoryId() {
        return submissionHistoryId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("submissionHistoryId")
    public void setSubmissionHistoryId(Long submissionHistoryId) {
        this.submissionHistoryId = submissionHistoryId;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("submissionHistoryId", submissionHistoryId).append("dailyPlanDate", dailyPlanDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dailyPlanDate).append(jobschedulerId).append(submissionHistoryId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanSubmissionHistoryItem) == false) {
            return false;
        }
        DailyPlanSubmissionHistoryItem rhs = ((DailyPlanSubmissionHistoryItem) other);
        return new EqualsBuilder().append(dailyPlanDate, rhs.dailyPlanDate).append(jobschedulerId, rhs.jobschedulerId).append(submissionHistoryId, rhs.submissionHistoryId).isEquals();
    }

}
