
package com.sos.joc.model.wizard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * job wizard filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "assignReference"
})
public class JobWizardFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignReference")
    private String assignReference;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignReference")
    public String getAssignReference() {
        return assignReference;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("assignReference")
    public void setAssignReference(String assignReference) {
        this.assignReference = assignReference;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("assignReference", assignReference).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(assignReference).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobWizardFilter) == false) {
            return false;
        }
        JobWizardFilter rhs = ((JobWizardFilter) other);
        return new EqualsBuilder().append(assignReference, rhs.assignReference).isEquals();
    }

}
