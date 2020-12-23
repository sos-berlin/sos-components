
package com.sos.joc.model.publish;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter for Deploy-delete operation
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deployConfigurations"
})
public class DeployDeleteFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    private List<Config> deployConfigurations = new ArrayList<Config>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    public List<Config> getDeployConfigurations() {
        return deployConfigurations;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    public void setDeployConfigurations(List<Config> deployConfigurations) {
        this.deployConfigurations = deployConfigurations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deployConfigurations", deployConfigurations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployConfigurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployDeleteFilter) == false) {
            return false;
        }
        DeployDeleteFilter rhs = ((DeployDeleteFilter) other);
        return new EqualsBuilder().append(deployConfigurations, rhs.deployConfigurations).isEquals();
    }

}
