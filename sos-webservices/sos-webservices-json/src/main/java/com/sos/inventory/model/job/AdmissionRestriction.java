
package com.sos.inventory.model.job;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sos.inventory.model.common.ClassHelper;


/**
 * admissionTimePeriod
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE"
})
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME, 
		include = JsonTypeInfo.As.PROPERTY, 
		property = "TYPE",
		visible = true)
@JsonSubTypes({ 
        @JsonSubTypes.Type(value = MonthRestriction.class, name = "MonthRestriction")})
public abstract class AdmissionRestriction
    extends ClassHelper
{

    /**
     * admissionTimePeriodType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    private AdmissionRestrictionType tYPE;
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public AdmissionRestriction() {
    }

    /**
     * 
     * @param tYPE
     */
    public AdmissionRestriction(AdmissionRestrictionType tYPE) {
        super();
        this.tYPE = tYPE;
    }

    /**
     * admissionRestrictionType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    @JsonIgnore
    public AdmissionRestrictionType getTYPE() {
        return tYPE;
    }

    /**
     * admissionRestrictionType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(AdmissionRestrictionType tYPE) {
        this.tYPE = tYPE;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("tYPE", tYPE).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdmissionRestriction) == false) {
            return false;
        }
        AdmissionRestriction rhs = ((AdmissionRestriction) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(tYPE, rhs.tYPE).isEquals();
    }

}
