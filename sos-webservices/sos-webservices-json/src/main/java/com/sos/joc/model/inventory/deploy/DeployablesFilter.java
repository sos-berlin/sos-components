
package com.sos.joc.model.inventory.deploy;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Filter Deployables
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "folder",
    "recursive",
    "objectTypes",
    "onlyValidObjects",
    "withRemovedObjects",
    "withoutDrafts",
    "withoutDeployed",
    "latest"
})
public class DeployablesFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path of an object.")
    private String folder;
    @JsonProperty("recursive")
    private Boolean recursive = false;
    @JsonProperty("objectTypes")
    private List<ConfigurationType> objectTypes = new ArrayList<ConfigurationType>();
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

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path of an object.
     * (Required)
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @JsonProperty("objectTypes")
    public List<ConfigurationType> getObjectTypes() {
        return objectTypes;
    }

    @JsonProperty("objectTypes")
    public void setObjectTypes(List<ConfigurationType> objectTypes) {
        this.objectTypes = objectTypes;
    }

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
        return new ToStringBuilder(this).append("controllerId", controllerId).append("folder", folder).append("recursive", recursive).append("objectTypes", objectTypes).append("onlyValidObjects", onlyValidObjects).append("withRemovedObjects", withRemovedObjects).append("withoutDrafts", withoutDrafts).append("withoutDeployed", withoutDeployed).append("latest", latest).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(controllerId).append(withRemovedObjects).append(withoutDeployed).append(withoutDrafts).append(objectTypes).append(recursive).append(onlyValidObjects).append(latest).toHashCode();
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
        return new EqualsBuilder().append(folder, rhs.folder).append(controllerId, rhs.controllerId).append(withRemovedObjects, rhs.withRemovedObjects).append(withoutDeployed, rhs.withoutDeployed).append(withoutDrafts, rhs.withoutDrafts).append(objectTypes, rhs.objectTypes).append(recursive, rhs.recursive).append(onlyValidObjects, rhs.onlyValidObjects).append(latest, rhs.latest).isEquals();
    }

}
