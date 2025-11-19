
package com.sos.joc.model.inventory.dependencies.get;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * JS Object configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "enforce"
})
public class EnforcedConfigurationObject
    extends ConfigurationObject
{

    @JsonProperty("enforce")
    private Boolean enforce;

    @JsonProperty("enforce")
    public Boolean getEnforce() {
        return enforce;
    }

    @JsonProperty("enforce")
    public void setEnforce(Boolean enforce) {
        this.enforce = enforce;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("enforce", enforce).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(enforce).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof EnforcedConfigurationObject) == false) {
            return false;
        }
        EnforcedConfigurationObject rhs = ((EnforcedConfigurationObject) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(enforce, rhs.enforce).isEquals();
    }

}
