
package com.sos.joc.model.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.cluster.common.ClusterServices;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Immediately run JOC services such as dailyplan,cleanup
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "type"
})
public class ClusterServiceRun {

    /**
     * JOC cluster services
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    private ClusterServices type;

    /**
     * JOC cluster services
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("type")
    public ClusterServices getType() {
        return type;
    }

    /**
     * JOC cluster services
     * <p>
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
        if ((other instanceof ClusterServiceRun) == false) {
            return false;
        }
        ClusterServiceRun rhs = ((ClusterServiceRun) other);
        return new EqualsBuilder().append(type, rhs.type).isEquals();
    }

}
