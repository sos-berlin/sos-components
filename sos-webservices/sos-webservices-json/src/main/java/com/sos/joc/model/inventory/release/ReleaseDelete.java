
package com.sos.joc.model.inventory.release;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurationId"
})
public class ReleaseDelete {

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

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configurationId", configurationId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurationId).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ReleaseDelete) == false) {
            return false;
        }
        ReleaseDelete rhs = ((ReleaseDelete) other);
        return new EqualsBuilder().append(configurationId, rhs.configurationId).isEquals();
    }

}
