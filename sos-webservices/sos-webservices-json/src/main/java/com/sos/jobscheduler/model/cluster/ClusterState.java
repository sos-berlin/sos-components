
package com.sos.jobscheduler.model.cluster;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * overview
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "uris",
    "active"
})
public class ClusterState {

    @JsonProperty("TYPE")
    private ClusterType tYPE;
    @JsonProperty("uris")
    private List<String> uris = new ArrayList<String>();
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    private Integer active;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterState() {
    }

    /**
     * 
     * @param uris
     * @param active
     * @param tYPE
     */
    public ClusterState(ClusterType tYPE, List<String> uris, Integer active) {
        super();
        this.tYPE = tYPE;
        this.uris = uris;
        this.active = active;
    }

    @JsonProperty("TYPE")
    public ClusterType getTYPE() {
        return tYPE;
    }

    @JsonProperty("TYPE")
    public void setTYPE(ClusterType tYPE) {
        this.tYPE = tYPE;
    }

    @JsonProperty("uris")
    public List<String> getUris() {
        return uris;
    }

    @JsonProperty("uris")
    public void setUris(List<String> uris) {
        this.uris = uris;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    public Integer getActive() {
        return active;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("active")
    public void setActive(Integer active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("uris", uris).append("active", active).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(uris).append(active).append(tYPE).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterState) == false) {
            return false;
        }
        ClusterState rhs = ((ClusterState) other);
        return new EqualsBuilder().append(uris, rhs.uris).append(active, rhs.active).append(tYPE, rhs.tYPE).isEquals();
    }

}
