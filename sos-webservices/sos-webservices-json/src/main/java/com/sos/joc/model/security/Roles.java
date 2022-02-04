
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Roles
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "identityServiceName",
    "roles"
})
public class Roles {

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    private String identityServiceName;
    @JsonProperty("roles")
    private List<Role> roles = new ArrayList<Role>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Roles() {
    }

    /**
     * 
     * @param identityServiceName
     * @param roles
     */
    public Roles(String identityServiceName, List<Role> roles) {
        super();
        this.identityServiceName = identityServiceName;
        this.roles = roles;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public String getIdentityServiceName() {
        return identityServiceName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * (Required)
     * 
     */
    @JsonProperty("identityServiceName")
    public void setIdentityServiceName(String identityServiceName) {
        this.identityServiceName = identityServiceName;
    }

    @JsonProperty("roles")
    public List<Role> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(List<Role> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("identityServiceName", identityServiceName).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceName).append(roles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Roles) == false) {
            return false;
        }
        Roles rhs = ((Roles) other);
        return new EqualsBuilder().append(identityServiceName, rhs.identityServiceName).append(roles, rhs.roles).isEquals();
    }

}
