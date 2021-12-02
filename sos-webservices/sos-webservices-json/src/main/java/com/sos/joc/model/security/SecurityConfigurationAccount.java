
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
    "account",
    "password",
    "identityServiceId",
    "roles"
})
public class SecurityConfigurationAccount {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    private String account;
    @JsonProperty("password")
    private String password;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceId")
    private Long identityServiceId;
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
    public SecurityConfigurationAccount() {
    }

    /**
     * 
     * @param password
     * @param identityServiceId
     * @param roles
     * @param account
     */
    public SecurityConfigurationAccount(String account, String password, Long identityServiceId, List<String> roles) {
        super();
        this.account = account;
        this.password = password;
        this.identityServiceId = identityServiceId;
        this.roles = roles;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    public String getAccount() {
        return account;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("account")
    public void setAccount(String account) {
        this.account = account;
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
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceId")
    public Long getIdentityServiceId() {
        return identityServiceId;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceId")
    public void setIdentityServiceId(Long identityServiceId) {
        this.identityServiceId = identityServiceId;
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
        return new ToStringBuilder(this).append("account", account).append("password", password).append("identityServiceId", identityServiceId).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(password).append(identityServiceId).append(account).append(roles).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfigurationAccount) == false) {
            return false;
        }
        SecurityConfigurationAccount rhs = ((SecurityConfigurationAccount) other);
        return new EqualsBuilder().append(password, rhs.password).append(identityServiceId, rhs.identityServiceId).append(account, rhs.account).append(roles, rhs.roles).isEquals();
    }

}
