
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
    "jobschedulerId",
    "types",
    "folders",
    "forInventory"
})
public class TreeFilter {

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
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
    @JsonProperty("forInventory")
    private Boolean forInventory = false;

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * filename
     * <p>
     * 
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
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

    @JsonProperty("forInventory")
    public Boolean getForInventory() {
        return forInventory;
    }

    @JsonProperty("forInventory")
    public void setForInventory(Boolean forInventory) {
        this.forInventory = forInventory;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("types", types).append("folders", folders).append("forInventory", forInventory).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(types).append(folders).append(jobschedulerId).append(forInventory).toHashCode();
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
        return new EqualsBuilder().append(types, rhs.types).append(folders, rhs.folders).append(jobschedulerId, rhs.jobschedulerId).append(forInventory, rhs.forInventory).isEquals();
    }

}
