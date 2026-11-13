
package com.sos.inventory.model.filetransfer.fragments.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicAuthentication;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.BasicConnection;
import com.sos.inventory.model.filetransfer.fragments.protocol.ftp.FtpKeepAlive;
import com.sos.inventory.model.filetransfer.fragments.protocol.ftp.FtpOptions;
import com.sos.inventory.model.filetransfer.fragments.protocol.ftp.FtpsOptions;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.FtpAlternativeFragmentRef;
import com.sos.inventory.model.filetransfer.fragments.protocol.refs.ProxyFragmentRefAny;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FTP - Connection and authentication settings for the FTP fragments.
 * <p>
 * 
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "ftp",
    "ftps"
})
public class FtpFragment
    extends AFtpFragment
{

    /**
     * FTP-specific options.
     * 
     */
    @JsonProperty("ftp")
    @JsonPropertyDescription("FTP-specific options.")
    private FtpOptions ftp;
    /**
     * FTPS-specific options.
     * 
     */
    @JsonProperty("ftps")
    @JsonPropertyDescription("FTPS-specific options.")
    private FtpsOptions ftps;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FtpFragment() {
    }

    /**
     * 
     * @param ftps
     * @param proxyRef
     * @param ftp
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
    public FtpFragment(FtpOptions ftp, FtpsOptions ftps, String jumpRef, ProxyFragmentRefAny proxyRef, FtpAlternativeFragmentRef alternativeRef, BasicConnection connection, BasicAuthentication authentication, FtpKeepAlive keepAlive, String transferMode, String credentialStoreRef, String decryptionRef, String version, String path, String title, String documentationName) {
        super(jumpRef, proxyRef, alternativeRef, connection, authentication, keepAlive, transferMode, credentialStoreRef, decryptionRef, version, path, title, documentationName);
        this.ftp = ftp;
        this.ftps = ftps;
    }

    /**
     * FTP-specific options.
     * 
     */
    @JsonProperty("ftp")
    public FtpOptions getFtp() {
        return ftp;
    }

    /**
     * FTP-specific options.
     * 
     */
    @JsonProperty("ftp")
    public void setFtp(FtpOptions ftp) {
        this.ftp = ftp;
    }

    /**
     * FTPS-specific options.
     * 
     */
    @JsonProperty("ftps")
    public FtpsOptions getFtps() {
        return ftps;
    }

    /**
     * FTPS-specific options.
     * 
     */
    @JsonProperty("ftps")
    public void setFtps(FtpsOptions ftps) {
        this.ftps = ftps;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("ftp", ftp).append("ftps", ftps).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().appendSuper(super.hashCode()).append(ftps).append(ftp).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FtpFragment) == false) {
            return false;
        }
        FtpFragment rhs = ((FtpFragment) other);
        return new EqualsBuilder().appendSuper(super.equals(other)).append(ftps, rhs.ftps).append(ftp, rhs.ftp).isEquals();
    }

}
