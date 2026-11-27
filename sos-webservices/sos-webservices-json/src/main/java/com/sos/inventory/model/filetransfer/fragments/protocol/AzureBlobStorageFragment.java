
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.azure.AzureBSAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.UrlConnection;
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
    "jumpFragmentName",
    "httpProxyFragmentName",
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
     * @param jumpFragmentName
     * @param httpProxyFragmentName
     * @param connection
     * @param authentication
     */
    public AzureBlobStorageFragment(String jumpFragmentName, String httpProxyFragmentName, UrlConnection connection, AzureBSAuthentication authentication) {
        super();
        this.jumpFragmentName = jumpFragmentName;
        this.httpProxyFragmentName = httpProxyFragmentName;
        this.connection = connection;
        this.authentication = authentication;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jumpFragmentName", jumpFragmentName).append("httpProxyFragmentName", httpProxyFragmentName).append("connection", connection).append("authentication", authentication).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jumpFragmentName).append(httpProxyFragmentName).append(connection).append(authentication).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jumpFragmentName, rhs.jumpFragmentName).append(httpProxyFragmentName, rhs.httpProxyFragmentName).append(connection, rhs.connection).append(authentication, rhs.authentication).isEquals();
    }

}
