
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "account",
    "password",
    "hashedPassword",
    "repeatedPassword",
    "oldPassword",
    "forcePasswordChange",
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
    @JsonProperty("hashedPassword")
    private String hashedPassword;
    @JsonProperty("repeatedPassword")
    private String repeatedPassword;
    @JsonProperty("oldPassword")
    private String oldPassword;
    /**
     * forcePasswordChange parameter
     * <p>
     * controls if the account is forced to change the password
     * 
     */
    @JsonProperty("forcePasswordChange")
    @JsonPropertyDescription("controls if the account is forced to change the password")
    private Boolean forcePasswordChange = false;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceId")
    private Long identityServiceId;
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
     * @param hashedPassword
     * @param oldPassword
     * @param forcePasswordChange
     * @param roles
     * @param repeatedPassword
     * @param account
     */
    public SecurityConfigurationAccount(String account, String password, String hashedPassword, String repeatedPassword, String oldPassword, Boolean forcePasswordChange, Long identityServiceId, List<String> roles) {
        super();
        this.account = account;
        this.password = password;
        this.hashedPassword = hashedPassword;
        this.repeatedPassword = repeatedPassword;
        this.oldPassword = oldPassword;
        this.forcePasswordChange = forcePasswordChange;
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

    @JsonProperty("hashedPassword")
    public String getHashedPassword() {
        return hashedPassword;
    }

    @JsonProperty("hashedPassword")
    public void setHashedPassword(String hashedPassword) {
        this.hashedPassword = hashedPassword;
    }

    @JsonProperty("repeatedPassword")
    public String getRepeatedPassword() {
        return repeatedPassword;
    }

    @JsonProperty("repeatedPassword")
    public void setRepeatedPassword(String repeatedPassword) {
        this.repeatedPassword = repeatedPassword;
    }

    @JsonProperty("oldPassword")
    public String getOldPassword() {
        return oldPassword;
    }

    @JsonProperty("oldPassword")
    public void setOldPassword(String oldPassword) {
        this.oldPassword = oldPassword;
    }

    /**
     * forcePasswordChange parameter
     * <p>
     * controls if the account is forced to change the password
     * 
     */
    @JsonProperty("forcePasswordChange")
    public Boolean getForcePasswordChange() {
        return forcePasswordChange;
    }

    /**
     * forcePasswordChange parameter
     * <p>
     * controls if the account is forced to change the password
     * 
     */
    @JsonProperty("forcePasswordChange")
    public void setForcePasswordChange(Boolean forcePasswordChange) {
        this.forcePasswordChange = forcePasswordChange;
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

    @JsonProperty("roles")
    public List<String> getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("account", account).append("password", password).append("hashedPassword", hashedPassword).append("repeatedPassword", repeatedPassword).append("oldPassword", oldPassword).append("forcePasswordChange", forcePasswordChange).append("identityServiceId", identityServiceId).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(password).append(identityServiceId).append(hashedPassword).append(oldPassword).append(forcePasswordChange).append(roles).append(repeatedPassword).append(account).toHashCode();
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
        return new EqualsBuilder().append(password, rhs.password).append(identityServiceId, rhs.identityServiceId).append(hashedPassword, rhs.hashedPassword).append(oldPassword, rhs.oldPassword).append(forcePasswordChange, rhs.forcePasswordChange).append(roles, rhs.roles).append(repeatedPassword, rhs.repeatedPassword).append(account, rhs.account).isEquals();
    }

}
