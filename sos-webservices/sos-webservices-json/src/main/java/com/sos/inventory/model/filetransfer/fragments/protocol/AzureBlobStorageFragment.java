
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.azure.AzureBSAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.UrlConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.AzureBlobStorageAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefHttpOnly;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * Azure - Connection and authentication settings for the Azure Blob Storage protocol fragments.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jumpRef",
    "proxyRef",
    "alternativeRef",
    "connection",
    "authentication"
})
public class AzureBlobStorageFragment
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
     * Azure Blob Storage Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    private AzureBlobStorageAlternativeFragmentRef alternativeRef;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    private UrlConnection connection;
    /**
     * Azure Blob Storage authentication. Provide either 'sharedKey' or 'sasToken'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    @JsonPropertyDescription("Azure Blob Storage authentication. Provide either 'sharedKey' or 'sasToken'.")
    private AzureBSAuthentication authentication;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AzureBlobStorageFragment() {
    }

    /**
     * 
     * @param path
     * @param proxyRef
     * @param alternativeRef
     * @param credentialStoreRef
     * @param jumpRef
     * @param connection
     * @param documentationName
     * @param title
     * @param decryptionRef
     * @param version
     * @param authentication
     */
    public AzureBlobStorageFragment(String jumpRef, ProxyFragmentRefHttpOnly proxyRef, AzureBlobStorageAlternativeFragmentRef alternativeRef, UrlConnection connection, AzureBSAuthentication authentication, String credentialStoreRef, String decryptionRef, String version, String path, String title, String documentationName) {
        super(credentialStoreRef, decryptionRef, version, path, title, documentationName);
        this.jumpRef = jumpRef;
        this.proxyRef = proxyRef;
        this.alternativeRef = alternativeRef;
        this.connection = connection;
        this.authentication = authentication;
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
     * Azure Blob Storage Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public AzureBlobStorageAlternativeFragmentRef getAlternativeRef() {
        return alternativeRef;
    }

    /**
     * Azure Blob Storage Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public void setAlternativeRef(AzureBlobStorageAlternativeFragmentRef alternativeRef) {
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

    /**
     * Azure Blob Storage authentication. Provide either 'sharedKey' or 'sasToken'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public AzureBSAuthentication getAuthentication() {
        return authentication;
    }

    /**
     * Azure Blob Storage authentication. Provide either 'sharedKey' or 'sasToken'.
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public void setAuthentication(AzureBSAuthentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jumpRef", jumpRef).append("proxyRef", proxyRef).append("alternativeRef", alternativeRef).append("connection", connection).append("authentication", authentication).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jumpRef).append(proxyRef).append(connection).append(alternativeRef).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AzureBlobStorageFragment) == false) {
            return false;
        }
        AzureBlobStorageFragment rhs = ((AzureBlobStorageFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jumpRef, rhs.jumpRef).append(proxyRef, rhs.proxyRef).append(connection, rhs.connection).append(alternativeRef, rhs.alternativeRef).append(authentication, rhs.authentication).isEquals();
    }

}
