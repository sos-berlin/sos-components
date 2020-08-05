
package com.sos.joc.model.inventory.deploy;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.inventory.common.ItemDeployment;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * filter for joe requests
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "deploymentId",
    "folder",
    "deployments"
})
public class DeployableVersion {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    private Long deploymentId;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date folder;
    @JsonProperty("deployments")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ItemDeployment> deployments = new LinkedHashSet<ItemDeployment>();

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public Long getId() {
        return id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    public Long getDeploymentId() {
        return deploymentId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    public void setDeploymentId(Long deploymentId) {
        this.deploymentId = deploymentId;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("folder")
    public Date getFolder() {
        return folder;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("folder")
    public void setFolder(Date folder) {
        this.folder = folder;
    }

    @JsonProperty("deployments")
    public Set<ItemDeployment> getDeployments() {
        return deployments;
    }

    @JsonProperty("deployments")
    public void setDeployments(Set<ItemDeployment> deployments) {
        this.deployments = deployments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("deploymentId", deploymentId).append("folder", folder).append("deployments", deployments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployments).append(id).append(folder).append(deploymentId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployableVersion) == false) {
            return false;
        }
        DeployableVersion rhs = ((DeployableVersion) other);
        return new EqualsBuilder().append(deployments, rhs.deployments).append(id, rhs.id).append(folder, rhs.folder).append(deploymentId, rhs.deploymentId).isEquals();
    }

}
