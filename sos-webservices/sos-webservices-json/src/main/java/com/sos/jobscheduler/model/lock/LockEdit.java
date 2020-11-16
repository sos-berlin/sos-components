
package com.sos.jobscheduler.model.lock;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JS Lock Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "configuration"
})
public class LockEdit
    extends ConfigurationObject
{

    /**
     * lock
     * <p>
     * deploy object with fixed property 'TYPE':'Lock'
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'Lock'")
    private Lock configuration;

    /**
     * lock
     * <p>
     * deploy object with fixed property 'TYPE':'Lock'
     * 
     */
    @JsonProperty("configuration")
    public Lock getConfiguration() {
        return configuration;
    }

    /**
     * lock
     * <p>
     * deploy object with fixed property 'TYPE':'Lock'
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(Lock configuration) {
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
        if ((other instanceof LockEdit) == false) {
            return false;
        }
        LockEdit rhs = ((LockEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
