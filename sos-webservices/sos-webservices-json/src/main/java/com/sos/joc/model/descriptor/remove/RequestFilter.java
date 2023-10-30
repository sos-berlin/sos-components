
package com.sos.joc.model.descriptor.remove;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * filter for descriptor rename
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "objectType"
})
public class RequestFilter
    extends com.sos.joc.model.descriptor.common.RequestFilter
{

    /**
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    private ConfigurationType objectType;

    /**
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public ConfigurationType getObjectType() {
        return objectType;
    }

    /**
     * configuration types
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("objectType")
    public void setObjectType(ConfigurationType objectType) {
        this.objectType = objectType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("objectType", objectType).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(objectType).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof RequestFilter) == false) {
            return false;
        }
        RequestFilter rhs = ((RequestFilter) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(objectType, rhs.objectType).isEquals();
    }

}
