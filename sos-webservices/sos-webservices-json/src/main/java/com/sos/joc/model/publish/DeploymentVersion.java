
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JS Object Filter configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configuration",
    "version"
})
public class DeploymentVersion {

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
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;

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

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configuration", configuration).append("version", version).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configuration).append(version).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeploymentVersion) == false) {
            return false;
        }
        DeploymentVersion rhs = ((DeploymentVersion) other);
        return new EqualsBuilder().append(configuration, rhs.configuration).append(version, rhs.version).isEquals();
    }

}
