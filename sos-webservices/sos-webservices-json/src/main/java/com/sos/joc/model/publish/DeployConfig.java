
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * DeployConfig
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deployConfiguration"
})
public class DeployConfig {

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deployConfiguration", deployConfiguration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployConfiguration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployConfig) == false) {
            return false;
        }
        DeployConfig rhs = ((DeployConfig) other);
        return new EqualsBuilder().append(deployConfiguration, rhs.deployConfiguration).isEquals();
    }

}
