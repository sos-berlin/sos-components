
package com.sos.sign.model.instruction;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.instruction.InstructionType;
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
    "skipIfNoAdmissionStartForOrderDay",
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
    @JsonProperty("skipIfNoAdmissionStartForOrderDay")
    @JsonAlias({
        "skipIfNoAdmissionForOrderDay"
    })
    private Boolean skipIfNoAdmissionStartForOrderDay = false;
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
     * @param skipIfNoAdmissionStartForOrderDay
     * @param block
     * @param tYPE
     * @param admissionTimeScheme
     */
    public AdmissionTime(AdmissionTimeScheme admissionTimeScheme, Boolean skipIfNoAdmissionStartForOrderDay, Instructions block, InstructionType tYPE) {
        super(tYPE);
        this.admissionTimeScheme = admissionTimeScheme;
        this.skipIfNoAdmissionStartForOrderDay = skipIfNoAdmissionStartForOrderDay;
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

    @JsonProperty("skipIfNoAdmissionStartForOrderDay")
    public Boolean getSkipIfNoAdmissionStartForOrderDay() {
        return skipIfNoAdmissionStartForOrderDay;
    }

    @JsonProperty("skipIfNoAdmissionStartForOrderDay")
    public void setSkipIfNoAdmissionStartForOrderDay(Boolean skipIfNoAdmissionStartForOrderDay) {
        this.skipIfNoAdmissionStartForOrderDay = skipIfNoAdmissionStartForOrderDay;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("admissionTimeScheme", admissionTimeScheme).append("skipIfNoAdmissionStartForOrderDay", skipIfNoAdmissionStartForOrderDay).append("block", block).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(skipIfNoAdmissionStartForOrderDay).append(block).append(admissionTimeScheme).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(skipIfNoAdmissionStartForOrderDay, rhs.skipIfNoAdmissionStartForOrderDay).append(block, rhs.block).append(admissionTimeScheme, rhs.admissionTimeScheme).isEquals();
    }

}
