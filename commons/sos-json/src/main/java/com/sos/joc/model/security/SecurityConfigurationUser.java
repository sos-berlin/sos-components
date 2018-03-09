
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
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

    @JsonProperty("user")
    @JacksonXmlProperty(localName = "user")
    private String user;
    @JsonProperty("password")
    @JacksonXmlProperty(localName = "password")
    private String password;
    @JsonProperty("roles")
    @JacksonXmlProperty(localName = "role")
    @JacksonXmlElementWrapper(useWrapping = true, localName = "roles")
    private List<String> roles = new ArrayList<String>();

    @JsonProperty("user")
    @JacksonXmlProperty(localName = "user")
    public String getUser() {
        return user;
    }

    @JsonProperty("user")
    @JacksonXmlProperty(localName = "user")
    public void setUser(String user) {
        this.user = user;
    }

    @JsonProperty("password")
    @JacksonXmlProperty(localName = "password")
    public String getPassword() {
        return password;
    }

    @JsonProperty("password")
    @JacksonXmlProperty(localName = "password")
    public void setPassword(String password) {
        this.password = password;
    }

    @JsonProperty("roles")
    @JacksonXmlProperty(localName = "role")
    public List<String> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    @JacksonXmlProperty(localName = "role")
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
