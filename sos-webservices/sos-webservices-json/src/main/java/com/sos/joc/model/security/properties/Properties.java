
package com.sos.joc.model.security.properties;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.properties.fido.FidoProperties;
import com.sos.joc.model.security.properties.keycloak.KeycloakProperties;
import com.sos.joc.model.security.properties.ldap.LdapProperties;
import com.sos.joc.model.security.properties.oidc.OidcProperties;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


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
    "keycloak",
    "oidc",
    "fido",
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
     * FIDO Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido")
    private FidoProperties fido;
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
     * @param fido
     * @param keycloak
     * @param initialPassword
     * @param ldap
     * @param minPasswordLength
     * @param sessionTimeout
     * @param oidc
     */
    public Properties(String initialPassword, Long minPasswordLength, Integer sessionTimeout, KeycloakProperties keycloak, OidcProperties oidc, FidoProperties fido, LdapProperties ldap) {
        super();
        this.initialPassword = initialPassword;
        this.minPasswordLength = minPasswordLength;
        this.sessionTimeout = sessionTimeout;
        this.keycloak = keycloak;
        this.oidc = oidc;
        this.fido = fido;
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
     * FIDO Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido")
    public FidoProperties getFido() {
        return fido;
    }

    /**
     * FIDO Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("fido")
    public void setFido(FidoProperties fido) {
        this.fido = fido;
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
        return new ToStringBuilder(this).append("initialPassword", initialPassword).append("minPasswordLength", minPasswordLength).append("sessionTimeout", sessionTimeout).append("keycloak", keycloak).append("oidc", oidc).append("fido", fido).append("ldap", ldap).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(fido).append(keycloak).append(initialPassword).append(ldap).append(minPasswordLength).append(sessionTimeout).append(oidc).toHashCode();
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
        return new EqualsBuilder().append(fido, rhs.fido).append(keycloak, rhs.keycloak).append(initialPassword, rhs.initialPassword).append(ldap, rhs.ldap).append(minPasswordLength, rhs.minPasswordLength).append(sessionTimeout, rhs.sessionTimeout).append(oidc, rhs.oidc).isEquals();
    }

}
