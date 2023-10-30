
package com.sos.joc.model.inventory.descriptor;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.descriptor.DeploymentDescriptor;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JS7 Deployment Descriptor Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class DeploymentDescriptorEdit
    extends ConfigurationObject
{

    /**
     * Deployment Descriptor
     * <p>
     * JS7 Deployment Descriptor Schema
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("JS7 Deployment Descriptor Schema")
    private DeploymentDescriptor configuration;

    /**
     * Deployment Descriptor
     * <p>
     * JS7 Deployment Descriptor Schema
     * 
     */
    @JsonProperty("configuration")
    public DeploymentDescriptor getConfiguration() {
        return configuration;
    }

    /**
     * Deployment Descriptor
     * <p>
     * JS7 Deployment Descriptor Schema
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(DeploymentDescriptor configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(configuration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeploymentDescriptorEdit) == false) {
            return false;
        }
        DeploymentDescriptorEdit rhs = ((DeploymentDescriptorEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
