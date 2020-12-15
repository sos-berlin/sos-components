
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ExportForSigningFilter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "deployables"
})
public class ExportForSigning {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * Export Deployables
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployables")
    private ExportDeployables deployables;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * Export Deployables
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployables")
    public ExportDeployables getDeployables() {
        return deployables;
    }

    /**
     * Export Deployables
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployables")
    public void setDeployables(ExportDeployables deployables) {
        this.deployables = deployables;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("deployables", deployables).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(deployables).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExportForSigning) == false) {
            return false;
        }
        ExportForSigning rhs = ((ExportForSigning) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(deployables, rhs.deployables).isEquals();
    }

}
