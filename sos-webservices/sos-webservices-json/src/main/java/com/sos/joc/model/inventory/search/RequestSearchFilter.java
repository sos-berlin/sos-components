
package com.sos.joc.model.inventory.search;

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
 * Filter Inventory search
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "search",
    "folders",
    "deployedOrReleased",
    "returnType",
    "advanced"
})
public class RequestSearchFilter {

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("search")
    private String search;
    /**
     * folders
     * <p>
     * 
     * 
     */
    @JsonProperty("folders")
    private List<Folder> folders = new ArrayList<Folder>();
    @JsonProperty("deployedOrReleased")
    private Boolean deployedOrReleased = false;
    /**
     * Inventory search return type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("returnType")
    private RequestSearchReturnType returnType;
    /**
     * Inventory advanced search
     * <p>
     * 
     * 
     */
    @JsonProperty("advanced")
    private RequestSearchAdvancedItem advanced;

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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("search")
    public String getSearch() {
        return search;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("search")
    public void setSearch(String search) {
        this.search = search;
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

    @JsonProperty("deployedOrReleased")
    public Boolean getDeployedOrReleased() {
        return deployedOrReleased;
    }

    @JsonProperty("deployedOrReleased")
    public void setDeployedOrReleased(Boolean deployedOrReleased) {
        this.deployedOrReleased = deployedOrReleased;
    }

    /**
     * Inventory search return type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("returnType")
    public RequestSearchReturnType getReturnType() {
        return returnType;
    }

    /**
     * Inventory search return type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("returnType")
    public void setReturnType(RequestSearchReturnType returnType) {
        this.returnType = returnType;
    }

    /**
     * Inventory advanced search
     * <p>
     * 
     * 
     */
    @JsonProperty("advanced")
    public RequestSearchAdvancedItem getAdvanced() {
        return advanced;
    }

    /**
     * Inventory advanced search
     * <p>
     * 
     * 
     */
    @JsonProperty("advanced")
    public void setAdvanced(RequestSearchAdvancedItem advanced) {
        this.advanced = advanced;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("search", search).append("folders", folders).append("deployedOrReleased", deployedOrReleased).append("returnType", returnType).append("advanced", advanced).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(search).append(folders).append(controllerId).append(advanced).append(deployedOrReleased).append(returnType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestSearchFilter) == false) {
            return false;
        }
        RequestSearchFilter rhs = ((RequestSearchFilter) other);
        return new EqualsBuilder().append(search, rhs.search).append(folders, rhs.folders).append(controllerId, rhs.controllerId).append(advanced, rhs.advanced).append(deployedOrReleased, rhs.deployedOrReleased).append(returnType, rhs.returnType).isEquals();
    }

}
