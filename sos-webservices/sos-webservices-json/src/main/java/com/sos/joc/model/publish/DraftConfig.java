
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
     * Filter for Configurations
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("draftConfiguration")
    private ConfigurationFilter draftConfiguration;

    /**
     * Filter for Configurations
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("draftConfiguration")
    public ConfigurationFilter getDraftConfiguration() {
        return draftConfiguration;
    }

    /**
     * Filter for Configurations
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("draftConfiguration")
    public void setDraftConfiguration(ConfigurationFilter draftConfiguration) {
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
