
package com.sos.joc.model.agent;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * reset agents
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "force"
})
public class ResetAgents
    extends DeployAgents
{

    @JsonProperty("force")
    private Boolean force = false;

    @JsonProperty("force")
    public Boolean getForce() {
        return force;
    }

    @JsonProperty("force")
    public void setForce(Boolean force) {
        this.force = force;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("force", force).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(force).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ResetAgents) == false) {
            return false;
        }
        ResetAgents rhs = ((ResetAgents) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(force, rhs.force).isEquals();
    }

}
