
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "restriction",
    "periods"
})
public class AdmissionRestrictedScheme {

    /**
     * admission restriction
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("restriction")
    private AdmissionRestriction restriction;
    @JsonProperty("periods")
    private List<AdmissionTimePeriod> periods = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AdmissionRestrictedScheme() {
    }

    /**
     * 
     * @param restriction
     * @param periods
     */
    public AdmissionRestrictedScheme(AdmissionRestriction restriction, List<AdmissionTimePeriod> periods) {
        super();
        this.restriction = restriction;
        this.periods = periods;
    }

    /**
     * admission restriction
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("restriction")
    public AdmissionRestriction getRestriction() {
        return restriction;
    }

    /**
     * admission restriction
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("restriction")
    public void setRestriction(AdmissionRestriction restriction) {
        this.restriction = restriction;
    }

    @JsonProperty("periods")
    public List<AdmissionTimePeriod> getPeriods() {
        return periods;
    }

    @JsonProperty("periods")
    public void setPeriods(List<AdmissionTimePeriod> periods) {
        this.periods = periods;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("restriction", restriction).append("periods", periods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(restriction).append(periods).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdmissionRestrictedScheme) == false) {
            return false;
        }
        AdmissionRestrictedScheme rhs = ((AdmissionRestrictedScheme) other);
        return new EqualsBuilder().append(restriction, rhs.restriction).append(periods, rhs.periods).isEquals();
    }

}
