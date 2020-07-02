
package com.sos.joc.model.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.cluster.common.ClusterServices;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * restart joc service such as cluster, history, dailyplan
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type"
})
public class ClusterRestart {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ClusterServices type;

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ClusterServices getType() {
        return type;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public void setType(ClusterServices type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("type", type).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(type).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterRestart) == false) {
            return false;
        }
        ClusterRestart rhs = ((ClusterRestart) other);
        return new EqualsBuilder().append(type, rhs.type).isEquals();
    }

}
