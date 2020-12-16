
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
 * Deployable Objects
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "draftConfigurations",
    "deployConfigurations"
})
public class DeployableObjects {

    @JsonProperty("draftConfigurations")
    private List<Config> draftConfigurations = new ArrayList<Config>();
    @JsonProperty("deployConfigurations")
    private List<Config> deployConfigurations = new ArrayList<Config>();

    @JsonProperty("draftConfigurations")
    public List<Config> getDraftConfigurations() {
        return draftConfigurations;
    }

    @JsonProperty("draftConfigurations")
    public void setDraftConfigurations(List<Config> draftConfigurations) {
        this.draftConfigurations = draftConfigurations;
    }

    @JsonProperty("deployConfigurations")
    public List<Config> getDeployConfigurations() {
        return deployConfigurations;
    }

    @JsonProperty("deployConfigurations")
    public void setDeployConfigurations(List<Config> deployConfigurations) {
        this.deployConfigurations = deployConfigurations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("draftConfigurations", draftConfigurations).append("deployConfigurations", deployConfigurations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(draftConfigurations).append(deployConfigurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployableObjects) == false) {
            return false;
        }
        DeployableObjects rhs = ((DeployableObjects) other);
        return new EqualsBuilder().append(draftConfigurations, rhs.draftConfigurations).append(deployConfigurations, rhs.deployConfigurations).isEquals();
    }

}
