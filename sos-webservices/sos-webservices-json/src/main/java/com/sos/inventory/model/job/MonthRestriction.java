
package com.sos.inventory.model.job;

import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * admission restricted scheme: MonthRestriction
 * <p>
 * admission restriction with fixed property 'TYPE':'MonthRestriction'
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "months"
})
public class MonthRestriction
    extends AdmissionRestriction
{

    /**
     *  1 .. 12
     * (Required)
     * 
     */
    @JsonProperty("months")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    @JsonPropertyDescription("1 .. 12")
    private Set<Integer> months = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public MonthRestriction() {
    }

    /**
     * 
     * @param months
     * @param tYPE
     */
    public MonthRestriction(Set<Integer> months, AdmissionRestrictionType tYPE) {
        super(tYPE);
        this.months = months;
    }

    /**
     *  1 .. 12
     * (Required)
     * 
     */
    @JsonProperty("months")
    public Set<Integer> getMonths() {
        return months;
    }

    /**
     *  1 .. 12
     * (Required)
     * 
     */
    @JsonProperty("months")
    public void setMonths(Set<Integer> months) {
        this.months = months;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("months", months).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(months).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof MonthRestriction) == false) {
            return false;
        }
        MonthRestriction rhs = ((MonthRestriction) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(months, rhs.months).isEquals();
    }

}
