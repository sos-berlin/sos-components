
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
 * dailyPlanSubmissionHistoryItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "submissionHistoryId",
    "dailyPlanDate",
    "submissionTime",
    "submittedOrders",
    "canceledOrders",
    "warnMessages",
    "errorMessages"
})
public class DailyPlanSubmissionsItem {

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
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("submissionTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date submissionTime;
    @JsonProperty("submittedOrders")
    private List<String> submittedOrders = null;
    @JsonProperty("canceledOrders")
    private List<String> canceledOrders = null;
    @JsonProperty("warnMessages")
    private List<String> warnMessages = null;
    @JsonProperty("errorMessages")
    private List<String> errorMessages = null;

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

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("submissionTime")
    public Date getSubmissionTime() {
        return submissionTime;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("submissionTime")
    public void setSubmissionTime(Date submissionTime) {
        this.submissionTime = submissionTime;
    }

    @JsonProperty("submittedOrders")
    public List<String> getSubmittedOrders() {
        return submittedOrders;
    }

    @JsonProperty("submittedOrders")
    public void setSubmittedOrders(List<String> submittedOrders) {
        this.submittedOrders = submittedOrders;
    }

    @JsonProperty("canceledOrders")
    public List<String> getCanceledOrders() {
        return canceledOrders;
    }

    @JsonProperty("canceledOrders")
    public void setCanceledOrders(List<String> canceledOrders) {
        this.canceledOrders = canceledOrders;
    }

    @JsonProperty("warnMessages")
    public List<String> getWarnMessages() {
        return warnMessages;
    }

    @JsonProperty("warnMessages")
    public void setWarnMessages(List<String> warnMessages) {
        this.warnMessages = warnMessages;
    }

    @JsonProperty("errorMessages")
    public List<String> getErrorMessages() {
        return errorMessages;
    }

    @JsonProperty("errorMessages")
    public void setErrorMessages(List<String> errorMessages) {
        this.errorMessages = errorMessages;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("submissionHistoryId", submissionHistoryId).append("dailyPlanDate", dailyPlanDate).append("submissionTime", submissionTime).append("submittedOrders", submittedOrders).append("canceledOrders", canceledOrders).append("warnMessages", warnMessages).append("errorMessages", errorMessages).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorMessages).append(dailyPlanDate).append(controllerId).append(warnMessages).append(submissionHistoryId).append(submittedOrders).append(canceledOrders).append(submissionTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanSubmissionsItem) == false) {
            return false;
        }
        DailyPlanSubmissionsItem rhs = ((DailyPlanSubmissionsItem) other);
        return new EqualsBuilder().append(errorMessages, rhs.errorMessages).append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(warnMessages, rhs.warnMessages).append(submissionHistoryId, rhs.submissionHistoryId).append(submittedOrders, rhs.submittedOrders).append(canceledOrders, rhs.canceledOrders).append(submissionTime, rhs.submissionTime).isEquals();
    }

}
