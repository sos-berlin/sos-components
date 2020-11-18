
package com.sos.jobscheduler.model.jobclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JS JobClass Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class JobClassEdit
    extends ConfigurationObject
{

    /**
     * jobClass
     * <p>
     * deploy object with fixed property 'TYPE':'jobClass'
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'jobClass'")
    private JobClass configuration;

    /**
     * jobClass
     * <p>
     * deploy object with fixed property 'TYPE':'jobClass'
     * 
     */
    @JsonProperty("configuration")
    public JobClass getConfiguration() {
        return configuration;
    }

    /**
     * jobClass
     * <p>
     * deploy object with fixed property 'TYPE':'jobClass'
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(JobClass configuration) {
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
        if ((other instanceof JobClassEdit) == false) {
            return false;
        }
        JobClassEdit rhs = ((JobClassEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
