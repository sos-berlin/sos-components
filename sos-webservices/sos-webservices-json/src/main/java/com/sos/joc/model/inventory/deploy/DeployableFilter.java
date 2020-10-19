
package com.sos.joc.model.inventory.deploy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.RequestFilter;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for deploy
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "onlyValidObjects",
    "withVersions"
})
public class DeployableFilter
    extends RequestFilter
{

    @JsonProperty("onlyValidObjects")
    private Boolean onlyValidObjects = false;
    @JsonProperty("withVersions")
    private Boolean withVersions = false;

    @JsonProperty("onlyValidObjects")
    public Boolean getOnlyValidObjects() {
        return onlyValidObjects;
    }

    @JsonProperty("onlyValidObjects")
    public void setOnlyValidObjects(Boolean onlyValidObjects) {
        this.onlyValidObjects = onlyValidObjects;
    }

    @JsonProperty("withVersions")
    public Boolean getWithVersions() {
        return withVersions;
    }

    @JsonProperty("withVersions")
    public void setWithVersions(Boolean withVersions) {
        this.withVersions = withVersions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("onlyValidObjects", onlyValidObjects).append("withVersions", withVersions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(onlyValidObjects).append(withVersions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployableFilter) == false) {
            return false;
        }
        DeployableFilter rhs = ((DeployableFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(onlyValidObjects, rhs.onlyValidObjects).append(withVersions, rhs.withVersions).isEquals();
    }

}
