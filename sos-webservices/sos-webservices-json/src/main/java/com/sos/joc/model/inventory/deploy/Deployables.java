
package com.sos.joc.model.inventory.deploy;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
    "deliveryDate",
    "deployables",
    "deployablesVersions"
})
public class Deployables {

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ")
    private Date deliveryDate;
    @JsonProperty("deployables")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<DeployableTreeItem> deployables = new LinkedHashSet<DeployableTreeItem>();
    @JsonProperty("deployablesVersions")
    @JsonDeserialize(as = java.util.LinkedHashSet.class)
    private Set<DeployableVersion> deployablesVersions = new LinkedHashSet<DeployableVersion>();

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * delivery date
     * <p>
     * Current date of the JOC server/REST service. Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    @JsonProperty("deployables")
    public Set<DeployableTreeItem> getDeployables() {
        return deployables;
    }

    @JsonProperty("deployables")
    public void setDeployables(Set<DeployableTreeItem> deployables) {
        this.deployables = deployables;
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
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("deployables", deployables).append("deployablesVersions", deployablesVersions).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployables).append(deliveryDate).append(deployablesVersions).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Deployables) == false) {
            return false;
        }
        Deployables rhs = ((Deployables) other);
        return new EqualsBuilder().append(deployables, rhs.deployables).append(deliveryDate, rhs.deliveryDate).append(deployablesVersions, rhs.deployablesVersions).isEquals();
    }

}
