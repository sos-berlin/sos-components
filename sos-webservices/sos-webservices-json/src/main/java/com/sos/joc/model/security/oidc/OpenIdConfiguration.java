
package com.sos.joc.model.security.oidc;

import java.util.ArrayList;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * OpenIdConfiguration
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "end_session_endpoint",
    "revocation_endpoint",
    "token_endpoint",
    "token_endpoint_auth_methods_supported",
    "claims_supported",
    "jwks_uri"
})
public class OpenIdConfiguration {

    /**
     * URL for logout request
     * 
     */
    @JsonProperty("revocation_endpoint")
    @JsonPropertyDescription("URL for logout request")
    private String revocation_endpoint;
    /**
     * alternative URL for logout request
     * 
     */
    @JsonProperty("end_session_endpoint")
    @JsonPropertyDescription("alternative URL for logout request")
    private String end_session_endpoint;
    /**
     * URL for token request
     * 
     */
    @JsonProperty("token_endpoint")
    @JsonPropertyDescription("URL for token request")
    private String token_endpoint;
    /**
     * e.g. client_secret_post, private_key_jwt, client_secret_basic
     * 
     */
    @JsonProperty("token_endpoint_auth_methods_supported")
    @JsonPropertyDescription("e.g. client_secret_post, private_key_jwt, client_secret_basic")
    private List<String> token_endpoint_auth_methods_supported = new ArrayList<String>();
    @JsonProperty("claims_supported")
    private List<String> claims_supported = new ArrayList<String>();
    @JsonProperty("jwks_uri")
    private String jwks_uri;

    /**
     * No args constructor for use in serialization
     * 
     */
    public OpenIdConfiguration() {
    }

    /**
     * 
     * @param claims_supported
     * @param jwks_uri
     * @param end_session_endpoint
     * @param revocation_endpoint
     * @param token_endpoint_auth_methods_supported
     * @param token_endpoint
     */
    public OpenIdConfiguration(String end_session_endpoint, String revocation_endpoint, String token_endpoint, List<String> token_endpoint_auth_methods_supported, List<String> claims_supported, String jwks_uri) {
        super();
        this.end_session_endpoint = end_session_endpoint;
        this.revocation_endpoint = revocation_endpoint;
        this.token_endpoint = token_endpoint;
        this.token_endpoint_auth_methods_supported = token_endpoint_auth_methods_supported;
        this.claims_supported = claims_supported;
        this.jwks_uri = jwks_uri;
    }

    /**
     * URL for logout request
     * 
     */
    @JsonProperty("revocation_endpoint")
    public String getRevocation_endpoint() {
        return revocation_endpoint;
    }

    /**
     * URL for logout request
     * 
     */
    @JsonProperty("revocation_endpoint")
    public void setRevocation_endpoint(String revocation_endpoint) {
        this.revocation_endpoint = revocation_endpoint;
    }
    
    /**
     * alternative URL for logout request
     * 
     */
    @JsonProperty("end_session_endpoint")
    public String getEnd_session_endpoint() {
        return end_session_endpoint;
    }

    /**
     * alternative URL for logout request
     * 
     */
    @JsonProperty("end_session_endpoint")
    public void setEnd_session_endpoint(String end_session_endpoint) {
        this.end_session_endpoint = end_session_endpoint;
    }

    /**
     * URL for token request
     * 
     */
    @JsonProperty("token_endpoint")
    public String getToken_endpoint() {
        return token_endpoint;
    }

    /**
     * URL for token request
     * 
     */
    @JsonProperty("token_endpoint")
    public void setToken_endpoint(String token_endpoint) {
        this.token_endpoint = token_endpoint;
    }

    /**
     * e.g. client_secret_post, private_key_jwt, client_secret_basic
     * 
     */
    @JsonProperty("token_endpoint_auth_methods_supported")
    public List<String> getToken_endpoint_auth_methods_supported() {
        return token_endpoint_auth_methods_supported;
    }

    /**
     * e.g. client_secret_post, private_key_jwt, client_secret_basic
     * 
     */
    @JsonProperty("token_endpoint_auth_methods_supported")
    public void setToken_endpoint_auth_methods_supported(List<String> token_endpoint_auth_methods_supported) {
        this.token_endpoint_auth_methods_supported = token_endpoint_auth_methods_supported;
    }

    @JsonProperty("claims_supported")
    public List<String> getClaims_supported() {
        return claims_supported;
    }

    @JsonProperty("claims_supported")
    public void setClaims_supported(List<String> claims_supported) {
        this.claims_supported = claims_supported;
    }

    @JsonProperty("jwks_uri")
    public String getJwks_uri() {
        return jwks_uri;
    }

    @JsonProperty("jwks_uri")
    public void setJwks_uri(String jwks_uri) {
        this.jwks_uri = jwks_uri;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("revocation_endpoint", revocation_endpoint).append("end_session_endpoint", end_session_endpoint).append("token_endpoint", token_endpoint).append("token_endpoint_auth_methods_supported", token_endpoint_auth_methods_supported).append("claims_supported", claims_supported).append("jwks_uri", jwks_uri).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(jwks_uri).append(revocation_endpoint).append(end_session_endpoint).append(token_endpoint_auth_methods_supported).append(claims_supported).append(token_endpoint).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof OpenIdConfiguration) == false) {
            return false;
        }
        OpenIdConfiguration rhs = ((OpenIdConfiguration) other);
        return new EqualsBuilder().append(jwks_uri, rhs.jwks_uri).append(revocation_endpoint, rhs.revocation_endpoint).append(end_session_endpoint, rhs.end_session_endpoint).append(token_endpoint_auth_methods_supported, rhs.token_endpoint_auth_methods_supported).append(claims_supported, rhs.claims_supported).append(token_endpoint, rhs.token_endpoint).isEquals();
    }

}
