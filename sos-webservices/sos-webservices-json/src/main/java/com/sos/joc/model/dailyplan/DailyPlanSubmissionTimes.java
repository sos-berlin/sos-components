
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
 * dailyPlanSubmissionTimes
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "submissionTime",
    "orderIds",
    "warnMessages",
    "errorMessages"
})
public class DailyPlanSubmissionTimes {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("submissionTime")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date submissionTime;
    @JsonProperty("orderIds")
    private List<DailyplanHistoryOrderItem> orderIds = null;
    @JsonProperty("warnMessages")
    private List<String> warnMessages = null;
    @JsonProperty("errorMessages")
    private List<String> errorMessages = null;

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

    @JsonProperty("orderIds")
    public List<DailyplanHistoryOrderItem> getOrderIds() {
        return orderIds;
    }

    @JsonProperty("orderIds")
    public void setOrderIds(List<DailyplanHistoryOrderItem> orderIds) {
        this.orderIds = orderIds;
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
        return new ToStringBuilder(this).append("submissionTime", submissionTime).append("orderIds", orderIds).append("warnMessages", warnMessages).append("errorMessages", errorMessages).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(errorMessages).append(orderIds).append(warnMessages).append(submissionTime).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanSubmissionTimes) == false) {
            return false;
        }
        DailyPlanSubmissionTimes rhs = ((DailyPlanSubmissionTimes) other);
        return new EqualsBuilder().append(errorMessages, rhs.errorMessages).append(orderIds, rhs.orderIds).append(warnMessages, rhs.warnMessages).append(submissionTime, rhs.submissionTime).isEquals();
    }

}
