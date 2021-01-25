
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Config
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configuration"
})
public class Config {

    /**
     * Configuration Filter. Identifies a configuration by its path and objectType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    private Configuration configuration;

    /**
     * Configuration Filter. Identifies a configuration by its path and objectType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * Configuration Filter. Identifies a configuration by its path and objectType
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configuration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Config) == false) {
            return false;
        }
        Config rhs = ((Config) other);
        return new EqualsBuilder().append(configuration, rhs.configuration).isEquals();
    }

}
