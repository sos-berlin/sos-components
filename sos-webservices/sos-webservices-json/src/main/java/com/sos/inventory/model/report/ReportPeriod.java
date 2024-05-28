
package com.sos.inventory.model.report;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * report period
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "length",
    "step"
})
public class ReportPeriod {

    @JsonProperty("length")
    private Integer length = 5;
    @JsonProperty("step")
    private Integer step = 5;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ReportPeriod() {
    }

    /**
     * 
     * @param length
     * @param step
     */
    public ReportPeriod(Integer length, Integer step) {
        super();
        this.length = length;
        this.step = step;
    }

    @JsonProperty("length")
    public Integer getLength() {
        return length;
    }

    @JsonProperty("length")
    public void setLength(Integer length) {
        this.length = length;
    }

    @JsonProperty("step")
    public Integer getStep() {
        return step;
    }

    @JsonProperty("step")
    public void setStep(Integer step) {
        this.step = step;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("length", length).append("step", step).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(length).append(step).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReportPeriod) == false) {
            return false;
        }
        ReportPeriod rhs = ((ReportPeriod) other);
        return new EqualsBuilder().append(length, rhs.length).append(step, rhs.step).isEquals();
    }

}
