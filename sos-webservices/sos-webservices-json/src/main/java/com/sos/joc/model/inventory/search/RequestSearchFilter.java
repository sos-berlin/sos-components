
package com.sos.joc.model.inventory.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Filter Inventory search
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "controllerId",
    "deployedOrReleased",
    "undeployedOrUnreleased",
    "valid",
    "returnType"
})
public class RequestSearchFilter
    extends RequestBaseSearchFilter
{

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    @JsonProperty("deployedOrReleased")
    private Boolean deployedOrReleased = false;
    @JsonProperty("undeployedOrUnreleased")
    private Boolean undeployedOrUnreleased = false;
    /**
     * considered only if 'undeployedOrUnreleased' is true
     * 
     */
    @JsonProperty("valid")
    @JsonPropertyDescription("considered only if 'undeployedOrUnreleased' is true")
    private Boolean valid;
    /**
     * Inventory search return type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("returnType")
    private RequestSearchReturnType returnType;

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * controllerId
     * <p>
     * 
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    @JsonProperty("deployedOrReleased")
    public Boolean getDeployedOrReleased() {
        return deployedOrReleased;
    }

    @JsonProperty("deployedOrReleased")
    public void setDeployedOrReleased(Boolean deployedOrReleased) {
        this.deployedOrReleased = deployedOrReleased;
    }

    @JsonProperty("undeployedOrUnreleased")
    public Boolean getUndeployedOrUnreleased() {
        return undeployedOrUnreleased;
    }

    @JsonProperty("undeployedOrUnreleased")
    public void setUndeployedOrUnreleased(Boolean undeployedOrUnreleased) {
        this.undeployedOrUnreleased = undeployedOrUnreleased;
    }

    /**
     * considered only if 'undeployedOrUnreleased' is true
     * 
     */
    @JsonProperty("valid")
    public Boolean getValid() {
        return valid;
    }

    /**
     * considered only if 'undeployedOrUnreleased' is true
     * 
     */
    @JsonProperty("valid")
    public void setValid(Boolean valid) {
        this.valid = valid;
    }

    /**
     * Inventory search return type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("returnType")
    public RequestSearchReturnType getReturnType() {
        return returnType;
    }

    /**
     * Inventory search return type
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("returnType")
    public void setReturnType(RequestSearchReturnType returnType) {
        this.returnType = returnType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("controllerId", controllerId).append("deployedOrReleased", deployedOrReleased).append("undeployedOrUnreleased", undeployedOrUnreleased).append("valid", valid).append("returnType", returnType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(valid).append(controllerId).append(deployedOrReleased).append(returnType).append(undeployedOrUnreleased).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestSearchFilter) == false) {
            return false;
        }
        RequestSearchFilter rhs = ((RequestSearchFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(valid, rhs.valid).append(controllerId, rhs.controllerId).append(deployedOrReleased, rhs.deployedOrReleased).append(returnType, rhs.returnType).append(undeployedOrUnreleased, rhs.undeployedOrUnreleased).isEquals();
    }

}
