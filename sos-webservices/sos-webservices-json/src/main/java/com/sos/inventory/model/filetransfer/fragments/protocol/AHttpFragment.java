
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.HttpHeaders;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.UrlConnection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * HTTP Base - Connection and authentication settings for the HTTP / HTTPS protocol fragments.
 * <p>
 * Abstract base for HTTP / HTTPS fragments. Do not instantiate directly.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jumpFragmentName",
    "httpProxyFragmentName",
    "connection",
    "authentication",
    "httpHeaders"
})
public class AHttpFragment
    extends AProtocolFragment
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpFragmentName")
    private String jumpFragmentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragmentName")
    private String httpProxyFragmentName;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    private UrlConnection connection;
    @JsonProperty("authentication")
    private BasicAuthentication authentication;
    /**
     * HTTP Headers
     * <p>
     * 
     * 
     */
    @JsonProperty("httpHeaders")
    private HttpHeaders httpHeaders;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AHttpFragment() {
    }

    /**
     * 
     * @param jumpFragmentName
     * @param httpProxyFragmentName
     * @param connection
     * @param httpHeaders
     * @param authentication
     */
    public AHttpFragment(String jumpFragmentName, String httpProxyFragmentName, UrlConnection connection, BasicAuthentication authentication, HttpHeaders httpHeaders) {
        super();
        this.jumpFragmentName = jumpFragmentName;
        this.httpProxyFragmentName = httpProxyFragmentName;
        this.connection = connection;
        this.authentication = authentication;
        this.httpHeaders = httpHeaders;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpFragmentName")
    public String getJumpFragmentName() {
        return jumpFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpFragmentName")
    public void setJumpFragmentName(String jumpFragmentName) {
        this.jumpFragmentName = jumpFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragmentName")
    public String getHttpProxyFragmentName() {
        return httpProxyFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("httpProxyFragmentName")
    public void setHttpProxyFragmentName(String httpProxyFragmentName) {
        this.httpProxyFragmentName = httpProxyFragmentName;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public UrlConnection getConnection() {
        return connection;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public void setConnection(UrlConnection connection) {
        this.connection = connection;
    }

    @JsonProperty("authentication")
    public BasicAuthentication getAuthentication() {
        return authentication;
    }

    @JsonProperty("authentication")
    public void setAuthentication(BasicAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * HTTP Headers
     * <p>
     * 
     * 
     */
    @JsonProperty("httpHeaders")
    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    /**
     * HTTP Headers
     * <p>
     * 
     * 
     */
    @JsonProperty("httpHeaders")
    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jumpFragmentName", jumpFragmentName).append("httpProxyFragmentName", httpProxyFragmentName).append("connection", connection).append("authentication", authentication).append("httpHeaders", httpHeaders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jumpFragmentName).append(httpProxyFragmentName).append(connection).append(httpHeaders).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AHttpFragment) == false) {
            return false;
        }
        AHttpFragment rhs = ((AHttpFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jumpFragmentName, rhs.jumpFragmentName).append(httpProxyFragmentName, rhs.httpProxyFragmentName).append(connection, rhs.connection).append(httpHeaders, rhs.httpHeaders).append(authentication, rhs.authentication).isEquals();
    }

}
