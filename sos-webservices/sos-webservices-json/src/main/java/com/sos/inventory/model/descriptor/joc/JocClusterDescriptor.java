
package com.sos.inventory.model.descriptor.joc;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Jocs Item of a Deployment Descriptor
 * <p>
 * JS7 JOC Cluster Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "clusterId",
    "members"
})
public class JocClusterDescriptor {

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
    @JsonProperty("members")
    private List<JocDescriptor> members = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JocClusterDescriptor() {
    }

    /**
     * 
     * @param members
     * @param clusterId
     */
    public JocClusterDescriptor(String clusterId, List<JocDescriptor> members) {
        super();
        this.clusterId = clusterId;
        this.members = members;
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
    @JsonProperty("members")
    public List<JocDescriptor> getMembers() {
        return members;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    public void setMembers(List<JocDescriptor> members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("clusterId", clusterId).append("members", members).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(clusterId).append(members).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocClusterDescriptor) == false) {
            return false;
        }
        JocClusterDescriptor rhs = ((JocClusterDescriptor) other);
        return new EqualsBuilder().append(clusterId, rhs.clusterId).append(members, rhs.members).isEquals();
    }

}
