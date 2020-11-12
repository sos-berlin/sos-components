
package com.sos.jobscheduler.model.cluster;

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
    "idToUri",
    "activeId"
})
public class ClusterState {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    private ClusterType tYPE;
    /**
     * IdToUri
     * <p>
     * 
     * 
     */
    @JsonProperty("idToUri")
    private IdToUri idToUri;
    @JsonProperty("activeId")
    private String activeId;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterState() {
    }

    /**
     * 
     * @param idToUri
     * @param activeId
     * @param tYPE
     */
    public ClusterState(ClusterType tYPE, IdToUri idToUri, String activeId) {
        super();
        this.tYPE = tYPE;
        this.idToUri = idToUri;
        this.activeId = activeId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public ClusterType getTYPE() {
        return tYPE;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("TYPE")
    public void setTYPE(ClusterType tYPE) {
        this.tYPE = tYPE;
    }

    /**
     * IdToUri
     * <p>
     * 
     * 
     */
    @JsonProperty("idToUri")
    public IdToUri getIdToUri() {
        return idToUri;
    }

    /**
     * IdToUri
     * <p>
     * 
     * 
     */
    @JsonProperty("idToUri")
    public void setIdToUri(IdToUri idToUri) {
        this.idToUri = idToUri;
    }

    @JsonProperty("activeId")
    public String getActiveId() {
        return activeId;
    }

    @JsonProperty("activeId")
    public void setActiveId(String activeId) {
        this.activeId = activeId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("tYPE", tYPE).append("idToUri", idToUri).append("activeId", activeId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(idToUri).append(activeId).append(tYPE).toHashCode();
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
        return new EqualsBuilder().append(idToUri, rhs.idToUri).append(activeId, rhs.activeId).append(tYPE, rhs.tYPE).isEquals();
    }

}
