
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Shallow Copy Export Filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deployables",
    "releasables"
})
public class ExportShallowCopy {

    /**
     * Filter for valid and invalid Deployable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("deployables")
    private DeployablesFilter deployables;
    /**
     * Filter for Releasable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("releasables")
    private ReleasablesFilter releasables;

    /**
     * Filter for valid and invalid Deployable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("deployables")
    public DeployablesFilter getDeployables() {
        return deployables;
    }

    /**
     * Filter for valid and invalid Deployable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("deployables")
    public void setDeployables(DeployablesFilter deployables) {
        this.deployables = deployables;
    }

    /**
     * Filter for Releasable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("releasables")
    public ReleasablesFilter getReleasables() {
        return releasables;
    }

    /**
     * Filter for Releasable Objects
     * <p>
     * 
     * 
     */
    @JsonProperty("releasables")
    public void setReleasables(ReleasablesFilter releasables) {
        this.releasables = releasables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deployables", deployables).append("releasables", releasables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(deployables).append(releasables).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportShallowCopy) == false) {
            return false;
        }
        ExportShallowCopy rhs = ((ExportShallowCopy) other);
        return new EqualsBuilder().append(deployables, rhs.deployables).append(releasables, rhs.releasables).isEquals();
    }

}
