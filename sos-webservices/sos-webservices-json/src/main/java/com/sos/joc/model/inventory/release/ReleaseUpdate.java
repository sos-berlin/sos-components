
package com.sos.joc.model.inventory.release;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurationId",
    "releaseId"
})
public class ReleaseUpdate {

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationId")
    private Long configurationId;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("releaseId")
    private Long releaseId;

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationId")
    public Long getConfigurationId() {
        return configurationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("configurationId")
    public void setConfigurationId(Long configurationId) {
        this.configurationId = configurationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("releaseId")
    public Long getReleaseId() {
        return releaseId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("releaseId")
    public void setReleaseId(Long releaseId) {
        this.releaseId = releaseId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configurationId", configurationId).append("releaseId", releaseId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(releaseId).append(configurationId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleaseUpdate) == false) {
            return false;
        }
        ReleaseUpdate rhs = ((ReleaseUpdate) other);
        return new EqualsBuilder().append(releaseId, rhs.releaseId).append(configurationId, rhs.configurationId).isEquals();
    }

}
