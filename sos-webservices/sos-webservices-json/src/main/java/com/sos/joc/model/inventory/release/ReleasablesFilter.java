
package com.sos.joc.model.inventory.release;

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
 * Filter Releasables
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "folder",
    "recursive",
    "objectTypes",
    "onlyValidObjects",
    "withRemovedObjects",
    "withoutDrafts",
    "withoutReleased"
})
public class ReleasablesFilter {

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
    @JsonProperty("withoutReleased")
    private Boolean withoutReleased = false;

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

    @JsonProperty("withoutReleased")
    public Boolean getWithoutReleased() {
        return withoutReleased;
    }

    @JsonProperty("withoutReleased")
    public void setWithoutReleased(Boolean withoutReleased) {
        this.withoutReleased = withoutReleased;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("folder", folder).append("recursive", recursive).append("objectTypes", objectTypes).append("onlyValidObjects", onlyValidObjects).append("withRemovedObjects", withRemovedObjects).append("withoutDrafts", withoutDrafts).append("withoutReleased", withoutReleased).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(withRemovedObjects).append(withoutDrafts).append(objectTypes).append(recursive).append(onlyValidObjects).append(withoutReleased).toHashCode();
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
        return new EqualsBuilder().append(folder, rhs.folder).append(withRemovedObjects, rhs.withRemovedObjects).append(withoutDrafts, rhs.withoutDrafts).append(objectTypes, rhs.objectTypes).append(recursive, rhs.recursive).append(onlyValidObjects, rhs.onlyValidObjects).append(withoutReleased, rhs.withoutReleased).isEquals();
    }

}
