
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.ftp.FtpKeepAlive;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.FtpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefAny;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FTP Base - Connection and authentication settings for the FTP / FTPS fragments.
 * <p>
 * Abstract base for FTP / FTPS fragments. Do not instantiate directly.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "jumpRef",
    "proxyRef",
    "alternativeRef",
    "connection",
    "authentication",
    "keepAlive",
    "transferMode"
})
public class AFtpFragment
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
     * Reference to an HTTP or SOCKS proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    @JsonPropertyDescription("Reference to an HTTP or SOCKS proxy fragment.")
    private ProxyFragmentRefAny proxyRef;
    /**
     * FTP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    private FtpAlternativeFragmentRef alternativeRef;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    private BasicConnection connection;
    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    private BasicAuthentication authentication;
    /**
     * Optional FTP keep-alive settings. Omit the property to disable keep-alive.
     * 
     */
    @JsonProperty("keepAlive")
    @JsonPropertyDescription("Optional FTP keep-alive settings. Omit the property to disable keep-alive.")
    private FtpKeepAlive keepAlive;
    /**
     * FTP transfer mode. Fixed values: binary, ascii. Or a single variable reference such as ${VAR}.
     * <p>
     * default: binary
     * 
     */
    @JsonProperty("transferMode")
    @JsonPropertyDescription("default: binary")
    private String transferMode;

    /**
     * No args constructor for use in serialization
     * 
     */
    public AFtpFragment() {
    }

    /**
     * 
     * @param proxyRef
     * @param alternativeRef
     * @param keepAlive
     * @param jumpRef
     * @param title
     * @param decryptionRef
     * @param version
     * @param path
     * @param transferMode
     * @param credentialStoreRef
     * @param connection
     * @param documentationName
     * @param authentication
     */
    public AFtpFragment(String jumpRef, ProxyFragmentRefAny proxyRef, FtpAlternativeFragmentRef alternativeRef, BasicConnection connection, BasicAuthentication authentication, FtpKeepAlive keepAlive, String transferMode, String credentialStoreRef, String decryptionRef, String version, String path, String title, String documentationName) {
        super(credentialStoreRef, decryptionRef, version, path, title, documentationName);
        this.jumpRef = jumpRef;
        this.proxyRef = proxyRef;
        this.alternativeRef = alternativeRef;
        this.connection = connection;
        this.authentication = authentication;
        this.keepAlive = keepAlive;
        this.transferMode = transferMode;
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
     * Reference to an HTTP or SOCKS proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    public ProxyFragmentRefAny getProxyRef() {
        return proxyRef;
    }

    /**
     * HTTP Proxy Reference
     * <p>
     * Reference to an HTTP or SOCKS proxy fragment.
     * 
     */
    @JsonProperty("proxyRef")
    public void setProxyRef(ProxyFragmentRefAny proxyRef) {
        this.proxyRef = proxyRef;
    }

    /**
     * FTP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public FtpAlternativeFragmentRef getAlternativeRef() {
        return alternativeRef;
    }

    /**
     * FTP Alternative Fragment Reference
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeRef")
    public void setAlternativeRef(FtpAlternativeFragmentRef alternativeRef) {
        this.alternativeRef = alternativeRef;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public BasicConnection getConnection() {
        return connection;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("connection")
    public void setConnection(BasicConnection connection) {
        this.connection = connection;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public BasicAuthentication getAuthentication() {
        return authentication;
    }

    /**
     * 
     * (Required)
     * 
     */
    @JsonProperty("authentication")
    public void setAuthentication(BasicAuthentication authentication) {
        this.authentication = authentication;
    }

    /**
     * Optional FTP keep-alive settings. Omit the property to disable keep-alive.
     * 
     */
    @JsonProperty("keepAlive")
    public FtpKeepAlive getKeepAlive() {
        return keepAlive;
    }

    /**
     * Optional FTP keep-alive settings. Omit the property to disable keep-alive.
     * 
     */
    @JsonProperty("keepAlive")
    public void setKeepAlive(FtpKeepAlive keepAlive) {
        this.keepAlive = keepAlive;
    }

    /**
     * FTP transfer mode. Fixed values: binary, ascii. Or a single variable reference such as ${VAR}.
     * <p>
     * default: binary
     * 
     */
    @JsonProperty("transferMode")
    public String getTransferMode() {
        return transferMode;
    }

    /**
     * FTP transfer mode. Fixed values: binary, ascii. Or a single variable reference such as ${VAR}.
     * <p>
     * default: binary
     * 
     */
    @JsonProperty("transferMode")
    public void setTransferMode(String transferMode) {
        this.transferMode = transferMode;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jumpRef", jumpRef).append("proxyRef", proxyRef).append("alternativeRef", alternativeRef).append("connection", connection).append("authentication", authentication).append("keepAlive", keepAlive).append("transferMode", transferMode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(proxyRef).append(alternativeRef).append(keepAlive).append(transferMode).append(jumpRef).append(connection).append(authentication).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof AFtpFragment) == false) {
            return false;
        }
        AFtpFragment rhs = ((AFtpFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(proxyRef, rhs.proxyRef).append(alternativeRef, rhs.alternativeRef).append(keepAlive, rhs.keepAlive).append(transferMode, rhs.transferMode).append(jumpRef, rhs.jumpRef).append(connection, rhs.connection).append(authentication, rhs.authentication).isEquals();
    }

}
