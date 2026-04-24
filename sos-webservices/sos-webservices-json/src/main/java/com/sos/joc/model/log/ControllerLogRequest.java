
package com.sos.joc.model.log;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.controller.Role;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * controller log filter
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "role"
})
public class ControllerLogRequest
    extends LogBaseRequest
{

    /**
     * Controller cluster role
     * <p>
     * 
     * 
     */
    @JsonProperty("role")
    private Role role;

    /**
     * Controller cluster role
     * <p>
     * 
     * 
     */
    @JsonProperty("role")
    public Role getRole() {
        return role;
    }

    /**
     * Controller cluster role
     * <p>
     * 
     * 
     */
    @JsonProperty("role")
    public void setRole(Role role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("role", role).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(role).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof ControllerLogRequest) == false) {
            return false;
        }
        ControllerLogRequest rhs = ((ControllerLogRequest) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(role, rhs.role).isEquals();
    }

}
