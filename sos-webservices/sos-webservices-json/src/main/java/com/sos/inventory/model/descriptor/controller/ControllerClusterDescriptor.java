
package com.sos.inventory.model.descriptor.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Controller Cluster Item of a Deployment Descriptor
 * <p>
 * JS7 Controller Cluster Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jocRef",
    "controllerId",
    "primary",
    "secondary"
})
public class ControllerClusterDescriptor {

    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("jocRef")
    @JsonPropertyDescription("jocClusterId of the JOC the controller should be managed by.")
    private String jocRef;
    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    @JsonPropertyDescription("jocClusterId of the JOC the controller should be managed by.")
    private String controllerId;
    /**
     * Controller Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Descriptor Schema
     * (Required)
     * 
     */
    @JsonProperty("primary")
    @JsonPropertyDescription("JS7 Controller Descriptor Schema")
    private ControllerDescriptor primary;
    /**
     * Controller Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Descriptor Schema
     * 
     */
    @JsonProperty("secondary")
    @JsonPropertyDescription("JS7 Controller Descriptor Schema")
    private ControllerDescriptor secondary;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ControllerClusterDescriptor() {
    }

    /**
     * 
     * @param secondary
     * @param controllerId
     * @param jocRef
     * @param primary
     */
    public ControllerClusterDescriptor(String jocRef, String controllerId, ControllerDescriptor primary, ControllerDescriptor secondary) {
        super();
        this.jocRef = jocRef;
        this.controllerId = controllerId;
        this.primary = primary;
        this.secondary = secondary;
    }

    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("jocRef")
    public String getJocRef() {
        return jocRef;
    }

    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("jocRef")
    public void setJocRef(String jocRef) {
        this.jocRef = jocRef;
    }

    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * jocClusterId of the JOC the controller should be managed by.
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * Controller Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Descriptor Schema
     * (Required)
     * 
     */
    @JsonProperty("primary")
    public ControllerDescriptor getPrimary() {
        return primary;
    }

    /**
     * Controller Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Descriptor Schema
     * (Required)
     * 
     */
    @JsonProperty("primary")
    public void setPrimary(ControllerDescriptor primary) {
        this.primary = primary;
    }

    /**
     * Controller Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Descriptor Schema
     * 
     */
    @JsonProperty("secondary")
    public ControllerDescriptor getSecondary() {
        return secondary;
    }

    /**
     * Controller Item of a Deployment Descriptor
     * <p>
     * JS7 Controller Descriptor Schema
     * 
     */
    @JsonProperty("secondary")
    public void setSecondary(ControllerDescriptor secondary) {
        this.secondary = secondary;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("jocRef", jocRef).append("controllerId", controllerId).append("primary", primary).append("secondary", secondary).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(secondary).append(jocRef).append(controllerId).append(primary).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerClusterDescriptor) == false) {
            return false;
        }
        ControllerClusterDescriptor rhs = ((ControllerClusterDescriptor) other);
        return new EqualsBuilder().append(secondary, rhs.secondary).append(jocRef, rhs.jocRef).append(controllerId, rhs.controllerId).append(primary, rhs.primary).isEquals();
    }

}
