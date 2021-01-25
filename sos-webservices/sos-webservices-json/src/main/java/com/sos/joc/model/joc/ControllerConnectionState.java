
package com.sos.joc.model.joc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.controller.ConnectionState;
import com.sos.joc.model.controller.Role;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "role",
    "state"
})
public class ControllerConnectionState {

    /**
     * jobscheduler role
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    private Role role;
    /**
     * connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    private ConnectionState state;

    /**
     * jobscheduler role
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    public Role getRole() {
        return role;
    }

    /**
     * jobscheduler role
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public ConnectionState getState() {
        return state;
    }

    /**
     * connection state
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("state")
    public void setState(ConnectionState state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("role", role).append("state", state).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(role).append(state).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerConnectionState) == false) {
            return false;
        }
        ControllerConnectionState rhs = ((ControllerConnectionState) other);
        return new EqualsBuilder().append(role, rhs.role).append(state, rhs.state).isEquals();
    }

}
