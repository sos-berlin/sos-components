
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
 * Filter for Releasable Objects
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "draftConfigurations",
    "releasedConfigurations",
    "withoutInvalid"
})
public class ReleasablesFilter {

    @JsonProperty("draftConfigurations")
    private List<Config> draftConfigurations = new ArrayList<Config>();
    @JsonProperty("releasedConfigurations")
    private List<Config> releasedConfigurations = new ArrayList<Config>();
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

    @JsonProperty("releasedConfigurations")
    public List<Config> getReleasedConfigurations() {
        return releasedConfigurations;
    }

    @JsonProperty("releasedConfigurations")
    public void setReleasedConfigurations(List<Config> releasedConfigurations) {
        this.releasedConfigurations = releasedConfigurations;
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
        return new ToStringBuilder(this).append("draftConfigurations", draftConfigurations).append("releasedConfigurations", releasedConfigurations).append("withoutInvalid", withoutInvalid).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(draftConfigurations).append(withoutInvalid).append(releasedConfigurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleasablesFilter) == false) {
            return false;
        }
        ReleasablesFilter rhs = ((ReleasablesFilter) other);
        return new EqualsBuilder().append(draftConfigurations, rhs.draftConfigurations).append(withoutInvalid, rhs.withoutInvalid).append(releasedConfigurations, rhs.releasedConfigurations).isEquals();
    }

}
