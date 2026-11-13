
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.HttpHeaders;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.UrlConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefHttpOnly;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.WebdavAlternativeFragmentRef;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * WebDAV Base - Connection and authentication settings for the WEBDAV / WEBDAVS protocol fragments.
 * <p>
 * Abstract base for WENDAV / WEBDAV fragments. Do not instantiate directly.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jumpRef",
    "proxyRef",
    "alternativeRef",
    "connection",
    "authentication",
    "httpHeaders"
})
public class AWebdavFragment
    extends AProtocolFragment
{

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpRef")
    private String jumpRef;
    /**
     * HTTP Proxy Reference
     * <p>
     * Reference to an HTTP proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    @JsonPropertyDescription("Reference to an HTTP proxy fragment.")
    private ProxyFragmentRefHttpOnly proxyRef;
    /**
     * WebDAV Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    private WebdavAlternativeFragmentRef alternativeRef;
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
    public AWebdavFragment() {
    }

    /**
     * 
     * @param path
     * @param proxyRef
     * @param alternativeRef
     * @param credentialStoreRef
     * @param jumpRef
     * @param connection
     * @param httpHeaders
     * @param documentationName
     * @param title
     * @param decryptionRef
     * @param version
     * @param authentication
     */
    public AWebdavFragment(String jumpRef, ProxyFragmentRefHttpOnly proxyRef, WebdavAlternativeFragmentRef alternativeRef, UrlConnection connection, BasicAuthentication authentication, HttpHeaders httpHeaders, String credentialStoreRef, String decryptionRef, String version, String path, String title, String documentationName) {
        super(credentialStoreRef, decryptionRef, version, path, title, documentationName);
        this.jumpRef = jumpRef;
        this.proxyRef = proxyRef;
        this.alternativeRef = alternativeRef;
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
    @JsonProperty("jumpRef")
    public String getJumpRef() {
        return jumpRef;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("jumpRef")
    public void setJumpRef(String jumpRef) {
        this.jumpRef = jumpRef;
    }

    /**
     * HTTP Proxy Reference
     * <p>
     * Reference to an HTTP proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    public ProxyFragmentRefHttpOnly getProxyRef() {
        return proxyRef;
    }

    /**
     * HTTP Proxy Reference
     * <p>
     * Reference to an HTTP proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    public void setProxyRef(ProxyFragmentRefHttpOnly proxyRef) {
        this.proxyRef = proxyRef;
    }

    /**
     * WebDAV Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public WebdavAlternativeFragmentRef getAlternativeRef() {
        return alternativeRef;
    }

    /**
     * WebDAV Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public void setAlternativeRef(WebdavAlternativeFragmentRef alternativeRef) {
        this.alternativeRef = alternativeRef;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jumpRef", jumpRef).append("proxyRef", proxyRef).append("alternativeRef", alternativeRef).append("connection", connection).append("authentication", authentication).append("httpHeaders", httpHeaders).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(proxyRef).append(alternativeRef).append(jumpRef).append(connection).append(httpHeaders).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AWebdavFragment) == false) {
            return false;
        }
        AWebdavFragment rhs = ((AWebdavFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(proxyRef, rhs.proxyRef).append(alternativeRef, rhs.alternativeRef).append(jumpRef, rhs.jumpRef).append(connection, rhs.connection).append(httpHeaders, rhs.httpHeaders).append(authentication, rhs.authentication).isEquals();
    }

}
