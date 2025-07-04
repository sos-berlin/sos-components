
package com.sos.joc.model.security.oidc;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * GetTokenRequest
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "code",
    "code_verifier",
    "redirect_uri"
})
public class GetTokenRequest {

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    private String code;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code_verifier")
    @JsonAlias({
        "codeVerifier"
    })
    private String code_verifier;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("redirect_uri")
    @JsonAlias({
        "redirectUri"
    })
    private String redirect_uri;

    /**
     * No args constructor for use in serialization
     * 
     */
    public GetTokenRequest() {
    }

    /**
     * 
     * @param code
     * @param redirect_uri
     * @param code_verifier
     */
    public GetTokenRequest(String code, String code_verifier, String redirect_uri) {
        super();
        this.code = code;
        this.code_verifier = code_verifier;
        this.redirect_uri = redirect_uri;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    public String getCode() {
        return code;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code")
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code_verifier")
    public String getCode_verifier() {
        return code_verifier;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("code_verifier")
    public void setCode_verifier(String code_verifier) {
        this.code_verifier = code_verifier;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("redirect_uri")
    public String getRedirect_uri() {
        return redirect_uri;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("redirect_uri")
    public void setRedirect_uri(String redirect_uri) {
        this.redirect_uri = redirect_uri;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("code", code).append("code_verifier", code_verifier).append("redirect_uri", redirect_uri).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(code).append(redirect_uri).append(code_verifier).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GetTokenRequest) == false) {
            return false;
        }
        GetTokenRequest rhs = ((GetTokenRequest) other);
        return new EqualsBuilder().append(code, rhs.code).append(redirect_uri, rhs.redirect_uri).append(code_verifier, rhs.code_verifier).isEquals();
    }

}
