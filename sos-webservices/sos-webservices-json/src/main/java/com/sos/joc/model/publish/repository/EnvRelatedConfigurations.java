
package com.sos.joc.model.publish.repository;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.publish.Config;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter for environment related Objects
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "draftConfigurations",
    "deployConfigurations",
    "releasedConfigurations"
})
public class EnvRelatedConfigurations {

    @JsonProperty("draftConfigurations")
    private List<Config> draftConfigurations = new ArrayList<Config>();
    @JsonProperty("deployConfigurations")
    private List<Config> deployConfigurations = new ArrayList<Config>();
    @JsonProperty("releasedConfigurations")
    private List<Config> releasedConfigurations = new ArrayList<Config>();

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

    @JsonProperty("releasedConfigurations")
    public List<Config> getReleasedConfigurations() {
        return releasedConfigurations;
    }

    @JsonProperty("releasedConfigurations")
    public void setReleasedConfigurations(List<Config> releasedConfigurations) {
        this.releasedConfigurations = releasedConfigurations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("draftConfigurations", draftConfigurations).append("deployConfigurations", deployConfigurations).append("releasedConfigurations", releasedConfigurations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(draftConfigurations).append(releasedConfigurations).append(deployConfigurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EnvRelatedConfigurations) == false) {
            return false;
        }
        EnvRelatedConfigurations rhs = ((EnvRelatedConfigurations) other);
        return new EqualsBuilder().append(draftConfigurations, rhs.draftConfigurations).append(releasedConfigurations, rhs.releasedConfigurations).append(deployConfigurations, rhs.deployConfigurations).isEquals();
    }

}
