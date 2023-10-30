
package com.sos.controller.model.cluster;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "idToUri",
    "activeId",
    "clusterWatches",
    "clusterWatchId",
    "timing"
})
public class ClusterSetting {

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
    @JsonProperty("clusterWatches")
    private List<ClusterWatcher> clusterWatches = null;
    @JsonProperty("clusterWatchId")
    private String clusterWatchId;
    @JsonProperty("timing")
    private ClusterTiming timing;

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterSetting() {
    }

    /**
     * 
     * @param idToUri
     * @param timing
     * @param clusterWatches
     * @param activeId
     * @param clusterWatchId
     */
    public ClusterSetting(IdToUri idToUri, String activeId, List<ClusterWatcher> clusterWatches, String clusterWatchId, ClusterTiming timing) {
        super();
        this.idToUri = idToUri;
        this.activeId = activeId;
        this.clusterWatches = clusterWatches;
        this.clusterWatchId = clusterWatchId;
        this.timing = timing;
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

    @JsonProperty("clusterWatches")
    public List<ClusterWatcher> getClusterWatches() {
        return clusterWatches;
    }

    @JsonProperty("clusterWatches")
    public void setClusterWatches(List<ClusterWatcher> clusterWatches) {
        this.clusterWatches = clusterWatches;
    }

    @JsonProperty("clusterWatchId")
    public String getClusterWatchId() {
        return clusterWatchId;
    }

    @JsonProperty("clusterWatchId")
    public void setClusterWatchId(String clusterWatchId) {
        this.clusterWatchId = clusterWatchId;
    }

    @JsonProperty("timing")
    public ClusterTiming getTiming() {
        return timing;
    }

    @JsonProperty("timing")
    public void setTiming(ClusterTiming timing) {
        this.timing = timing;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("idToUri", idToUri).append("activeId", activeId).append("clusterWatches", clusterWatches).append("clusterWatchId", clusterWatchId).append("timing", timing).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(idToUri).append(clusterWatches).append(activeId).append(clusterWatchId).append(timing).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ClusterSetting) == false) {
            return false;
        }
        ClusterSetting rhs = ((ClusterSetting) other);
        return new EqualsBuilder().append(idToUri, rhs.idToUri).append(clusterWatches, rhs.clusterWatches).append(activeId, rhs.activeId).append(clusterWatchId, rhs.clusterWatchId).append(timing, rhs.timing).isEquals();
    }

}
