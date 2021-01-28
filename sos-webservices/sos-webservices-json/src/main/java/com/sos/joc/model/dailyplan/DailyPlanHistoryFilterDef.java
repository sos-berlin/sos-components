
package com.sos.joc.model.dailyplan;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Daily Plan  Submissions Filter Definition
 * <p>
 * Define the filter To get the daily plan history
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "haveMessage",
    "dateFrom",
    "dateTo",
    "controllerIds"
})
public class DailyPlanHistoryFilterDef {

    @JsonProperty("haveMessage")
    private Boolean haveMessage = false;
    @JsonProperty("dateFrom")
    private String dateFrom;
    @JsonProperty("dateTo")
    private String dateTo;
    @JsonProperty("controllerIds")
    private List<String> controllerIds = null;

    @JsonProperty("haveMessage")
    public Boolean getHaveMessage() {
        return haveMessage;
    }

    @JsonProperty("haveMessage")
    public void setHaveMessage(Boolean haveMessage) {
        this.haveMessage = haveMessage;
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

    @JsonProperty("controllerIds")
    public List<String> getControllerIds() {
        return controllerIds;
    }

    @JsonProperty("controllerIds")
    public void setControllerIds(List<String> controllerIds) {
        this.controllerIds = controllerIds;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("haveMessage", haveMessage).append("dateFrom", dateFrom).append("dateTo", dateTo).append("controllerIds", controllerIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateTo).append(dateFrom).append(haveMessage).append(controllerIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanHistoryFilterDef) == false) {
            return false;
        }
        DailyPlanHistoryFilterDef rhs = ((DailyPlanHistoryFilterDef) other);
        return new EqualsBuilder().append(dateTo, rhs.dateTo).append(dateFrom, rhs.dateFrom).append(haveMessage, rhs.haveMessage).append(controllerIds, rhs.controllerIds).isEquals();
    }

}
