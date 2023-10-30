
package com.sos.inventory.model.descriptor.agent;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "controllerRefs"
})
public class AgentsDescriptor {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerRefs")
    private List<ControllerRefDescriptor> controllerRefs = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AgentsDescriptor() {
    }

    /**
     * 
     * @param controllerRefs
     */
    public AgentsDescriptor(List<ControllerRefDescriptor> controllerRefs) {
        super();
        this.controllerRefs = controllerRefs;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerRefs")
    public List<ControllerRefDescriptor> getControllerRefs() {
        return controllerRefs;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("controllerRefs")
    public void setControllerRefs(List<ControllerRefDescriptor> controllerRefs) {
        this.controllerRefs = controllerRefs;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("controllerRefs", controllerRefs).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(controllerRefs).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AgentsDescriptor) == false) {
            return false;
        }
        AgentsDescriptor rhs = ((AgentsDescriptor) other);
        return new EqualsBuilder().append(controllerRefs, rhs.controllerRefs).isEquals();
    }

}
