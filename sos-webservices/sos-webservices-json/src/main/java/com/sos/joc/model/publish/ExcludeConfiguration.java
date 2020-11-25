
package com.sos.joc.model.publish;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "path",
    "deployType"
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
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployType")
    private ConfigurationType deployType;

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
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployType")
    public ConfigurationType getDeployType() {
        return deployType;
    }

    /**
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("deployType")
    public void setDeployType(ConfigurationType deployType) {
        this.deployType = deployType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("path", path).append("deployType", deployType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(path).append(deployType).toHashCode();
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
        return new EqualsBuilder().append(path, rhs.path).append(deployType, rhs.deployType).isEquals();
    }

}
