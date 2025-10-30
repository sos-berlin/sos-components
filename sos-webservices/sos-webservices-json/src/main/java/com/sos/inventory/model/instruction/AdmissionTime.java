
package com.sos.inventory.model.instruction;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.job.AdmissionTimeScheme;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * AdmissionTime
 * <p>
 * instruction with fixed property 'TYPE':'AdmissionTime'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "admissionTimeScheme",
    "skipIfNoAdmissionForOrderDay",
    "block"
})
public class AdmissionTime
    extends Instruction
{

    /**
     * admission time scheme
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("admissionTimeScheme")
    private AdmissionTimeScheme admissionTimeScheme;
    @JsonProperty("skipIfNoAdmissionForOrderDay")
    @JsonAlias({
        "skipIfNoAdmissionStartForOrderDay"
    })
    private Boolean skipIfNoAdmissionForOrderDay = false;
    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("block")
    private Instructions block;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AdmissionTime() {
    }

    /**
     * 
     * @param skipIfNoAdmissionForOrderDay
     * @param block
     * @param admissionTimeScheme
     */
    public AdmissionTime(AdmissionTimeScheme admissionTimeScheme, Boolean skipIfNoAdmissionForOrderDay, Instructions block) {
        super();
        this.admissionTimeScheme = admissionTimeScheme;
        this.skipIfNoAdmissionForOrderDay = skipIfNoAdmissionForOrderDay;
        this.block = block;
    }

    /**
     * admission time scheme
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("admissionTimeScheme")
    public void setAdmissionTimeScheme(AdmissionTimeScheme admissionTimeScheme) {
        this.admissionTimeScheme = admissionTimeScheme;
    }

    @JsonProperty("skipIfNoAdmissionForOrderDay")
    public Boolean getSkipIfNoAdmissionForOrderDay() {
        return skipIfNoAdmissionForOrderDay;
    }

    @JsonProperty("skipIfNoAdmissionForOrderDay")
    public void setSkipIfNoAdmissionForOrderDay(Boolean skipIfNoAdmissionForOrderDay) {
        this.skipIfNoAdmissionForOrderDay = skipIfNoAdmissionForOrderDay;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("block")
    public Instructions getBlock() {
        return block;
    }

    /**
     * instructions
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("block")
    public void setBlock(Instructions block) {
        this.block = block;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("admissionTimeScheme", admissionTimeScheme).append("skipIfNoAdmissionForOrderDay", skipIfNoAdmissionForOrderDay).append("block", block).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(block).append(admissionTimeScheme).append(skipIfNoAdmissionForOrderDay).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AdmissionTime) == false) {
            return false;
        }
        AdmissionTime rhs = ((AdmissionTime) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(block, rhs.block).append(admissionTimeScheme, rhs.admissionTimeScheme).append(skipIfNoAdmissionForOrderDay, rhs.skipIfNoAdmissionForOrderDay).isEquals();
    }

}
