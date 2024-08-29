
package com.sos.joc.model.inventory.dependencies;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * dependencies
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configurations"
})
public class GetDependenciesRequest {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    private List<RequestItem> configurations = new ArrayList<RequestItem>();

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    public List<RequestItem> getConfigurations() {
        return configurations;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("configurations")
    public void setConfigurations(List<RequestItem> configurations) {
        this.configurations = configurations;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("configurations", configurations).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(configurations).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GetDependenciesRequest) == false) {
            return false;
        }
        GetDependenciesRequest rhs = ((GetDependenciesRequest) other);
        return new EqualsBuilder().append(configurations, rhs.configurations).isEquals();
    }

}
