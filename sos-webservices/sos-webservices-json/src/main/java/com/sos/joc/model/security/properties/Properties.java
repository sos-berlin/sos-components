
package com.sos.joc.model.security.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.fido2.Fido2Properties;
import com.sos.joc.model.security.properties.keycloak.KeycloakProperties;
import com.sos.joc.model.security.properties.ldap.LdapProperties;
import com.sos.joc.model.security.properties.oidc.OidcProperties;
import com.sos.joc.model.security.properties.vault.VaultProperties;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Identity Service Properties
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "initialPassword",
    "minPasswordLength",
    "sessionTimeout",
    "vault",
    "keycloak",
    "oidc",
    "fido2",
    "ldap"
})
public class Properties {

    @JsonProperty("initialPassword")
    private String initialPassword;
    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("minPasswordLength")
    private Long minPasswordLength;
    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("sessionTimeout")
    private Integer sessionTimeout;
    /**
     * Vault Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("vault")
    private VaultProperties vault;
    /**
     * Keycloak Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("keycloak")
    private KeycloakProperties keycloak;
    /**
     * Openid Connect Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("oidc")
    private OidcProperties oidc;
    /**
     * FIDO2 Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido2")
    private Fido2Properties fido2;
    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("ldap")
    private LdapProperties ldap;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Properties() {
    }

    /**
     * 
     * @param keycloak
     * @param initialPassword
     * @param ldap
     * @param minPasswordLength
     * @param sessionTimeout
     * @param fido2
     * @param oidc
     * @param vault
     */
    public Properties(String initialPassword, Long minPasswordLength, Integer sessionTimeout, VaultProperties vault, KeycloakProperties keycloak, OidcProperties oidc, Fido2Properties fido2, LdapProperties ldap) {
        super();
        this.initialPassword = initialPassword;
        this.minPasswordLength = minPasswordLength;
        this.sessionTimeout = sessionTimeout;
        this.vault = vault;
        this.keycloak = keycloak;
        this.oidc = oidc;
        this.fido2 = fido2;
        this.ldap = ldap;
    }

    @JsonProperty("initialPassword")
    public String getInitialPassword() {
        return initialPassword;
    }

    @JsonProperty("initialPassword")
    public void setInitialPassword(String initialPassword) {
        this.initialPassword = initialPassword;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("minPasswordLength")
    public Long getMinPasswordLength() {
        return minPasswordLength;
    }

    /**
     * non negative long
     * <p>
     * 
     * 
     */
    @JsonProperty("minPasswordLength")
    public void setMinPasswordLength(Long minPasswordLength) {
        this.minPasswordLength = minPasswordLength;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("sessionTimeout")
    public Integer getSessionTimeout() {
        return sessionTimeout;
    }

    /**
     * non negative integer
     * <p>
     * 
     * 
     */
    @JsonProperty("sessionTimeout")
    public void setSessionTimeout(Integer sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    /**
     * Vault Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("vault")
    public VaultProperties getVault() {
        return vault;
    }

    /**
     * Vault Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("vault")
    public void setVault(VaultProperties vault) {
        this.vault = vault;
    }

    /**
     * Keycloak Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("keycloak")
    public KeycloakProperties getKeycloak() {
        return keycloak;
    }

    /**
     * Keycloak Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("keycloak")
    public void setKeycloak(KeycloakProperties keycloak) {
        this.keycloak = keycloak;
    }

    /**
     * Openid Connect Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("oidc")
    public OidcProperties getOidc() {
        return oidc;
    }

    /**
     * Openid Connect Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("oidc")
    public void setOidc(OidcProperties oidc) {
        this.oidc = oidc;
    }

    /**
     * FIDO2 Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido2")
    public Fido2Properties getFido2() {
        return fido2;
    }

    /**
     * FIDO2 Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido2")
    public void setFido2(Fido2Properties fido2) {
        this.fido2 = fido2;
    }

    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("ldap")
    public LdapProperties getLdap() {
        return ldap;
    }

    /**
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("ldap")
    public void setLdap(LdapProperties ldap) {
        this.ldap = ldap;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("initialPassword", initialPassword).append("minPasswordLength", minPasswordLength).append("sessionTimeout", sessionTimeout).append("vault", vault).append("keycloak", keycloak).append("oidc", oidc).append("fido2", fido2).append("ldap", ldap).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(keycloak).append(initialPassword).append(ldap).append(minPasswordLength).append(sessionTimeout).append(fido2).append(oidc).append(vault).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Properties) == false) {
            return false;
        }
        Properties rhs = ((Properties) other);
        return new EqualsBuilder().append(keycloak, rhs.keycloak).append(initialPassword, rhs.initialPassword).append(ldap, rhs.ldap).append(minPasswordLength, rhs.minPasswordLength).append(sessionTimeout, rhs.sessionTimeout).append(fido2, rhs.fido2).append(oidc, rhs.oidc).append(vault, rhs.vault).isEquals();
    }

}
