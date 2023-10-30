
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
    "clusterId",
    "instances"
})
public class Members {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterId")
    private String clusterId;
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
    public Members() {
    }

    /**
     * 
     * @param instances
     * @param clusterId
     */
    public Members(String clusterId, List<JocInstanceDescriptor> instances) {
        super();
        this.clusterId = clusterId;
        this.instances = instances;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterId")
    public String getClusterId() {
        return clusterId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterId")
    public void setClusterId(String clusterId) {
        this.clusterId = clusterId;
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
        return new ToStringBuilder(this).append("clusterId", clusterId).append("instances", instances).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(instances).append(clusterId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Members) == false) {
            return false;
        }
        Members rhs = ((Members) other);
        return new EqualsBuilder().append(instances, rhs.instances).append(clusterId, rhs.clusterId).isEquals();
    }

}
