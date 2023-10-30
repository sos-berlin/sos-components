
package com.sos.inventory.model.descriptor.agent;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Controller Reference Descriptor
 * <p>
 * JS7 Controller Reference Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "controllerId",
    "members"
})
public class ControllerRefDescriptor {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    private String controllerId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    private List<AgentDescriptor> members = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ControllerRefDescriptor() {
    }

    /**
     * 
     * @param controllerId
     * @param members
     */
    public ControllerRefDescriptor(String controllerId, List<AgentDescriptor> members) {
        super();
        this.controllerId = controllerId;
        this.members = members;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public String getControllerId() {
        return controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerId")
    public void setControllerId(String controllerId) {
        this.controllerId = controllerId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    public List<AgentDescriptor> getMembers() {
        return members;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    public void setMembers(List<AgentDescriptor> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerId", controllerId).append("members", members).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerId).append(members).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerRefDescriptor) == false) {
            return false;
        }
        ControllerRefDescriptor rhs = ((ControllerRefDescriptor) other);
        return new EqualsBuilder().append(controllerId, rhs.controllerId).append(members, rhs.members).isEquals();
    }

}
