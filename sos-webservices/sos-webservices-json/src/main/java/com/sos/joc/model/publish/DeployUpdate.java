
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurationId",
    "deploymentId"
})
public class DeployUpdate {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationId")
    private Long configurationId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    private Long deploymentId;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationId")
    public Long getConfigurationId() {
        return configurationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationId")
    public void setConfigurationId(Long configurationId) {
        this.configurationId = configurationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    public Long getDeploymentId() {
        return deploymentId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    public void setDeploymentId(Long deploymentId) {
        this.deploymentId = deploymentId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configurationId", configurationId).append("deploymentId", deploymentId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationId).append(deploymentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployUpdate) == false) {
            return false;
        }
        DeployUpdate rhs = ((DeployUpdate) other);
        return new EqualsBuilder().append(configurationId, rhs.configurationId).append(deploymentId, rhs.deploymentId).isEquals();
    }

}
