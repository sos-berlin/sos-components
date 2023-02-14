
package com.sos.inventory.model.descriptor.controller;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Controller Item of a Deployment Descriptor
 * <p>
 * JS7 Controller Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "instanceType"
})
public class ControllerDescriptor {

    @JsonProperty("instanceType")
    private Instance instanceType;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ControllerDescriptor() {
    }

    /**
     * 
     * @param instanceType
     */
    public ControllerDescriptor(Instance instanceType) {
        super();
        this.instanceType = instanceType;
    }

    @JsonProperty("instanceType")
    public Instance getInstanceType() {
        return instanceType;
    }

    @JsonProperty("instanceType")
    public void setInstanceType(Instance instanceType) {
        this.instanceType = instanceType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("instanceType", instanceType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instanceType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerDescriptor) == false) {
            return false;
        }
        ControllerDescriptor rhs = ((ControllerDescriptor) other);
        return new EqualsBuilder().append(instanceType, rhs.instanceType).isEquals();
    }

}
