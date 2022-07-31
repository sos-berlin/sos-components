
package com.sos.joc.model.inventory.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("controllerId", controllerId).append("deployedOrReleased", deployedOrReleased).append("returnType", returnType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(controllerId).append(deployedOrReleased).append(returnType).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(controllerId, rhs.controllerId).append(deployedOrReleased, rhs.deployedOrReleased).append(returnType, rhs.returnType).isEquals();
    }

}
