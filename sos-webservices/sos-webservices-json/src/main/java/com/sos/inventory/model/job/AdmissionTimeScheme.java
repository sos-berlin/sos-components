
package com.sos.inventory.model.job;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * admission time scheme
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "periods",
    "restrictedSchemes"
})
public class AdmissionTimeScheme {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("periods")
    private List<AdmissionTimePeriod> periods = null;
    @JsonProperty("restrictedSchemes")
    private List<AdmissionRestrictedScheme> restrictedSchemes = null;

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
     * @param restrictedSchemes
     * @param periods
     */
    public AdmissionTimeScheme(List<AdmissionTimePeriod> periods, List<AdmissionRestrictedScheme> restrictedSchemes) {
        super();
        this.periods = periods;
        this.restrictedSchemes = restrictedSchemes;
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

    @JsonProperty("restrictedSchemes")
    public List<AdmissionRestrictedScheme> getRestrictedSchemes() {
        return restrictedSchemes;
    }

    @JsonProperty("restrictedSchemes")
    public void setRestrictedSchemes(List<AdmissionRestrictedScheme> restrictedSchemes) {
        this.restrictedSchemes = restrictedSchemes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("periods", periods).append("restrictedSchemes", restrictedSchemes).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(restrictedSchemes).append(periods).toHashCode();
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
        return new EqualsBuilder().append(restrictedSchemes, rhs.restrictedSchemes).append(periods, rhs.periods).isEquals();
    }

}
