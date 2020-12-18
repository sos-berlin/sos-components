
package com.sos.joc.model.inventory.deploy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
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
    "withRemovedObjects",
    "withoutDrafts",
    "withoutDeployed",
    "latest"
})
public class DeployableFilter
    extends RequestFilter
{

    @JsonProperty("onlyValidObjects")
    private Boolean onlyValidObjects = false;
    @JsonProperty("withRemovedObjects")
    private Boolean withRemovedObjects = false;
    @JsonProperty("withoutDrafts")
    private Boolean withoutDrafts = false;
    @JsonProperty("withoutDeployed")
    private Boolean withoutDeployed = false;
    /**
     * only relevant for deployed objects
     * 
     */
    @JsonProperty("latest")
    @JsonPropertyDescription("only relevant for deployed objects")
    private Boolean latest = false;

    @JsonProperty("onlyValidObjects")
    public Boolean getOnlyValidObjects() {
        return onlyValidObjects;
    }

    @JsonProperty("onlyValidObjects")
    public void setOnlyValidObjects(Boolean onlyValidObjects) {
        this.onlyValidObjects = onlyValidObjects;
    }

    @JsonProperty("withRemovedObjects")
    public Boolean getWithRemovedObjects() {
        return withRemovedObjects;
    }

    @JsonProperty("withRemovedObjects")
    public void setWithRemovedObjects(Boolean withRemovedObjects) {
        this.withRemovedObjects = withRemovedObjects;
    }

    @JsonProperty("withoutDrafts")
    public Boolean getWithoutDrafts() {
        return withoutDrafts;
    }

    @JsonProperty("withoutDrafts")
    public void setWithoutDrafts(Boolean withoutDrafts) {
        this.withoutDrafts = withoutDrafts;
    }

    @JsonProperty("withoutDeployed")
    public Boolean getWithoutDeployed() {
        return withoutDeployed;
    }

    @JsonProperty("withoutDeployed")
    public void setWithoutDeployed(Boolean withoutDeployed) {
        this.withoutDeployed = withoutDeployed;
    }

    /**
     * only relevant for deployed objects
     * 
     */
    @JsonProperty("latest")
    public Boolean getLatest() {
        return latest;
    }

    /**
     * only relevant for deployed objects
     * 
     */
    @JsonProperty("latest")
    public void setLatest(Boolean latest) {
        this.latest = latest;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("onlyValidObjects", onlyValidObjects).append("withRemovedObjects", withRemovedObjects).append("withoutDrafts", withoutDrafts).append("withoutDeployed", withoutDeployed).append("latest", latest).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(withoutDeployed).append(withoutDrafts).append(onlyValidObjects).append(withRemovedObjects).append(latest).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(withoutDeployed, rhs.withoutDeployed).append(withoutDrafts, rhs.withoutDrafts).append(onlyValidObjects, rhs.onlyValidObjects).append(withRemovedObjects, rhs.withRemovedObjects).append(latest, rhs.latest).isEquals();
    }

}
