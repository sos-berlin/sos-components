
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.common.HttpHeaders;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.UrlConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.ssl.Ssl;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.HttpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefHttpOnly;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * HTTP - Connection and authentication settings for the HTTP protocol fragments.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "ssl"
})
public class HttpFragment
    extends AHttpFragment
{

    /**
     * TLS configuration. Choose either a trusted store or an untrusted server.
     * 
     */
    @JsonProperty("ssl")
    @JsonPropertyDescription("TLS configuration. Choose either a trusted store or an untrusted server.")
    private Ssl ssl;

    /**
     * No args constructor for use in serialization
     * 
     */
    public HttpFragment() {
    }

    /**
     * 
     * @param proxyRef
     * @param alternativeRef
     * @param jumpRef
     * @param httpHeaders
     * @param title
     * @param ssl
     * @param decryptionRef
     * @param version
     * @param path
     * @param credentialStoreRef
     * @param connection
     * @param documentationName
     * @param authentication
     */
    public HttpFragment(Ssl ssl, String jumpRef, ProxyFragmentRefHttpOnly proxyRef, HttpAlternativeFragmentRef alternativeRef, UrlConnection connection, BasicAuthentication authentication, HttpHeaders httpHeaders, String credentialStoreRef, String decryptionRef, String version, String path, String title, String documentationName) {
        super(jumpRef, proxyRef, alternativeRef, connection, authentication, httpHeaders, credentialStoreRef, decryptionRef, version, path, title, documentationName);
        this.ssl = ssl;
    }

    /**
     * TLS configuration. Choose either a trusted store or an untrusted server.
     * 
     */
    @JsonProperty("ssl")
    public Ssl getSsl() {
        return ssl;
    }

    /**
     * TLS configuration. Choose either a trusted store or an untrusted server.
     * 
     */
    @JsonProperty("ssl")
    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("ssl", ssl).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(ssl).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof HttpFragment) == false) {
            return false;
        }
        HttpFragment rhs = ((HttpFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(ssl, rhs.ssl).isEquals();
    }

}
