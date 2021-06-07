
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "user",
    "password",
    "roles"
})
public class SecurityConfigurationUser {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user")
    private String user;
    @JsonProperty("password")
    private String password;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roles")
    private List<String> roles = new ArrayList<String>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public SecurityConfigurationUser() {
    }

    /**
     * 
     * @param password
     * @param roles
     * @param user
     */
    public SecurityConfigurationUser(String user, String password, List<String> roles) {
        super();
        this.user = user;
        this.password = password;
        this.roles = roles;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user")
    public String getUser() {
        return user;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("user")
    public void setUser(String user) {
        this.user = user;
    }

    @JsonProperty("password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roles")
    public List<String> getRoles() {
        return roles;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("roles")
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("user", user).append("password", password).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(password).append(user).append(roles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationUser) == false) {
            return false;
        }
        SecurityConfigurationUser rhs = ((SecurityConfigurationUser) other);
        return new EqualsBuilder().append(password, rhs.password).append(user, rhs.user).append(roles, rhs.roles).isEquals();
    }

}
