
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * DraftConfig
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "draftConfiguration"
})
public class DraftConfig {

    /**
     * DraftConfiguration
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("draftConfiguration")
    private DraftConfiguration draftConfiguration;

    /**
     * DraftConfiguration
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("draftConfiguration")
    public DraftConfiguration getDraftConfiguration() {
        return draftConfiguration;
    }

    /**
     * DraftConfiguration
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("draftConfiguration")
    public void setDraftConfiguration(DraftConfiguration draftConfiguration) {
        this.draftConfiguration = draftConfiguration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("draftConfiguration", draftConfiguration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(draftConfiguration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DraftConfig) == false) {
            return false;
        }
        DraftConfig rhs = ((DraftConfig) other);
        return new EqualsBuilder().append(draftConfiguration, rhs.draftConfiguration).isEquals();
    }

}
