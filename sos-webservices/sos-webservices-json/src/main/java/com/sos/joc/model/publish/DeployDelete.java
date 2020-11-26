
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
 * DeployDelete
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deployConfigurations"
})
public class DeployDelete {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    private List<DeployConfig> deployConfigurations = new ArrayList<DeployConfig>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    public List<DeployConfig> getDeployConfigurations() {
        return deployConfigurations;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployConfigurations")
    public void setDeployConfigurations(List<DeployConfig> deployConfigurations) {
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
        if ((other instanceof DeployDelete) == false) {
            return false;
        }
        DeployDelete rhs = ((DeployDelete) other);
        return new EqualsBuilder().append(deployConfigurations, rhs.deployConfigurations).isEquals();
    }

}
