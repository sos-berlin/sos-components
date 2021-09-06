
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * admission time scheme
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "periods"
})
public class AdmissionTimeScheme {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periods")
    private List<AdmissionTimePeriod> periods = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AdmissionTimeScheme() {
    }

    /**
     * 
     * @param periods
     */
    public AdmissionTimeScheme(List<AdmissionTimePeriod> periods) {
        super();
        this.periods = periods;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periods")
    public List<AdmissionTimePeriod> getPeriods() {
        return periods;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periods")
    public void setPeriods(List<AdmissionTimePeriod> periods) {
        this.periods = periods;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("periods", periods).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(periods).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdmissionTimeScheme) == false) {
            return false;
        }
        AdmissionTimeScheme rhs = ((AdmissionTimeScheme) other);
        return new EqualsBuilder().append(periods, rhs.periods).isEquals();
    }

}
