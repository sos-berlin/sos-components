
package com.sos.inventory.model.descriptor.joc;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "instances"
})
public class ApiServers {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instances")
    private List<JocInstanceDescriptor> instances = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ApiServers() {
    }

    /**
     * 
     * @param instances
     */
    public ApiServers(List<JocInstanceDescriptor> instances) {
        super();
        this.instances = instances;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instances")
    public List<JocInstanceDescriptor> getInstances() {
        return instances;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("instances")
    public void setInstances(List<JocInstanceDescriptor> instances) {
        this.instances = instances;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("instances", instances).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instances).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ApiServers) == false) {
            return false;
        }
        ApiServers rhs = ((ApiServers) other);
        return new EqualsBuilder().append(instances, rhs.instances).isEquals();
    }

}
