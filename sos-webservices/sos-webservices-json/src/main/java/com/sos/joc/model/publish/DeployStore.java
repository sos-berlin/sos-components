
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
 * DeployStore
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "draftConfigurations",
    "deployConfigurations"
})
public class DeployStore {

    @JsonProperty("draftConfigurations")
    private List<DraftConfig> draftConfigurations = new ArrayList<DraftConfig>();
    @JsonProperty("deployConfigurations")
    private List<DeployConfig> deployConfigurations = new ArrayList<DeployConfig>();

    @JsonProperty("draftConfigurations")
    public List<DraftConfig> getDraftConfigurations() {
        return draftConfigurations;
    }

    @JsonProperty("draftConfigurations")
    public void setDraftConfigurations(List<DraftConfig> draftConfigurations) {
        this.draftConfigurations = draftConfigurations;
    }

    @JsonProperty("deployConfigurations")
    public List<DeployConfig> getDeployConfigurations() {
        return deployConfigurations;
    }

    @JsonProperty("deployConfigurations")
    public void setDeployConfigurations(List<DeployConfig> deployConfigurations) {
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
        if ((other instanceof DeployStore) == false) {
            return false;
        }
        DeployStore rhs = ((DeployStore) other);
        return new EqualsBuilder().append(draftConfigurations, rhs.draftConfigurations).append(deployConfigurations, rhs.deployConfigurations).isEquals();
    }

}
