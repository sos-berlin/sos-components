
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
 * Filter for valid and invalid Deployable Objects
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "draftConfigurations",
    "deployConfigurations",
    "withoutInvalid"
})
public class DeployablesFilter {

    @JsonProperty("draftConfigurations")
    private List<Config> draftConfigurations = new ArrayList<Config>();
    @JsonProperty("deployConfigurations")
    private List<Config> deployConfigurations = new ArrayList<Config>();
    @JsonProperty("withoutInvalid")
    private Boolean withoutInvalid = false;

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

    @JsonProperty("withoutInvalid")
    public Boolean getWithoutInvalid() {
        return withoutInvalid;
    }

    @JsonProperty("withoutInvalid")
    public void setWithoutInvalid(Boolean withoutInvalid) {
        this.withoutInvalid = withoutInvalid;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("draftConfigurations", draftConfigurations).append("deployConfigurations", deployConfigurations).append("withoutInvalid", withoutInvalid).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(draftConfigurations).append(withoutInvalid).append(deployConfigurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployablesFilter) == false) {
            return false;
        }
        DeployablesFilter rhs = ((DeployablesFilter) other);
        return new EqualsBuilder().append(draftConfigurations, rhs.draftConfigurations).append(withoutInvalid, rhs.withoutInvalid).append(deployConfigurations, rhs.deployConfigurations).isEquals();
    }

}
