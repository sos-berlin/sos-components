
package com.sos.inventory.model.descriptor.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Joc Item of a Deployment Descriptor
 * <p>
 * JS7 JOC Item Descriptor Schema
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "members",
    "apiServers"
})
public class JocDescriptor {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    private Members members;
    @JsonProperty("apiServers")
    private ApiServers apiServers;

    /**
     * No args constructor for use in serialization
     * 
     */
    public JocDescriptor() {
    }

    /**
     * 
     * @param members
     * @param apiServers
     */
    public JocDescriptor(Members members, ApiServers apiServers) {
        super();
        this.members = members;
        this.apiServers = apiServers;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    public Members getMembers() {
        return members;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("members")
    public void setMembers(Members members) {
        this.members = members;
    }

    @JsonProperty("apiServers")
    public ApiServers getApiServers() {
        return apiServers;
    }

    @JsonProperty("apiServers")
    public void setApiServers(ApiServers apiServers) {
        this.apiServers = apiServers;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("members", members).append("apiServers", apiServers).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(apiServers).append(members).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JocDescriptor) == false) {
            return false;
        }
        JocDescriptor rhs = ((JocDescriptor) other);
        return new EqualsBuilder().append(apiServers, rhs.apiServers).append(members, rhs.members).isEquals();
    }

}
