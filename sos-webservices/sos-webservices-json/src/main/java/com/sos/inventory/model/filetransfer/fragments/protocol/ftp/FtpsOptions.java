
package com.sos.inventory.model.filetransfer.fragments.protocol.ftp;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.sos.inventory.model.filetransfer.fragments.protocol.common.ssl.Ssl;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * FTPS-specific options.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "securityMode",
    "ssl"
})
public class FtpsOptions {

    /**
     * FTP security mode. Fixed values: explicit, implicit. Or a single variable reference such as ${VAR}.
     * <p>
     * default: explicit
     * 
     */
    @JsonProperty("securityMode")
    @JsonPropertyDescription("default: explicit")
    private String securityMode;
    /**
     * TLS configuration. Choose either a trusted store or an untrusted server.
     * (Required)
     * 
     */
    @JsonProperty("ssl")
    @JsonPropertyDescription("TLS configuration. Choose either a trusted store or an untrusted server.")
    private Ssl ssl;

    /**
     * No args constructor for use in serialization
     * 
     */
    public FtpsOptions() {
    }

    /**
     * 
     * @param securityMode
     * @param ssl
     */
    public FtpsOptions(String securityMode, Ssl ssl) {
        super();
        this.securityMode = securityMode;
        this.ssl = ssl;
    }

    /**
     * FTP security mode. Fixed values: explicit, implicit. Or a single variable reference such as ${VAR}.
     * <p>
     * default: explicit
     * 
     */
    @JsonProperty("securityMode")
    public String getSecurityMode() {
        return securityMode;
    }

    /**
     * FTP security mode. Fixed values: explicit, implicit. Or a single variable reference such as ${VAR}.
     * <p>
     * default: explicit
     * 
     */
    @JsonProperty("securityMode")
    public void setSecurityMode(String securityMode) {
        this.securityMode = securityMode;
    }

    /**
     * TLS configuration. Choose either a trusted store or an untrusted server.
     * (Required)
     * 
     */
    @JsonProperty("ssl")
    public Ssl getSsl() {
        return ssl;
    }

    /**
     * TLS configuration. Choose either a trusted store or an untrusted server.
     * (Required)
     * 
     */
    @JsonProperty("ssl")
    public void setSsl(Ssl ssl) {
        this.ssl = ssl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("securityMode", securityMode).append("ssl", ssl).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(ssl).append(securityMode).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof FtpsOptions) == false) {
            return false;
        }
        FtpsOptions rhs = ((FtpsOptions) other);
        return new EqualsBuilder().append(ssl, rhs.ssl).append(securityMode, rhs.securityMode).isEquals();
    }

}
