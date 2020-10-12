
package com.sos.jobscheduler.model.cluster;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "idToUri",
    "activeId"
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

    /**
     * No args constructor for use in serialization
     * 
     */
    public ClusterSetting() {
    }

    /**
     * 
     * @param idToUri
     * @param activeId
     */
    public ClusterSetting(IdToUri idToUri, String activeId) {
        super();
        this.idToUri = idToUri;
        this.activeId = activeId;
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
        return new ToStringBuilder(this).append("idToUri", idToUri).append("activeId", activeId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(idToUri).append(activeId).toHashCode();
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
        return new EqualsBuilder().append(idToUri, rhs.idToUri).append(activeId, rhs.activeId).isEquals();
    }

}
