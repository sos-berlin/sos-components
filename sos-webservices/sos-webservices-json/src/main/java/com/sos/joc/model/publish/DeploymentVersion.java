
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
    "deployConfiguration",
    "version"
})
public class DeploymentVersion {

    /**
     * DeployConfiguration
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfiguration")
    private DeployConfiguration deployConfiguration;
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
     * DeployConfiguration
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfiguration")
    public DeployConfiguration getDeployConfiguration() {
        return deployConfiguration;
    }

    /**
     * DeployConfiguration
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfiguration")
    public void setDeployConfiguration(DeployConfiguration deployConfiguration) {
        this.deployConfiguration = deployConfiguration;
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
        return new ToStringBuilder(this).append("deployConfiguration", deployConfiguration).append("version", version).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(version).append(deployConfiguration).toHashCode();
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
        return new EqualsBuilder().append(version, rhs.version).append(deployConfiguration, rhs.deployConfiguration).isEquals();
    }

}
