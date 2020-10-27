
package com.sos.joc.model.publish;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * set version
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "version",
    "deployments"
})
public class SetVersionFilter {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    private String version;
    @JsonProperty("deployments")
    private List<Long> deployments = new ArrayList<Long>();

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public String getVersion() {
        return version;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("version")
    public void setVersion(String version) {
        this.version = version;
    }

    @JsonProperty("deployments")
    public List<Long> getDeployments() {
        return deployments;
    }

    @JsonProperty("deployments")
    public void setDeployments(List<Long> deployments) {
        this.deployments = deployments;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("version", version).append("deployments", deployments).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(version).append(deployments).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SetVersionFilter) == false) {
            return false;
        }
        SetVersionFilter rhs = ((SetVersionFilter) other);
        return new EqualsBuilder().append(version, rhs.version).append(deployments, rhs.deployments).isEquals();
    }

}
