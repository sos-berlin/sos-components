
package com.sos.joc.model.publish.folder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Shallow Copy ExportFolderFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "objectTypes",
    "folders",
    "recursive",
    "onlyValidObjects",
    "withoutDrafts",
    "withoutDeployed",
    "withoutReleased",
    "inclAllTags"
})
public class ExportFolderShallowCopy {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ConfigurationType> objectTypes = new LinkedHashSet<ConfigurationType>();
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("folders")
    private List<String> folders = new ArrayList<String>();
    @JsonProperty("recursive")
    private Boolean recursive = false;
    @JsonProperty("onlyValidObjects")
    private Boolean onlyValidObjects = false;
    @JsonProperty("withoutDrafts")
    private Boolean withoutDrafts = false;
    @JsonProperty("withoutDeployed")
    private Boolean withoutDeployed = false;
    @JsonProperty("withoutReleased")
    private Boolean withoutReleased = false;
    @JsonProperty("inclAllTags")
    private Boolean inclAllTags = false;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    public Set<ConfigurationType> getObjectTypes() {
        return objectTypes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectTypes")
    public void setObjectTypes(Set<ConfigurationType> objectTypes) {
        this.objectTypes = objectTypes;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("folders")
    public List<String> getFolders() {
        return folders;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<String> folders) {
        this.folders = folders;
    }

    @JsonProperty("recursive")
    public Boolean getRecursive() {
        return recursive;
    }

    @JsonProperty("recursive")
    public void setRecursive(Boolean recursive) {
        this.recursive = recursive;
    }

    @JsonProperty("onlyValidObjects")
    public Boolean getOnlyValidObjects() {
        return onlyValidObjects;
    }

    @JsonProperty("onlyValidObjects")
    public void setOnlyValidObjects(Boolean onlyValidObjects) {
        this.onlyValidObjects = onlyValidObjects;
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

    @JsonProperty("withoutReleased")
    public Boolean getWithoutReleased() {
        return withoutReleased;
    }

    @JsonProperty("withoutReleased")
    public void setWithoutReleased(Boolean withoutReleased) {
        this.withoutReleased = withoutReleased;
    }

    @JsonProperty("inclAllTags")
    public Boolean getInclAllTags() {
        return inclAllTags;
    }

    @JsonProperty("inclAllTags")
    public void setInclAllTags(Boolean inclAllTags) {
        this.inclAllTags = inclAllTags;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("objectTypes", objectTypes).append("folders", folders).append("recursive", recursive).append("onlyValidObjects", onlyValidObjects).append("withoutDrafts", withoutDrafts).append("withoutDeployed", withoutDeployed).append("withoutReleased", withoutReleased).append("inclAllTags", inclAllTags).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folders).append(inclAllTags).append(withoutDeployed).append(withoutDrafts).append(objectTypes).append(recursive).append(onlyValidObjects).append(withoutReleased).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportFolderShallowCopy) == false) {
            return false;
        }
        ExportFolderShallowCopy rhs = ((ExportFolderShallowCopy) other);
        return new EqualsBuilder().append(folders, rhs.folders).append(inclAllTags, rhs.inclAllTags).append(withoutDeployed, rhs.withoutDeployed).append(withoutDrafts, rhs.withoutDrafts).append(objectTypes, rhs.objectTypes).append(recursive, rhs.recursive).append(onlyValidObjects, rhs.onlyValidObjects).append(withoutReleased, rhs.withoutReleased).isEquals();
    }

}
