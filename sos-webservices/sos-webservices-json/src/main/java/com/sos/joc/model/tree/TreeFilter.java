
package com.sos.joc.model.tree;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * treeFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "types",
    "folders",
    "onlyValidObjects",
    "onlyWithAssignReference",
    "forInventory",
    "forInventoryTrash"
})
public class TreeFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("types")
    private List<TreeType> types = new ArrayList<TreeType>();
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("onlyValidObjects")
    private Boolean onlyValidObjects = false;
    @JsonProperty("onlyWithAssignReference")
    private Boolean onlyWithAssignReference = false;
    @JsonProperty("forInventory")
    private Boolean forInventory = false;
    @JsonProperty("forInventoryTrash")
    private Boolean forInventoryTrash = false;

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

    @JsonProperty("types")
    public List<TreeType> getTypes() {
        return types;
    }

    @JsonProperty("types")
    public void setTypes(List<TreeType> types) {
        this.types = types;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public List<Folder> getFolders() {
        return folders;
    }

    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }

    @JsonProperty("onlyValidObjects")
    public Boolean getOnlyValidObjects() {
        return onlyValidObjects;
    }

    @JsonProperty("onlyValidObjects")
    public void setOnlyValidObjects(Boolean onlyValidObjects) {
        this.onlyValidObjects = onlyValidObjects;
    }

    @JsonProperty("onlyWithAssignReference")
    public Boolean getOnlyWithAssignReference() {
        return onlyWithAssignReference;
    }

    @JsonProperty("onlyWithAssignReference")
    public void setOnlyWithAssignReference(Boolean onlyWithAssignReference) {
        this.onlyWithAssignReference = onlyWithAssignReference;
    }

    @JsonProperty("forInventory")
    public Boolean getForInventory() {
        return forInventory;
    }

    @JsonProperty("forInventory")
    public void setForInventory(Boolean forInventory) {
        this.forInventory = forInventory;
    }

    @JsonProperty("forInventoryTrash")
    public Boolean getForInventoryTrash() {
        return forInventoryTrash;
    }

    @JsonProperty("forInventoryTrash")
    public void setForInventoryTrash(Boolean forInventoryTrash) {
        this.forInventoryTrash = forInventoryTrash;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("types", types).append("folders", folders).append("onlyValidObjects", onlyValidObjects).append("onlyWithAssignReference", onlyWithAssignReference).append("forInventory", forInventory).append("forInventoryTrash", forInventoryTrash).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(forInventoryTrash).append(types).append(folders).append(controllerId).append(forInventory).append(onlyValidObjects).append(onlyWithAssignReference).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof TreeFilter) == false) {
            return false;
        }
        TreeFilter rhs = ((TreeFilter) other);
        return new EqualsBuilder().append(forInventoryTrash, rhs.forInventoryTrash).append(types, rhs.types).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(forInventory, rhs.forInventory).append(onlyValidObjects, rhs.onlyValidObjects).append(onlyWithAssignReference, rhs.onlyWithAssignReference).isEquals();
    }

}
