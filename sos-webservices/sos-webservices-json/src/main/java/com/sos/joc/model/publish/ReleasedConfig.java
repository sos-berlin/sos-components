
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Released Configuration
 * <p>
 * Path and Type to identify a configuration of a release Objects
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "releasedConfiguration"
})
public class ReleasedConfig {

    /**
     * Filter for Configurations
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("releasedConfiguration")
    private ConfigurationFilter releasedConfiguration;

    /**
     * Filter for Configurations
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("releasedConfiguration")
    public ConfigurationFilter getReleasedConfiguration() {
        return releasedConfiguration;
    }

    /**
     * Filter for Configurations
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("releasedConfiguration")
    public void setReleasedConfiguration(ConfigurationFilter releasedConfiguration) {
        this.releasedConfiguration = releasedConfiguration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("releasedConfiguration", releasedConfiguration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(releasedConfiguration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleasedConfig) == false) {
            return false;
        }
        ReleasedConfig rhs = ((ReleasedConfig) other);
        return new EqualsBuilder().append(releasedConfiguration, rhs.releasedConfiguration).isEquals();
    }

}
