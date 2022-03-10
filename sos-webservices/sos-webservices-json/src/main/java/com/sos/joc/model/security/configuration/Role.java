
package com.sos.joc.model.security.configuration;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "role"
})
public class Role {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    private String role;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Role() {
    }

    /**
     * 
     * @param role
     */
    public Role(String role) {
        super();
        this.role = role;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    public String getRole() {
        return role;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("role")
    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("role", role).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(role).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Role) == false) {
            return false;
        }
        Role rhs = ((Role) other);
        return new EqualsBuilder().append(role, rhs.role).isEquals();
    }

}
