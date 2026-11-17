
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.ftp.FtpKeepAlive;
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
    "jumpFragmentName",
    "httpProxyFragmentName",
    "socksProxyFragmentName",
    "alternativeFtpFragmentName",
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragmentName")
    private String socksProxyFragmentName;
    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeFtpFragmentName")
    private String alternativeFtpFragmentName;
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
     * @param jumpFragmentName
     * @param keepAlive
     * @param alternativeFtpFragmentName
     * @param transferMode
     * @param httpProxyFragmentName
     * @param connection
     * @param socksProxyFragmentName
     * @param authentication
     */
    public AFtpFragment(String jumpFragmentName, String httpProxyFragmentName, String socksProxyFragmentName, String alternativeFtpFragmentName, BasicConnection connection, BasicAuthentication authentication, FtpKeepAlive keepAlive, String transferMode) {
        super();
        this.jumpFragmentName = jumpFragmentName;
        this.httpProxyFragmentName = httpProxyFragmentName;
        this.socksProxyFragmentName = socksProxyFragmentName;
        this.alternativeFtpFragmentName = alternativeFtpFragmentName;
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
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragmentName")
    public String getSocksProxyFragmentName() {
        return socksProxyFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("socksProxyFragmentName")
    public void setSocksProxyFragmentName(String socksProxyFragmentName) {
        this.socksProxyFragmentName = socksProxyFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeFtpFragmentName")
    public String getAlternativeFtpFragmentName() {
        return alternativeFtpFragmentName;
    }

    /**
     * string without < and >
     * <p>
     * 
     * 
     */
    @JsonProperty("alternativeFtpFragmentName")
    public void setAlternativeFtpFragmentName(String alternativeFtpFragmentName) {
        this.alternativeFtpFragmentName = alternativeFtpFragmentName;
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
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jumpFragmentName", jumpFragmentName).append("httpProxyFragmentName", httpProxyFragmentName).append("socksProxyFragmentName", socksProxyFragmentName).append("alternativeFtpFragmentName", alternativeFtpFragmentName).append("connection", connection).append("authentication", authentication).append("keepAlive", keepAlive).append("transferMode", transferMode).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(jumpFragmentName).append(keepAlive).append(alternativeFtpFragmentName).append(transferMode).append(httpProxyFragmentName).append(connection).append(socksProxyFragmentName).append(authentication).toHashCode();
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
        return new EqualsBuilder().appendSuper(super.equals(other)).append(jumpFragmentName, rhs.jumpFragmentName).append(keepAlive, rhs.keepAlive).append(alternativeFtpFragmentName, rhs.alternativeFtpFragmentName).append(transferMode, rhs.transferMode).append(httpProxyFragmentName, rhs.httpProxyFragmentName).append(connection, rhs.connection).append(socksProxyFragmentName, rhs.socksProxyFragmentName).append(authentication, rhs.authentication).isEquals();
    }

}
