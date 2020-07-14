
package com.sos.joc.model.tree;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.common.JobSchedulerObjectType;
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
    "force",
    "folders",
    "forInventory",
    "forJoe"
})
public class TreeFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    private String jobschedulerId;
    /**
     * JobScheduler object types
     * <p>
     * 
     * 
     */
    @JsonProperty("types")
    private List<JobSchedulerObjectType> types = new ArrayList<JobSchedulerObjectType>();
    /**
     * force full tree
     * <p>
     * controls whether the folder permissions are use. If true the full tree will be returned
     * 
     */
    @JsonProperty("force")
    @JsonPropertyDescription("controls whether the folder permissions are use. If true the full tree will be returned")
    private Boolean force = false;
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
    @JsonProperty("forJoe")
    private Boolean forJoe = false;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public String getJobschedulerId() {
        return jobschedulerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("jobschedulerId")
    public void setJobschedulerId(String jobschedulerId) {
        this.jobschedulerId = jobschedulerId;
    }

    /**
     * JobScheduler object types
     * <p>
     * 
     * 
     */
    @JsonProperty("types")
    public List<JobSchedulerObjectType> getTypes() {
        return types;
    }

    /**
     * JobScheduler object types
     * <p>
     * 
     * 
     */
    @JsonProperty("types")
    public void setTypes(List<JobSchedulerObjectType> types) {
        this.types = types;
    }

    /**
     * force full tree
     * <p>
     * controls whether the folder permissions are use. If true the full tree will be returned
     * 
     */
    @JsonProperty("force")
    public Boolean getForce() {
        return force;
    }

    /**
     * force full tree
     * <p>
     * controls whether the folder permissions are use. If true the full tree will be returned
     * 
     */
    @JsonProperty("force")
    public void setForce(Boolean force) {
        this.force = force;
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

    @JsonProperty("forJoe")
    public Boolean getForJoe() {
        return forJoe;
    }

    @JsonProperty("forJoe")
    public void setForJoe(Boolean forJoe) {
        this.forJoe = forJoe;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jobschedulerId", jobschedulerId).append("types", types).append("force", force).append("folders", folders).append("forInventory", forInventory).append("forJoe", forJoe).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(types).append(folders).append(forInventory).append(forJoe).append(force).append(jobschedulerId).toHashCode();
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
        return new EqualsBuilder().append(types, rhs.types).append(folders, rhs.folders).append(forInventory, rhs.forInventory).append(forJoe, rhs.forJoe).append(force, rhs.force).append(jobschedulerId, rhs.jobschedulerId).isEquals();
    }

}
