
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
    "valide",
    "deleted",
    "deployed",
    "deploymentId",
    "deployablesVersions"
})
public class ResponseDeployableTreeItem {

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
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    private String objectName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
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
    @JsonProperty("valide")
    private Boolean valide;
    @JsonProperty("deleted")
    private Boolean deleted;
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
    private Set<ResponseDeployableVersion> deployablesVersions = new LinkedHashSet<ResponseDeployableVersion>();

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

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    public String getObjectName() {
        return objectName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("objectName")
    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
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

    @JsonProperty("valide")
    public Boolean getValide() {
        return valide;
    }

    @JsonProperty("valide")
    public void setValide(Boolean valide) {
        this.valide = valide;
    }

    @JsonProperty("deleted")
    public Boolean getDeleted() {
        return deleted;
    }

    @JsonProperty("deleted")
    public void setDeleted(Boolean deleted) {
        this.deleted = deleted;
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
    public Set<ResponseDeployableVersion> getDeployablesVersions() {
        return deployablesVersions;
    }

    @JsonProperty("deployablesVersions")
    public void setDeployablesVersions(Set<ResponseDeployableVersion> deployablesVersions) {
        this.deployablesVersions = deployablesVersions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("folder", folder).append("objectName", objectName).append("account", account).append("objectType", objectType).append("valide", valide).append("deleted", deleted).append("deployed", deployed).append("deploymentId", deploymentId).append("deployablesVersions", deployablesVersions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(folder).append(deleted).append(deploymentId).append(objectName).append(deployed).append(id).append(valide).append(deployablesVersions).append(account).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseDeployableTreeItem) == false) {
            return false;
        }
        ResponseDeployableTreeItem rhs = ((ResponseDeployableTreeItem) other);
        return new EqualsBuilder().append(folder, rhs.folder).append(deleted, rhs.deleted).append(deploymentId, rhs.deploymentId).append(objectName, rhs.objectName).append(deployed, rhs.deployed).append(id, rhs.id).append(valide, rhs.valide).append(deployablesVersions, rhs.deployablesVersions).append(account, rhs.account).append(objectType, rhs.objectType).isEquals();
    }

}
