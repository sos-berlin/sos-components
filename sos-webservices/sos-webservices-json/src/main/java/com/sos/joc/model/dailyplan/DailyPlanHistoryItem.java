
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
 * dailyPlanHistoryItem
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "dailyPlanDate",
    "submittedOrders",
    "canceledOrders",
    "submissions",
    "infoMessages",
    "warnMessages",
    "errorMessages"
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
    @JsonProperty("submittedOrders")
    private List<String> submittedOrders = null;
    @JsonProperty("canceledOrders")
    private List<String> canceledOrders = null;
    @JsonProperty("submissions")
    private List<DailyPlanSubmissionsItem> submissions = null;
    @JsonProperty("infoMessages")
    private List<String> infoMessages = null;
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

    @JsonProperty("submissions")
    public List<DailyPlanSubmissionsItem> getSubmissions() {
        return submissions;
    }

    @JsonProperty("submissions")
    public void setSubmissions(List<DailyPlanSubmissionsItem> submissions) {
        this.submissions = submissions;
    }

    @JsonProperty("infoMessages")
    public List<String> getInfoMessages() {
        return infoMessages;
    }

    @JsonProperty("infoMessages")
    public void setInfoMessages(List<String> infoMessages) {
        this.infoMessages = infoMessages;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("dailyPlanDate", dailyPlanDate).append("submittedOrders", submittedOrders).append("canceledOrders", canceledOrders).append("submissions", submissions).append("infoMessages", infoMessages).append("warnMessages", warnMessages).append("errorMessages", errorMessages).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorMessages).append(dailyPlanDate).append(controllerId).append(warnMessages).append(submissions).append(submittedOrders).append(infoMessages).append(canceledOrders).toHashCode();
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
        return new EqualsBuilder().append(errorMessages, rhs.errorMessages).append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(warnMessages, rhs.warnMessages).append(submissions, rhs.submissions).append(submittedOrders, rhs.submittedOrders).append(infoMessages, rhs.infoMessages).append(canceledOrders, rhs.canceledOrders).isEquals();
    }

}
