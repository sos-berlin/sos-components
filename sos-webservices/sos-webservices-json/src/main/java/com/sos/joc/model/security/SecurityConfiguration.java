
package com.sos.joc.model.security;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.configuration.Profile;
import com.sos.joc.model.security.permissions.SecurityConfigurationRoles;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Authentication configuration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "deliveryDate",
    "identityServiceType",
    "main",
    "accounts",
    "profiles",
    "roles"
})
public class SecurityConfiguration {

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    @JsonPropertyDescription("Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty")
    private Date deliveryDate;
    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    private IdentityServiceTypes identityServiceType;
    @JsonProperty("main")
    private List<SecurityConfigurationMainEntry> main = new ArrayList<SecurityConfigurationMainEntry>();
    @JsonProperty("accounts")
    private List<SecurityConfigurationAccount> accounts = new ArrayList<SecurityConfigurationAccount>();
    @JsonProperty("profiles")
    private List<Profile> profiles = new ArrayList<Profile>();
    @JsonProperty("roles")
    private SecurityConfigurationRoles roles;

    /**
     * No args constructor for use in serialization
     * 
     */
    public SecurityConfiguration() {
    }

    /**
     * 
     * @param identityServiceType
     * @param roles
     * @param profiles
     * @param main
     * @param accounts
     * @param deliveryDate
     */
    public SecurityConfiguration(Date deliveryDate, IdentityServiceTypes identityServiceType, List<SecurityConfigurationMainEntry> main, List<SecurityConfigurationAccount> accounts, List<Profile> profiles, SecurityConfigurationRoles roles) {
        super();
        this.deliveryDate = deliveryDate;
        this.identityServiceType = identityServiceType;
        this.main = main;
        this.accounts = accounts;
        this.profiles = profiles;
        this.roles = roles;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    public Date getDeliveryDate() {
        return deliveryDate;
    }

    /**
     * timestamp
     * <p>
     * Value is UTC timestamp in ISO 8601 YYYY-MM-DDThh:mm:ss.sZ or empty
     * 
     */
    @JsonProperty("deliveryDate")
    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    public IdentityServiceTypes getIdentityServiceType() {
        return identityServiceType;
    }

    /**
     * Identity Service Types
     * <p>
     * 
     * 
     */
    @JsonProperty("identityServiceType")
    public void setIdentityServiceType(IdentityServiceTypes identityServiceType) {
        this.identityServiceType = identityServiceType;
    }

    @JsonProperty("main")
    public List<SecurityConfigurationMainEntry> getMain() {
        return main;
    }

    @JsonProperty("main")
    public void setMain(List<SecurityConfigurationMainEntry> main) {
        this.main = main;
    }

    @JsonProperty("accounts")
    public List<SecurityConfigurationAccount> getAccounts() {
        return accounts;
    }

    @JsonProperty("accounts")
    public void setAccounts(List<SecurityConfigurationAccount> accounts) {
        this.accounts = accounts;
    }

    @JsonProperty("profiles")
    public List<Profile> getProfiles() {
        return profiles;
    }

    @JsonProperty("profiles")
    public void setProfiles(List<Profile> profiles) {
        this.profiles = profiles;
    }

    @JsonProperty("roles")
    public SecurityConfigurationRoles getRoles() {
        return roles;
    }

    @JsonProperty("roles")
    public void setRoles(SecurityConfigurationRoles roles) {
        this.roles = roles;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("deliveryDate", deliveryDate).append("identityServiceType", identityServiceType).append("main", main).append("accounts", accounts).append("profiles", profiles).append("roles", roles).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(identityServiceType).append(roles).append(profiles).append(main).append(accounts).append(deliveryDate).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof SecurityConfiguration) == false) {
            return false;
        }
        SecurityConfiguration rhs = ((SecurityConfiguration) other);
        return new EqualsBuilder().append(identityServiceType, rhs.identityServiceType).append(roles, rhs.roles).append(profiles, rhs.profiles).append(main, rhs.main).append(accounts, rhs.accounts).append(deliveryDate, rhs.deliveryDate).isEquals();
    }

}
