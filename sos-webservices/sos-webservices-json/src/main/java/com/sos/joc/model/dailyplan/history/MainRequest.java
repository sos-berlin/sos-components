
package com.sos.joc.model.dailyplan.history;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * daily plan history request
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "dateFrom",
    "dateTo",
    "submitted",
    "limit"
})
public class MainRequest {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][dwMy] (where dwMy unit for day, week, etc) or ISO date in YYYY-MM-DD format
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("0 or [number][dwMy] (where dwMy unit for day, week, etc) or ISO date in YYYY-MM-DD format")
    private String dateFrom;
    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][dwMy] (where dwMy unit for day, week, etc) or ISO date in YYYY-MM-DD format
     * 
     */
    @JsonProperty("dateTo")
    @JsonPropertyDescription("0 or [number][dwMy] (where dwMy unit for day, week, etc) or ISO date in YYYY-MM-DD format")
    private String dateTo;
    @JsonProperty("submitted")
    private Boolean submitted;
    @JsonProperty("limit")
    private Integer limit = 5000;

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][dwMy] (where dwMy unit for day, week, etc) or ISO date in YYYY-MM-DD format
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][dwMy] (where dwMy unit for day, week, etc) or ISO date in YYYY-MM-DD format
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][dwMy] (where dwMy unit for day, week, etc) or ISO date in YYYY-MM-DD format
     * 
     */
    @JsonProperty("dateTo")
    public String getDateTo() {
        return dateTo;
    }

    /**
     * string for dateFrom and dateTo as search filter
     * <p>
     *  0 or [number][dwMy] (where dwMy unit for day, week, etc) or ISO date in YYYY-MM-DD format
     * 
     */
    @JsonProperty("dateTo")
    public void setDateTo(String dateTo) {
        this.dateTo = dateTo;
    }

    @JsonProperty("submitted")
    public Boolean getSubmitted() {
        return submitted;
    }

    @JsonProperty("submitted")
    public void setSubmitted(Boolean submitted) {
        this.submitted = submitted;
    }

    @JsonProperty("limit")
    public Integer getLimit() {
        return limit;
    }

    @JsonProperty("limit")
    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("dateFrom", dateFrom).append("dateTo", dateTo).append("submitted", submitted).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateTo).append(limit).append(submitted).append(controllerId).append(dateFrom).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MainRequest) == false) {
            return false;
        }
        MainRequest rhs = ((MainRequest) other);
        return new EqualsBuilder().append(dateTo, rhs.dateTo).append(limit, rhs.limit).append(submitted, rhs.submitted).append(controllerId, rhs.controllerId).append(dateFrom, rhs.dateFrom).isEquals();
    }

}
