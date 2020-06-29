
package com.sos.joc.model.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * switch joc cluster node
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "memberId"
})
public class ClusterSwitchMember {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("memberId")
    private String memberId;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("memberId")
    public String getMemberId() {
        return memberId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("memberId")
    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("memberId", memberId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(memberId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterSwitchMember) == false) {
            return false;
        }
        ClusterSwitchMember rhs = ((ClusterSwitchMember) other);
        return new EqualsBuilder().append(memberId, rhs.memberId).isEquals();
    }

}
