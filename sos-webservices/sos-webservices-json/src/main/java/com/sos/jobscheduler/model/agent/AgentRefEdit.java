
package com.sos.jobscheduler.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.inventory.ConfigurationObject;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * JS AgentRef Edit configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "configuration"
})
public class AgentRefEdit
    extends ConfigurationObject
{

    /**
     * agent
     * <p>
     * deploy object with fixed property 'TYPE':'AgentRef'
     * 
     */
    @JsonProperty("configuration")
    @JsonPropertyDescription("deploy object with fixed property 'TYPE':'AgentRef'")
    private AgentRef configuration;

    /**
     * agent
     * <p>
     * deploy object with fixed property 'TYPE':'AgentRef'
     * 
     */
    @JsonProperty("configuration")
    public AgentRef getConfiguration() {
        return configuration;
    }

    /**
     * agent
     * <p>
     * deploy object with fixed property 'TYPE':'AgentRef'
     * 
     */
    @JsonProperty("configuration")
    public void setConfiguration(AgentRef configuration) {
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
        if ((other instanceof AgentRefEdit) == false) {
            return false;
        }
        AgentRefEdit rhs = ((AgentRefEdit) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(configuration, rhs.configuration).isEquals();
    }

}
