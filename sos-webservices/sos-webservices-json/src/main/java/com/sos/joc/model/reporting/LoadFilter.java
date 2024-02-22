
package com.sos.joc.model.reporting;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dateFrom filter for loading of reporting data
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "monthFrom",
    "monthTo"
})
public class LoadFilter {

    /**
     * A month in the format YYYY-MM
     * (Required)
     * 
     */
    @JsonProperty("monthFrom")
    @JsonPropertyDescription("A month in the format YYYY-MM")
    private String monthFrom;
    /**
     * A month in the format YYYY-MM
     * 
     */
    @JsonProperty("monthTo")
    @JsonPropertyDescription("A month in the format YYYY-MM")
    private String monthTo;

    /**
     * A month in the format YYYY-MM
     * (Required)
     * 
     */
    @JsonProperty("monthFrom")
    public String getMonthFrom() {
        return monthFrom;
    }

    /**
     * A month in the format YYYY-MM
     * (Required)
     * 
     */
    @JsonProperty("monthFrom")
    public void setMonthFrom(String monthFrom) {
        this.monthFrom = monthFrom;
    }

    /**
     * A month in the format YYYY-MM
     * 
     */
    @JsonProperty("monthTo")
    public String getMonthTo() {
        return monthTo;
    }

    /**
     * A month in the format YYYY-MM
     * 
     */
    @JsonProperty("monthTo")
    public void setMonthTo(String monthTo) {
        this.monthTo = monthTo;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("monthFrom", monthFrom).append("monthTo", monthTo).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(monthFrom).append(monthTo).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof LoadFilter) == false) {
            return false;
        }
        LoadFilter rhs = ((LoadFilter) other);
        return new EqualsBuilder().append(monthFrom, rhs.monthFrom).append(monthTo, rhs.monthTo).isEquals();
    }

}
