
package com.sos.joc.model.publish;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * set versions
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deployments"
})
public class SetVersionsFilter {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployments")
    private List<DeploymentVersion> deployments = new ArrayList<DeploymentVersion>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployments")
    public List<DeploymentVersion> getDeployments() {
        return deployments;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployments")
    public void setDeployments(List<DeploymentVersion> deployments) {
        this.deployments = deployments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deployments", deployments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SetVersionsFilter) == false) {
            return false;
        }
        SetVersionsFilter rhs = ((SetVersionsFilter) other);
        return new EqualsBuilder().append(deployments, rhs.deployments).isEquals();
    }

}
