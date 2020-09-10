
package com.sos.joc.model.dailyplan;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.webservices.order.initiator.model.OrderTemplate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Order Filter
 * <p>
 * To get orders from the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "submissionHistoryId",
    "orderTemplates",
    "orderKeys",
    "dailyPlanDate"
})
public class DailyPlanOrderFilter {

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
    @JsonProperty("submissionHistoryId")
    private Long submissionHistoryId;
    @JsonProperty("orderTemplates")
    private List<OrderTemplate> orderTemplates = new ArrayList<OrderTemplate>();
    @JsonProperty("orderKeys")
    private List<String> orderKeys = new ArrayList<String>();
    @JsonProperty("dailyPlanDate")
    private String dailyPlanDate;

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

    @JsonProperty("orderTemplates")
    public List<OrderTemplate> getOrderTemplates() {
        return orderTemplates;
    }

    @JsonProperty("orderTemplates")
    public void setOrderTemplates(List<OrderTemplate> orderTemplates) {
        this.orderTemplates = orderTemplates;
    }

    @JsonProperty("orderKeys")
    public List<String> getOrderKeys() {
        return orderKeys;
    }

    @JsonProperty("orderKeys")
    public void setOrderKeys(List<String> orderKeys) {
        this.orderKeys = orderKeys;
    }

    @JsonProperty("dailyPlanDate")
    public String getDailyPlanDate() {
        return dailyPlanDate;
    }

    @JsonProperty("dailyPlanDate")
    public void setDailyPlanDate(String dailyPlanDate) {
        this.dailyPlanDate = dailyPlanDate;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("submissionHistoryId", submissionHistoryId).append("orderTemplates", orderTemplates).append("orderKeys", orderKeys).append("dailyPlanDate", dailyPlanDate).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dailyPlanDate).append(controllerId).append(orderTemplates).append(orderKeys).append(submissionHistoryId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanOrderFilter) == false) {
            return false;
        }
        DailyPlanOrderFilter rhs = ((DailyPlanOrderFilter) other);
        return new EqualsBuilder().append(dailyPlanDate, rhs.dailyPlanDate).append(controllerId, rhs.controllerId).append(orderTemplates, rhs.orderTemplates).append(orderKeys, rhs.orderKeys).append(submissionHistoryId, rhs.submissionHistoryId).isEquals();
    }

}
