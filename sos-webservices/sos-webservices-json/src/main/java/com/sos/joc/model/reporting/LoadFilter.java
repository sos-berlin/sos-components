
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
    "dateFrom"
})
public class LoadFilter {

    /**
     * A month in the format YYYY-MM
     * (Required)
     * 
     */
    @JsonProperty("dateFrom")
    @JsonPropertyDescription("A month in the format YYYY-MM")
    private String dateFrom;

    /**
     * A month in the format YYYY-MM
     * (Required)
     * 
     */
    @JsonProperty("dateFrom")
    public String getDateFrom() {
        return dateFrom;
    }

    /**
     * A month in the format YYYY-MM
     * (Required)
     * 
     */
    @JsonProperty("dateFrom")
    public void setDateFrom(String dateFrom) {
        this.dateFrom = dateFrom;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("dateFrom", dateFrom).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(dateFrom).toHashCode();
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
        return new EqualsBuilder().append(dateFrom, rhs.dateFrom).isEquals();
    }

}
