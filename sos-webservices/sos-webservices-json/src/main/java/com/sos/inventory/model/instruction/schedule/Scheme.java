
package com.sos.inventory.model.instruction.schedule;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Scheme
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "repeat",
    "admissionTimeScheme"
})
public class Scheme {

    /**
     * abstract Repeat
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("repeat")
    private Repeat repeat;
    /**
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    private AdmissionTimeScheme admissionTimeScheme;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Scheme() {
    }

    /**
     * 
     * @param repeat
     * @param admissionTimeScheme
     */
    public Scheme(Repeat repeat, AdmissionTimeScheme admissionTimeScheme) {
        super();
        this.repeat = repeat;
        this.admissionTimeScheme = admissionTimeScheme;
    }

    /**
     * abstract Repeat
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("repeat")
    public Repeat getRepeat() {
        return repeat;
    }

    /**
     * abstract Repeat
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("repeat")
    public void setRepeat(Repeat repeat) {
        this.repeat = repeat;
    }

    /**
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    public AdmissionTimeScheme getAdmissionTimeScheme() {
        return admissionTimeScheme;
    }

    /**
     * admission time scheme
     * <p>
     * 
     * 
     */
    @JsonProperty("admissionTimeScheme")
    public void setAdmissionTimeScheme(AdmissionTimeScheme admissionTimeScheme) {
        this.admissionTimeScheme = admissionTimeScheme;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("repeat", repeat).append("admissionTimeScheme", admissionTimeScheme).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(repeat).append(admissionTimeScheme).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Scheme) == false) {
            return false;
        }
        Scheme rhs = ((Scheme) other);
        return new EqualsBuilder().append(repeat, rhs.repeat).append(admissionTimeScheme, rhs.admissionTimeScheme).isEquals();
    }

}
