
package com.sos.joc.model.security.oidc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * GetTokenResponse
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "token_type",
    "expires_in",
    "ext_expires_in",
    "expires_on",
    "access_token",
    "refresh_token",
    "id_token"
})
public class GetTokenResponse {

    @JsonProperty("token_type")
    private String token_type;
    @JsonProperty("expires_in")
    private String expires_in;
    @JsonProperty("ext_expires_in")
    private String ext_expires_in;
    @JsonProperty("expires_on")
    private String expires_on;
    @JsonProperty("access_token")
    private String access_token;
    @JsonProperty("refresh_token")
    private String refresh_token;
    @JsonProperty("id_token")
    private String id_token;

    /**
     * No args constructor for use in serialization
     * 
     */
    public GetTokenResponse() {
    }

    /**
     * 
     * @param access_token
     * @param refresh_token
     * @param expires_on
     * @param id_token
     * @param ext_expires_in
     * @param token_type
     * @param expires_in
     */
    public GetTokenResponse(String token_type, String expires_in, String ext_expires_in, String expires_on, String access_token, String refresh_token, String id_token) {
        super();
        this.token_type = token_type;
        this.expires_in = expires_in;
        this.ext_expires_in = ext_expires_in;
        this.expires_on = expires_on;
        this.access_token = access_token;
        this.refresh_token = refresh_token;
        this.id_token = id_token;
    }

    @JsonProperty("token_type")
    public String getToken_type() {
        return token_type;
    }

    @JsonProperty("token_type")
    public void setToken_type(String token_type) {
        this.token_type = token_type;
    }

    @JsonProperty("expires_in")
    public String getExpires_in() {
        return expires_in;
    }

    @JsonProperty("expires_in")
    public void setExpires_in(String expires_in) {
        this.expires_in = expires_in;
    }

    @JsonProperty("ext_expires_in")
    public String getExt_expires_in() {
        return ext_expires_in;
    }

    @JsonProperty("ext_expires_in")
    public void setExt_expires_in(String ext_expires_in) {
        this.ext_expires_in = ext_expires_in;
    }

    @JsonProperty("expires_on")
    public String getExpires_on() {
        return expires_on;
    }

    @JsonProperty("expires_on")
    public void setExpires_on(String expires_on) {
        this.expires_on = expires_on;
    }

    @JsonProperty("access_token")
    public String getAccess_token() {
        return access_token;
    }

    @JsonProperty("access_token")
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }

    @JsonProperty("refresh_token")
    public String getRefresh_token() {
        return refresh_token;
    }

    @JsonProperty("refresh_token")
    public void setRefresh_token(String refresh_token) {
        this.refresh_token = refresh_token;
    }

    @JsonProperty("id_token")
    public String getId_token() {
        return id_token;
    }

    @JsonProperty("id_token")
    public void setId_token(String id_token) {
        this.id_token = id_token;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("token_type", token_type).append("expires_in", expires_in).append("ext_expires_in", ext_expires_in).append("expires_on", expires_on).append("access_token", access_token).append("refresh_token", refresh_token).append("id_token", id_token).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(access_token).append(refresh_token).append(expires_on).append(id_token).append(ext_expires_in).append(token_type).append(expires_in).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof GetTokenResponse) == false) {
            return false;
        }
        GetTokenResponse rhs = ((GetTokenResponse) other);
        return new EqualsBuilder().append(access_token, rhs.access_token).append(refresh_token, rhs.refresh_token).append(expires_on, rhs.expires_on).append(id_token, rhs.id_token).append(ext_expires_in, rhs.ext_expires_in).append(token_type, rhs.token_type).append(expires_in, rhs.expires_in).isEquals();
    }

}
