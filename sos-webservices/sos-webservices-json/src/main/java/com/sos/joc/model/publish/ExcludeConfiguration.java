
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "invConfigurationId"
})
public class ExcludeConfiguration {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    private String path;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("invConfigurationId")
    private Long invConfigurationId;

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public String getPath() {
        return path;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("path")
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("invConfigurationId")
    public Long getInvConfigurationId() {
        return invConfigurationId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("invConfigurationId")
    public void setInvConfigurationId(Long invConfigurationId) {
        this.invConfigurationId = invConfigurationId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("invConfigurationId", invConfigurationId).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(invConfigurationId).append(path).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ExcludeConfiguration) == false) {
            return false;
        }
        ExcludeConfiguration rhs = ((ExcludeConfiguration) other);
        return new EqualsBuilder().append(invConfigurationId, rhs.invConfigurationId).append(path, rhs.path).isEquals();
    }

}
