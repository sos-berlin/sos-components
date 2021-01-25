
package com.sos.controller.model.command;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.controller.model.cluster.ClusterWatcher;
import com.sos.controller.model.cluster.IdToUri;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * ClusterAppointNodes
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "TYPE",
    "idToUri",
    "activeId",
    "clusterWatches"
})
public class ClusterAppointNodes
    extends Command
{

    /**
     * IdToUri
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("idToUri")
    private IdToUri idToUri;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activeId")
    private String activeId;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterWatches")
    private List<ClusterWatcher> clusterWatches = null;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterAppointNodes() {
    }

    /**
     * 
     * @param idToUri
     * @param clusterWatches
     * @param activeId
     */
    public ClusterAppointNodes(IdToUri idToUri, String activeId, List<ClusterWatcher> clusterWatches) {
        super();
        this.idToUri = idToUri;
        this.activeId = activeId;
        this.clusterWatches = clusterWatches;
    }

    /**
     * IdToUri
     * <p>
     * 
     * (Required)
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
     * (Required)
     * 
     */
    @JsonProperty("idToUri")
    public void setIdToUri(IdToUri idToUri) {
        this.idToUri = idToUri;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activeId")
    public String getActiveId() {
        return activeId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("activeId")
    public void setActiveId(String activeId) {
        this.activeId = activeId;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterWatches")
    public List<ClusterWatcher> getClusterWatches() {
        return clusterWatches;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("clusterWatches")
    public void setClusterWatches(List<ClusterWatcher> clusterWatches) {
        this.clusterWatches = clusterWatches;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("idToUri", idToUri).append("activeId", activeId).append("clusterWatches", clusterWatches).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(idToUri).append(clusterWatches).append(activeId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterAppointNodes) == false) {
            return false;
        }
        ClusterAppointNodes rhs = ((ClusterAppointNodes) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(idToUri, rhs.idToUri).append(clusterWatches, rhs.clusterWatches).append(activeId, rhs.activeId).isEquals();
    }

}
