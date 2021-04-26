
package com.sos.joc.model.inventory.jobresource;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JobResource Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class JobResourceEdit
    extends ConfigurationObject
{

    /**
     * JobResource
     * <p>
     * deploy object with fixed property 'TYPE':'JobResource'
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'JobResource'")
    private JobResource configuration;

    /**
     * JobResource
     * <p>
     * deploy object with fixed property 'TYPE':'JobResource'
     * 
     */
    @JsonProperty("configuration")
    public JobResource getConfiguration() {
        return configuration;
    }

    /**
     * JobResource
     * <p>
     * deploy object with fixed property 'TYPE':'JobResource'
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(JobResource configuration) {
        this.configuration = configuration;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("configuration", configuration).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(configuration).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof JobResourceEdit) == false) {
            return false;
        }
        JobResourceEdit rhs = ((JobResourceEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
