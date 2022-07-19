
package com.sos.joc.model.security;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.joc.model.security.ldap.LdapProperties;
import com.sos.joc.model.security.vault.VaultProperties;
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
     * Ldap Properties
     * <p>
     * 
     * 
     */
    @JsonProperty("ldap")
    private LdapProperties ldap;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Properties() {
    }

    /**
     * 
     * @param initialPassword
     * @param ldap
     * @param minPasswordLength
     * @param sessionTimeout
     * @param vault
     */
    public Properties(String initialPassword, Long minPasswordLength, Integer sessionTimeout, VaultProperties vault, LdapProperties ldap) {
        super();
        this.initialPassword = initialPassword;
        this.minPasswordLength = minPasswordLength;
        this.sessionTimeout = sessionTimeout;
        this.vault = vault;
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

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("initialPassword", initialPassword).append("minPasswordLength", minPasswordLength).append("sessionTimeout", sessionTimeout).append("vault", vault).append("ldap", ldap).append("additionalProperties", additionalProperties).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(initialPassword).append(ldap).append(minPasswordLength).append(sessionTimeout).append(additionalProperties).append(vault).toHashCode();
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
        return new EqualsBuilder().append(initialPassword, rhs.initialPassword).append(ldap, rhs.ldap).append(minPasswordLength, rhs.minPasswordLength).append(sessionTimeout, rhs.sessionTimeout).append(additionalProperties, rhs.additionalProperties).append(vault, rhs.vault).isEquals();
    }

}
