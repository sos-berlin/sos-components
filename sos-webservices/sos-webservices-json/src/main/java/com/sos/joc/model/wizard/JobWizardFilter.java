
package com.sos.joc.model.wizard;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * job wizard filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "assignReference",
    "language"
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
    @JsonProperty("language")
    private String language = "en";

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
    
    @JsonProperty("language")
    public String getLanguage() {
        return language;
    }

    @JsonProperty("language")
    public void setLanguage(String language) {
        this.language = language;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("assignReference", assignReference).append("language", language).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(assignReference).append(language).toHashCode();
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
        return new EqualsBuilder().append(assignReference, rhs.assignReference).append(language, rhs.language).isEquals();
    }

}
