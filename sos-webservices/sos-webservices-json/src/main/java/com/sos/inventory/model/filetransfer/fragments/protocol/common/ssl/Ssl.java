
package com.sos.inventory.model.filetransfer.fragments.protocol.common.ssl;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;


/**
 * TLS configuration. Choose either a trusted store or an untrusted server.
 * 
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
@JsonPropertyOrder({
    "trusted",
    "untrusted",
    "enabledProtocols"
})
public class Ssl {

    @JsonProperty("trusted")
    private Trusted trusted;
    @JsonProperty("untrusted")
    private Untrusted untrusted;
    /**
     * Comma-separated list of TLS protocol names (e.g. TLSv1.2,TLSv1.3). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("enabledProtocols")
    private String enabledProtocols;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Ssl() {
    }

    /**
     * 
     * @param enabledProtocols
     * @param untrusted
     * @param trusted
     */
    public Ssl(Trusted trusted, Untrusted untrusted, String enabledProtocols) {
        super();
        this.trusted = trusted;
        this.untrusted = untrusted;
        this.enabledProtocols = enabledProtocols;
    }

    @JsonProperty("trusted")
    public Trusted getTrusted() {
        return trusted;
    }

    @JsonProperty("trusted")
    public void setTrusted(Trusted trusted) {
        this.trusted = trusted;
    }

    @JsonProperty("untrusted")
    public Untrusted getUntrusted() {
        return untrusted;
    }

    @JsonProperty("untrusted")
    public void setUntrusted(Untrusted untrusted) {
        this.untrusted = untrusted;
    }

    /**
     * Comma-separated list of TLS protocol names (e.g. TLSv1.2,TLSv1.3). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("enabledProtocols")
    public String getEnabledProtocols() {
        return enabledProtocols;
    }

    /**
     * Comma-separated list of TLS protocol names (e.g. TLSv1.2,TLSv1.3). Or a single variable reference such as ${VAR}.
     * <p>
     * 
     * 
     */
    @JsonProperty("enabledProtocols")
    public void setEnabledProtocols(String enabledProtocols) {
        this.enabledProtocols = enabledProtocols;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("trusted", trusted).append("untrusted", untrusted).append("enabledProtocols", enabledProtocols).toString();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(enabledProtocols).append(untrusted).append(trusted).toHashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if ((other instanceof Ssl) == false) {
            return false;
        }
        Ssl rhs = ((Ssl) other);
        return new EqualsBuilder().append(enabledProtocols, rhs.enabledProtocols).append(untrusted, rhs.untrusted).append(trusted, rhs.trusted).isEquals();
    }

}
