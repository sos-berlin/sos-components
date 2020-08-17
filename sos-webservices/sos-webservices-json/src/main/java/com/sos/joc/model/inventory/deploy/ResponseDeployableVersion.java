
package com.sos.joc.model.inventory.deploy;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sos.joc.model.inventory.common.ResponseItemDeployment;
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
    "deploymentPath",
    "deploymentOperation",
    "versionDate",
    "versions"
})
public class ResponseDeployableVersion {

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
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("deploymentPath")
    @JsonPropertyDescription("absolute path of a JobScheduler object.")
    private String deploymentPath;
    @JsonProperty("deploymentOperation")
    private String deploymentOperation;
    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date versionDate;
    @JsonProperty("versions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<ResponseItemDeployment> versions = new LinkedHashSet<ResponseItemDeployment>();

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
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("deploymentPath")
    public String getDeploymentPath() {
        return deploymentPath;
    }

    /**
     * path
     * <p>
     * absolute path of a JobScheduler object.
     * 
     */
    @JsonProperty("deploymentPath")
    public void setDeploymentPath(String deploymentPath) {
        this.deploymentPath = deploymentPath;
    }

    @JsonProperty("deploymentOperation")
    public String getDeploymentOperation() {
        return deploymentOperation;
    }

    @JsonProperty("deploymentOperation")
    public void setDeploymentOperation(String deploymentOperation) {
        this.deploymentOperation = deploymentOperation;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    public Date getVersionDate() {
        return versionDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("versionDate")
    public void setVersionDate(Date versionDate) {
        this.versionDate = versionDate;
    }

    @JsonProperty("versions")
    public Set<ResponseItemDeployment> getVersions() {
        return versions;
    }

    @JsonProperty("versions")
    public void setVersions(Set<ResponseItemDeployment> versions) {
        this.versions = versions;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", id).append("deploymentId", deploymentId).append("deploymentPath", deploymentPath).append("deploymentOperation", deploymentOperation).append("versionDate", versionDate).append("versions", versions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deploymentPath).append(deploymentOperation).append(versions).append(deploymentId).append(id).append(versionDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResponseDeployableVersion) == false) {
            return false;
        }
        ResponseDeployableVersion rhs = ((ResponseDeployableVersion) other);
        return new EqualsBuilder().append(deploymentPath, rhs.deploymentPath).append(deploymentOperation, rhs.deploymentOperation).append(versions, rhs.versions).append(deploymentId, rhs.deploymentId).append(id, rhs.id).append(versionDate, rhs.versionDate).isEquals();
    }

}
