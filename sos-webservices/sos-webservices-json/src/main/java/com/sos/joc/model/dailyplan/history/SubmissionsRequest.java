
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
    "date",
    "submitted",
    "limit"
})
public class SubmissionsRequest {

    /**
     * controllerId
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("date")
    @JsonPropertyDescription("ISO date YYYY-MM-DD")
    private String date;
    @JsonProperty("submitted")
    private Boolean submitted;
    @JsonProperty("limit")
    private Integer limit = 5000;

    /**
     * controllerId
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("date")
    public String getDate() {
        return date;
    }

    /**
     * date
     * <p>
     * ISO date YYYY-MM-DD
     * (Required)
     * 
     */
    @JsonProperty("date")
    public void setDate(String date) {
        this.date = date;
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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("date", date).append("submitted", submitted).append("limit", limit).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(date).append(limit).append(submitted).append(controllerId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SubmissionsRequest) == false) {
            return false;
        }
        SubmissionsRequest rhs = ((SubmissionsRequest) other);
        return new EqualsBuilder().append(date, rhs.date).append(limit, rhs.limit).append(submitted, rhs.submitted).append(controllerId, rhs.controllerId).isEquals();
    }

}
