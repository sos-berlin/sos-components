
package com.sos.joc.model.inventory.deploy;

import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.common.JobSchedulerObjectType;
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
    "folder",
    "objectName",
    "account",
    "objectType",
    "deployed",
    "deploymentId",
    "deployablesVersions"
})
public class DeployableTreeItem {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("id")
    private Long id;
    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("folder")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String folder;
    @JsonProperty("objectName")
    private String objectName;
    @JsonProperty("account")
    private String account;
    /**
     * JobScheduler object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    private JobSchedulerObjectType objectType;
    @JsonProperty("deployed")
    private Boolean deployed;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("deploymentId")
    private Long deploymentId;
    @JsonProperty("deployablesVersions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<DeployableVersion> deployablesVersions = new LinkedHashSet<DeployableVersion>();

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
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("folder")
    public String getFolder() {
        return folder;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("folder")
    public void setFolder(String folder) {
        this.folder = folder;
    }

    @JsonProperty("objectName")
    public String getObjectName() {
        return objectName;
    }

    @JsonProperty("objectName")
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
    }

    /**
     * JobScheduler object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public JobSchedulerObjectType getObjectType() {
        return objectType;
    }

    /**
     * JobScheduler object type
     * <p>
     * 
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(JobSchedulerObjectType objectType) {
        this.objectType = objectType;
    }

    @JsonProperty("deployed")
    public Boolean getDeployed() {
        return deployed;
    }

    @JsonProperty("deployed")
    public void setDeployed(Boolean deployed) {
        this.deployed = deployed;
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

    @JsonProperty("deployablesVersions")
    public Set<DeployableVersion> getDeployablesVersions() {
        return deployablesVersions;
    }

    @JsonProperty("deployablesVersions")
    public void setDeployablesVersions(Set<DeployableVersion> deployablesVersions) {
        this.deployablesVersions = deployablesVersions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("folder", folder).append("objectName", objectName).append("account", account).append("objectType", objectType).append("deployed", deployed).append("deploymentId", deploymentId).append("deployablesVersions", deployablesVersions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(deploymentId).append(objectName).append(deployed).append(id).append(deployablesVersions).append(account).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof DeployableTreeItem) == false) {
            return false;
        }
        DeployableTreeItem rhs = ((DeployableTreeItem) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(deploymentId, rhs.deploymentId).append(objectName, rhs.objectName).append(deployed, rhs.deployed).append(id, rhs.id).append(deployablesVersions, rhs.deployablesVersions).append(account, rhs.account).append(objectType, rhs.objectType).isEquals();
    }

}
