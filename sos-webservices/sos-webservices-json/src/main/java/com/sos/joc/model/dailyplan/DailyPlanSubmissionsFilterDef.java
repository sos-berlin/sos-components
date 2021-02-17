
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
 * Daily Plan  Submissions Filter Definition
 * <p>
 * Define the filter To get submissions from the daily plan
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "dateFrom",
    "dateTo",
    "dateFor",
    "controllerIds"
})
public class DailyPlanSubmissionsFilterDef {

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateTo;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFor")
    @JsonPropertyDescription("0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp")
    private String dateFor;
    @JsonProperty("controllerIds")
    private List<String> controllerIds = null;

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFor")
    public String getDateFor() {
        return dateFor;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][smhdwMy] (where smhdwMy unit for second, minute, etc) or ISO 8601 timestamp
     * 
     */
    @JsonProperty("dateFor")
    public void setDateFor(String dateFor) {
        this.dateFor = dateFor;
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
        return new ToStringBuilder(this).append("dateFrom", dateFrom).append("dateTo", dateTo).append("dateFor", dateFor).append("controllerIds", controllerIds).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateTo).append(dateFrom).append(dateFor).append(controllerIds).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DailyPlanSubmissionsFilterDef) == false) {
            return false;
        }
        DailyPlanSubmissionsFilterDef rhs = ((DailyPlanSubmissionsFilterDef) other);
        return new EqualsBuilder().append(dateTo, rhs.dateTo).append(dateFrom, rhs.dateFrom).append(dateFor, rhs.dateFor).append(controllerIds, rhs.controllerIds).isEquals();
    }

}
